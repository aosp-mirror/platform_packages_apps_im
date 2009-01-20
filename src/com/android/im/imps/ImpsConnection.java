/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.im.imps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.android.im.engine.ChatGroupManager;
import com.android.im.engine.ChatSessionManager;
import com.android.im.engine.Contact;
import com.android.im.engine.ContactListManager;
import com.android.im.engine.ImConnection;
import com.android.im.engine.ImErrorInfo;
import com.android.im.engine.ImException;
import com.android.im.engine.LoginInfo;
import com.android.im.engine.Presence;
import com.android.im.imps.ImpsConnectionConfig.CirMethod;
import com.android.im.imps.ImpsConnectionConfig.TransportType;
import com.android.im.imps.Primitive.TransactionMode;

/**
 * An implementation of ImConnection of Wireless Village IMPS protocol.
 */
public class ImpsConnection extends ImConnection {
    ImpsConnectionConfig mConfig;

    DataChannel mDataChannel;
    private CirChannel mCirChannel;
    private PrimitiveDispatcherThread mDispatcherThread;

    ImpsSession mSession;
    ImpsTransactionManager mTransactionManager;
    private ImpsChatSessionManager mChatSessionManager;
    private ImpsContactListManager mContactListManager;
    private ImpsChatGroupManager   mChatGroupManager;
    private boolean mReestablishing;

    /**
     * Constructs a new WVConnection with a WVConnectionConfig object.
     *
     * @param config the configuration.
     * @throws ImException if there's an error in the configuration.
     */
    public ImpsConnection(ImpsConnectionConfig config) {
        super();

        mConfig = config;

        mTransactionManager = new ImpsTransactionManager(this);
        mChatSessionManager = new ImpsChatSessionManager(this);
        mContactListManager = new ImpsContactListManager(this);
        mChatGroupManager   = new ImpsChatGroupManager(this);
    }

    /**
     * Gets the configuration of this connection.
     *
     * @return the configuration.
     */
    ImpsConnectionConfig getConfig() {
        return mConfig;
    }

    synchronized void shutdownOnError(ImErrorInfo error) {
        if(mState == DISCONNECTED) {
            return;
        }

        if (mCirChannel != null) {
            mCirChannel.shutdown();
        }
        if (mDispatcherThread != null) {
            mDispatcherThread.shutdown();
        }
        if (mDataChannel != null) {
            mDataChannel.shutdown();
        }
        if (mContactListManager != null && !mReestablishing) {
            mContactListManager.reset();
        }
        setState(mReestablishing ? SUSPENDED: DISCONNECTED, error);
        mReestablishing = false;
    }

    void shutdown(){
        shutdownOnError(null);
    }

    @Override
    public int getCapability() {
        return CAPABILITY_GROUP_CHAT | CAPABILITY_SESSION_REESTABLISHMENT;
    }

    @Override
    public void loginAsync(LoginInfo loginInfo) {
        if (!checkAndSetState(DISCONNECTED)) {
            return;
        }
        try {
            mSession = new ImpsSession(this, loginInfo);
        } catch (ImException e) {
            setState(DISCONNECTED, e.getImError());
            return;
        }
        doLogin();
    }

    @Override
    public void reestablishSessionAsync(
            HashMap<String, String> cookie) {
        if (!checkAndSetState(SUSPENDED)) {
            return;
        }
        // If we can resume from the data channel, which means the
        // session is still valid, we can just re-use the existing
        // session and don't need to re-establish it.
        if (mDataChannel.resume()) {
            try {
                setupCIRChannel();
            } catch(ImException e) {}
            setState(LOGGED_IN, null);
        } else {
            // Failed to resume the data channel which means the
            // session might have expired, we need to re-establish
            // the session by signing in again.
            mReestablishing = true;
            try {
                mSession = new ImpsSession(this, cookie);
            } catch (ImException e) {
                setState(DISCONNECTED, e.getImError());
                return;
            }
            doLogin();
        }
    }

    @Override
    public void networkTypeChanged() {
        if (mCirChannel != null) {
            mCirChannel.reconnect();
        }
    }

    private synchronized boolean checkAndSetState(int state) {
        if(mState != state){
            return false;
        }
        setState(LOGGING_IN, null);
        return true;
    }

    private void doLogin() {
        try {
            if (mConfig.useSmsAuth()) {
                mDataChannel = new SmsDataChannel(this);
            } else {
                mDataChannel = createDataChannel();
            }
            mDataChannel.connect();
        } catch (ImException e) {
            ImErrorInfo error = e.getImError();
            if(error == null){
                error = new ImErrorInfo(ImErrorInfo.UNKNOWN_LOGIN_ERROR,
                        e.getMessage());
            }
            shutdownOnError(error);
            return;
        }

        mDispatcherThread = new PrimitiveDispatcherThread(mDataChannel);
        mDispatcherThread.start();

        LoginTransaction login = new LoginTransaction();
        login.startAuthenticate();
    }

    @Override
    public HashMap<String, String> getSessionContext() {
        if(mState != LOGGED_IN) {
            return null;
        } else {
            return mSession.getContext();
        }
    }

    class LoginTransaction extends MultiPhaseTransaction {

        LoginTransaction() {
            // We're not passing completion to ImpsAsyncTransaction. Instead
            // we'll handle the notification in LoginTransaction.
            super(mTransactionManager);
        }

        public void startAuthenticate() {
            Primitive login = buildBasicLoginReq();
            if (mConfig.use4wayLogin()) {
                // first login request of 4 way login
                String[] supportedDigestSchema = mConfig.getPasswordDigest().getSupportedDigestSchema();
                for (String element : supportedDigestSchema) {
                    login.addElement(ImpsTags.DigestSchema, element);
                }
            } else {
                // 2 way login
                login.addElement(ImpsTags.Password, mSession.getPassword());
            }
            sendRequest(login);
        }

        @Override
        public TransactionStatus processResponse(Primitive response) {
            if (response.getElement(ImpsTags.SessionID) != null) {
                // If server chooses authentication based on network, we might
                // got the final Login-Response before the 2nd Login-Request.
                String sessionId = response.getElementContents(ImpsTags.SessionID);
                String keepAliveTime = response.getElementContents(ImpsTags.KeepAliveTime);
                String capablityReqeust = response.getElementContents(ImpsTags.CapabilityRequest);

                long keepAlive = ImpsUtils.parseLong(keepAliveTime,
                        mConfig.getDefaultKeepAliveInterval());
                // make sure we always have time to send keep-alive requests.
                // see buildBasicLoginReq().
                keepAlive -= 5;
                mSession.setId(sessionId);
                mSession.setKeepAliveTime(keepAlive);
                mSession.setCapablityRequestRequired(ImpsUtils.isTrue(capablityReqeust));

                onAuthenticated();
                return TransactionStatus.TRANSACTION_COMPLETED;
            } else {
                return sendSecondLogin(response);
            }
        }

        @Override
        public TransactionStatus processResponseError(ImpsErrorInfo error) {
            if (error.getCode() == ImpsConstants.STATUS_UNAUTHORIZED
                    && error.getPrimitive() != null) {
                if (mConfig.use4wayLogin()) {
                    // Not really an error. Send the 2nd Login-Request.
                    return sendSecondLogin(error.getPrimitive());
                } else {
                    // We have already sent password in 2way login, while OZ's
                    // yahoo gateway server returns "401 - Further authorization
                    // required" instead of "409 - Invalid password" if the
                    // password only contains spaces.
                    shutdownOnError(new ImErrorInfo(409, "Invalid password"));
                    return TransactionStatus.TRANSACTION_COMPLETED;
                }
            } else if(error.getCode() == ImpsConstants.STATUS_COULD_NOT_RECOVER_SESSION) {
                // The server could not recover the session, create a new
                // session and try to login again.
                LoginInfo loginInfo = mSession.getLoginInfo();
                try {
                    mSession = new ImpsSession(ImpsConnection.this, loginInfo);
                } catch (ImException ignore) {
                    // This shouldn't happen since we have tried to login with
                    // the loginInfo
                }
                startAuthenticate();
                return TransactionStatus.TRANSACTION_COMPLETED;
            } else {
                shutdownOnError(error);
                return TransactionStatus.TRANSACTION_COMPLETED;
            }
        }

        private TransactionStatus sendSecondLogin(Primitive res) {
            try {
                Primitive secondLogin = buildBasicLoginReq();

                String nonce = res.getElementContents(ImpsTags.Nonce);
                String digestSchema = res.getElementContents(ImpsTags.DigestSchema);
                String digestBytes = mConfig.getPasswordDigest().digest(digestSchema, nonce,
                        mSession.getPassword());

                secondLogin.addElement(ImpsTags.DigestBytes, digestBytes);

                sendRequest(secondLogin);
                return TransactionStatus.TRANSACTION_CONTINUE;
            } catch (ImException e) {
                ImpsLog.logError(e);
                shutdownOnError(new ImErrorInfo(ImErrorInfo.UNKNOWN_ERROR, e.toString()));
                return TransactionStatus.TRANSACTION_COMPLETED;
            }
        }

        private void onAuthenticated() {
            // The user has chosen logout before the session established, just
            // send the Logout-Request in this case.
            if (mState == LOGGING_OUT) {
                sendLogoutRequest();
                return;
            }

            if (mConfig.useSmsAuth()
                    && mConfig.getDataChannelBinding() != TransportType.SMS) {
                // SMS data channel was used if it's set to send authentication
                // over SMS. Switch to the config data channel after authentication
                // completed.
                try {
                    DataChannel dataChannel = createDataChannel();
                    dataChannel.connect();

                    mDataChannel.shutdown();
                    mDataChannel = dataChannel;
                    mDispatcherThread.changeDataChannel(dataChannel);
                } catch (ImException e) {
                    // This should not happen since only http data channel which
                    // does not do the real network connection in connect() is
                    // valid here now.
                    logoutAsync();
                    return;
                }
            }

            if(mSession.isCapablityRequestRequired()) {
                mSession.negotiateCapabilityAsync(new AsyncCompletion(){
                    public void onComplete() {
                        onCapabilityNegotiated();
                    }

                    public void onError(ImErrorInfo error) {
                        shutdownOnError(error);
                    }
                });
            } else {
                onCapabilityNegotiated();
            }
        }

        void onCapabilityNegotiated() {
            mDataChannel.setServerMinPoll(mSession.getServerPollMin());
            if(getConfig().getCirChannelBinding() != CirMethod.NONE) {
                try {
                    setupCIRChannel();
                } catch (ImException e) {
                    shutdownOnError(new ImErrorInfo(
                            ImErrorInfo.UNSUPPORTED_CIR_CHANNEL, e.toString()));
                    return;
                }
            }

            mSession.negotiateServiceAsync(new AsyncCompletion(){
                public void onComplete() {
                    onServiceNegotiated();
                }

                public void onError(ImErrorInfo error) {
                    shutdownOnError(error);
                }
            });
        }

        void onServiceNegotiated() {
            mDataChannel.startKeepAlive(mSession.getKeepAliveTime());

            retrieveUserPresenceAsync(new AsyncCompletion() {
                public void onComplete() {
                    setState(LOGGED_IN, null);
                    if (mReestablishing) {
                        ImpsContactListManager listMgr=  (ImpsContactListManager) getContactListManager();
                        listMgr.subscribeToAllListAsync();
                        mReestablishing = false;
                    }
                }

                public void onError(ImErrorInfo error) {
                    // Just continue. initUserPresenceAsync already made a
                    // default mUserPresence for us.
                    onComplete();
                }
            });
        }
    }

    @Override
    public void logoutAsync() {
        setState(LOGGING_OUT, null);
        // Shutdown the CIR channel first.
        if(mCirChannel != null) {
            mCirChannel.shutdown();
            mCirChannel = null;
        }

        // Only send the Logout-Request if the session has been established.
        if (mSession.getID() != null) {
            sendLogoutRequest();
        }
    }

    void sendLogoutRequest() {
        // We cannot shut down our connections in ImpsAsyncTransaction.onResponse()
        // because at that time the logout transaction itself hasn't ended yet. So
        // we have to do this in this completion object.
        AsyncCompletion completion = new AsyncCompletion() {
            public void onComplete() {
                shutdown();
            }

            public void onError(ImErrorInfo error) {
                // We simply ignore all errors when logging out.
                // NowIMP responds a <Disconnect> instead of <Status> on logout request.
                shutdown();
            }
        };
        AsyncTransaction tx = new SimpleAsyncTransaction(mTransactionManager,
                completion);
        Primitive logoutPrimitive = new Primitive(ImpsTags.Logout_Request);
        tx.sendRequest(logoutPrimitive);
    }

    public ImpsSession getSession() {
        return mSession;
    }

    @Override
    public Contact getLoginUser() {
        if(mSession == null){
            return null;
        }
        Contact loginUser = mSession.getLoginUser();
        loginUser.setPresence(getUserPresence());
        return loginUser;
    }

    @Override
    public int[] getSupportedPresenceStatus() {
        return mConfig.getPresenceMapping().getSupportedPresenceStatus();
    }

    public ImpsTransactionManager getTransactionManager() {
        return mTransactionManager;
    }

    @Override
    public ChatSessionManager getChatSessionManager() {
        return mChatSessionManager;
    }

    @Override
    public ContactListManager getContactListManager() {
        return mContactListManager;
    }

    @Override
    public ChatGroupManager getChatGroupManager() {
        return mChatGroupManager;
    }

    /**
     * Sends a specific primitive to the server. It will return immediately
     * after the primitive has been put to the sending queue.
     *
     * @param primitive the packet to send.
     */
    void sendPrimitive(Primitive primitive) {
        mDataChannel.sendPrimitive(primitive);
    }

    /**
     * Sends a PollingRequest to the server.
     */
    void sendPollingRequest() {
        Primitive pollingRequest = new Primitive(ImpsTags.Polling_Request);
        pollingRequest.setSession(getSession().getID());
        mDataChannel.sendPrimitive(pollingRequest);
    }

    private DataChannel createDataChannel() throws ImException {
        TransportType dataChannelBinding = mConfig.getDataChannelBinding();
        if (dataChannelBinding == TransportType.HTTP) {
            return new HttpDataChannel(this);
        } else if (dataChannelBinding == TransportType.SMS) {
            return new SmsDataChannel(this);
        } else {
            throw new ImException("Unsupported data channel binding");
        }
    }

    void setupCIRChannel() throws ImException {
        if(mConfig.getDataChannelBinding() == TransportType.SMS) {
            // No CIR channel is needed, do nothing.
            return;
        }
        CirMethod cirMethod = mSession.getCurrentCirMethod();
        if (cirMethod == null) {
            cirMethod = mConfig.getCirChannelBinding();

            if (!mSession.getSupportedCirMethods().contains(cirMethod)) {
                // Sever don't support the CIR method
                cirMethod = CirMethod.SHTTP;
            }
            mSession.setCurrentCirMethod(cirMethod);
        }

        if (cirMethod == CirMethod.SHTTP) {
            mCirChannel = new HttpCirChannel(this, mDataChannel);
        } else if (cirMethod == CirMethod.STCP) {
            mCirChannel = new TcpCirChannel(this);
        } else if (cirMethod == CirMethod.SSMS) {
            mCirChannel = new SmsCirChannel(this);
        } else if (cirMethod == CirMethod.NONE) {
            //Do nothing
        } else {
            throw new ImException(ImErrorInfo.UNSUPPORTED_CIR_CHANNEL,
                    "Unsupported CIR channel binding");
        }

        if(mCirChannel != null) {
            mCirChannel.connect();
        }
    }

    private class PrimitiveDispatcherThread extends Thread {
        private boolean stopped;
        private DataChannel mChannel;

        public PrimitiveDispatcherThread(DataChannel channel)
        {
            super("ImpsPrimitiveDispatcher");
            mChannel = channel;
        }

        public void changeDataChannel(DataChannel channel) {
            mChannel = channel;
            interrupt();
        }

        @Override
        public void run() {
            Primitive primitive = null;
            while (!stopped) {
                try {
                    primitive = mChannel.receivePrimitive();
                } catch (InterruptedException e) {
                    if (stopped) {
                        break;
                    }
                    primitive = null;
                }

                if (primitive != null) {
                    try {
                        processIncomingPrimitive(primitive);
                    } catch (Throwable t) {
                        // We don't know what is going to happen in the various
                        // listeners.
                        ImpsLog.logError("ImpsDispatcher: uncaught Throwable", t);
                    }
                }
            }
        }

        void shutdown() {
            stopped = true;
            interrupt();
        }
    }

    /**
     * Handles the primitive received from the server.
     *
     * @param primitive the received primitive.
     */
    void processIncomingPrimitive(Primitive primitive) {
        // if CIR is 'F', the CIR channel is not available. Re-establish it.
        if (primitive.getCir() != null && ImpsUtils.isFalse(primitive.getCir())) {
            if(mCirChannel != null) {
                mCirChannel.shutdown();
            }
            try {
                setupCIRChannel();
            } catch (ImException e) {
                e.printStackTrace();
            }
        }

        if (primitive.getPoll() != null && ImpsUtils.isTrue(primitive.getPoll())) {
            sendPollingRequest();
        }

        if (primitive.getType().equals(ImpsTags.Disconnect)) {
            if (mState != LOGGING_OUT) {
                ImErrorInfo error = ImpsUtils.checkResultError(primitive);
                shutdownOnError(error);
                return;
            }
        }

        if (primitive.getTransactionMode() == TransactionMode.Response) {
            ImpsErrorInfo error = ImpsUtils.checkResultError(primitive);
            if (error != null) {
                int code = error.getCode();
                if (code == ImpsErrorInfo.SESSION_EXPIRED
                        || code == ImpsErrorInfo.FORCED_LOGOUT
                        || code == ImpsErrorInfo.INVALID_SESSION) {
                    shutdownOnError(error);
                    return;
                }
            }
        }

        // According to the IMPS spec, only VersionDiscoveryResponse which
        // are not supported now doesn't have a transaction ID.
        if (primitive.getTransactionID() != null) {
            mTransactionManager.notifyIncomingPrimitive(primitive);
        }
    }

    @Override
    protected void doUpdateUserPresenceAsync(Presence presence) {
        ArrayList<PrimitiveElement> presenceSubList = ImpsPresenceUtils.buildUpdatePresenceElems(
                mUserPresence, presence, mConfig.getPresenceMapping());
        Primitive request = buildUpdatePresenceReq(presenceSubList);
        // Need to make a copy because the presence passed in may change
        // before the transaction finishes.
        final Presence newPresence = new Presence(presence);

        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {

            @Override
            public void onResponseOk(Primitive response) {
                savePresenceChange(newPresence);
                notifyUserPresenceUpdated();
            }

            @Override
            public void onResponseError(ImpsErrorInfo error) {
                notifyUpdateUserPresenceError(error);
            }
        };
        tx.sendRequest(request);
    }

    void savePresenceChange(Presence newPresence) {
        mUserPresence.setStatusText(newPresence.getStatusText());
        mUserPresence.setStatus(newPresence.getStatus());
        mUserPresence.setAvatar(newPresence.getAvatarData(), newPresence.getAvatarType());
        // no need to update extended info because it's always read only.
    }

    void retrieveUserPresenceAsync(final AsyncCompletion completion) {
        Primitive request = new Primitive(ImpsTags.GetPresence_Request);

        request.addElement(this.getSession().getLoginUserAddress().toPrimitiveElement());
        AsyncTransaction tx = new AsyncTransaction(mTransactionManager){

            @Override
            public void onResponseOk(Primitive response) {
                PrimitiveElement presence = response.getElement(ImpsTags.Presence);
                PrimitiveElement presenceSubList = presence.getChild(ImpsTags.PresenceSubList);
                mUserPresence = ImpsPresenceUtils.extractPresence(presenceSubList,
                        mConfig.getPresenceMapping());
                // XXX: workaround for the OZ IMPS GTalk server that
                // returns an initial 'F' OnlineStatus. Set the online
                // status to available in this case.
                if(mUserPresence.getStatus() == Presence.OFFLINE) {
                    mUserPresence.setStatus(Presence.AVAILABLE);
                }
                compareAndUpdateClientInfo();
            }

            @Override
            public void onResponseError(ImpsErrorInfo error) {
                mUserPresence = new Presence(Presence.AVAILABLE, "", null,
                        null, Presence.CLIENT_TYPE_MOBILE, ImpsUtils.getClientInfo());
                completion.onError(error);
            }

            private void compareAndUpdateClientInfo() {
                if (!ImpsUtils.getClientInfo().equals(mUserPresence.getExtendedInfo())) {
                    updateClientInfoAsync(completion);
                    return;
                }
                // no need to update our client info to the server again
                completion.onComplete();
            }
        };

        tx.sendRequest(request);
    }

    void updateClientInfoAsync(AsyncCompletion completion) {
        Primitive updatePresenceRequest = buildUpdatePresenceReq(buildClientInfoElem());

        AsyncTransaction tx = new SimpleAsyncTransaction(mTransactionManager,
                completion);
        tx.sendRequest(updatePresenceRequest);
    }

    private Primitive buildUpdatePresenceReq(PrimitiveElement presence) {
        ArrayList<PrimitiveElement> presences = new ArrayList<PrimitiveElement>();

        presences.add(presence);

        return buildUpdatePresenceReq(presences);
    }

    private Primitive buildUpdatePresenceReq(ArrayList<PrimitiveElement> presences) {
        Primitive updatePresenceRequest = new Primitive(ImpsTags.UpdatePresence_Request);

        PrimitiveElement presenceSubList = updatePresenceRequest
                .addElement(ImpsTags.PresenceSubList);
        presenceSubList.setAttribute(ImpsTags.XMLNS, mConfig.getPresenceNs());

        for (PrimitiveElement presence : presences) {
            presenceSubList.addChild(presence);
        }

        return updatePresenceRequest;
    }

    private PrimitiveElement buildClientInfoElem() {
        PrimitiveElement clientInfo = new PrimitiveElement(ImpsTags.ClientInfo);
        clientInfo.addChild(ImpsTags.Qualifier, true);

        Map<String, String> map = ImpsUtils.getClientInfo();
        for (Map.Entry<String, String> item : map.entrySet()) {
            clientInfo.addChild(item.getKey(), item.getValue());
        }

        return clientInfo;
    }

    Primitive buildBasicLoginReq() {
        Primitive login = new Primitive(ImpsTags.Login_Request);
        login.addElement(ImpsTags.UserID, mSession.getUserName());
        PrimitiveElement clientId = login.addElement(ImpsTags.ClientID);
        clientId.addChild(ImpsTags.URL, mConfig.getClientId());
        if (mConfig.getMsisdn() != null) {
            clientId.addChild(ImpsTags.MSISDN, mConfig.getMsisdn());
        }
        // we request for a bigger TimeToLive value than our default keep
        // alive interval to make sure we always have time to send the keep
        // alive requests.
        login.addElement(ImpsTags.TimeToLive,
                Integer.toString(mConfig.getDefaultKeepAliveInterval() + 5));
        login.addElement(ImpsTags.SessionCookie, mSession.getCookie());
        return login;
    }

    @Override
    synchronized public void suspend() {
        setState(SUSPENDING, null);

        if (mCirChannel != null) {
            mCirChannel.shutdown();
        }

        if (mDataChannel != null) {
            mDataChannel.suspend();
        }

        setState(SUSPENDED, null);
    }
}
