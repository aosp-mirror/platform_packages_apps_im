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
import java.util.Date;

import android.text.format.Time;
import android.util.TimeFormatException;

import com.android.im.engine.Address;
import com.android.im.engine.ChatSession;
import com.android.im.engine.ChatSessionManager;
import com.android.im.engine.ImEntity;
import com.android.im.engine.ImErrorInfo;
import com.android.im.engine.Message;

/**
 * The implementation of ChatSessionManager with Wireless Village IMPS protocol.
 */
public class ImpsChatSessionManager extends ChatSessionManager
            implements ServerTransactionListener {
    private ImpsConnection mConnection;
    private ImpsTransactionManager mTransactionManager;
    private ArrayList<Message> mMessageQueue;
    private boolean mStartNotifying;

    ImpsChatSessionManager(ImpsConnection connection) {
        mConnection = connection;
        mMessageQueue = new ArrayList<Message>();

        mTransactionManager = connection.getTransactionManager();
        mTransactionManager.setTransactionListener(ImpsTags.NewMessage, this);
        mTransactionManager.setTransactionListener(ImpsTags.DeliveryReport_Request, this);
    }

    @Override
    protected void sendMessageAsync(final ChatSession ses, final Message message) {
        // force to send from the currently logged user.
        message.setFrom(mConnection.getSession().getLoginUserAddress());

        if(message.getDateTime() == null) {
            message.setDateTime(new Date());
        }
        Primitive primitive = createSendMessagePrimitive(message);
        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {

            @Override
            public void onResponseOk(Primitive response) { }

            @Override
            public void onResponseError(ImpsErrorInfo error) {
                ses.onSendMessageError(message, error);
            }
        };

        tx.sendRequest(primitive);
    }

    public void notifyServerTransaction(ServerTransaction tx) {
        Primitive primitive = tx.getRequest();

        if (ImpsTags.NewMessage.equals(primitive.getType())) {
            Message msg = extractMessage(primitive);

            // send response to the server.
            Primitive response = new Primitive(ImpsTags.MessageDelivered);
            response.addElement(ImpsTags.MessageID, msg.getID());
            tx.sendResponse(response);

            synchronized(mMessageQueue) {
                if(mStartNotifying){
                    processMessage(msg);
                } else {
                    mMessageQueue.add(msg);
                }
            }
        } else if(ImpsTags.DeliveryReport_Request.equals(primitive.getType())) {
            tx.sendStatusResponse(ImpsConstants.SUCCESS_CODE);

            // We only notify the user when an error occurs.
            ImErrorInfo error = ImpsUtils.checkResultError(primitive);
            if(error != null) {
                PrimitiveElement msgInfo = primitive.getElement(ImpsTags.MessageInfo);
                String msgId = msgInfo.getChildContents(ImpsTags.MessageID);
                PrimitiveElement recipent = msgInfo.getChild(ImpsTags.Recipient);
                ImpsAddress recipentAddress = ImpsAddress.fromPrimitiveElement(
                        recipent.getFirstChild());
                ChatSession session = findSession(recipentAddress);
                if(session != null) {
                    session.onSendMessageError(msgId, error);
                } else {
                    ImpsLog.log("Session has closed when received delivery error: "
                            + error);
                }
            }
        }
    }

    public void start() {
        synchronized (mMessageQueue) {
            mStartNotifying = true;
            for (Message message : mMessageQueue) {
                processMessage(message);
            }
            mMessageQueue.clear();
        }
    }

    /**
     * Extracts a message from a NewMessage primitive.
     *
     * @param primitive
     *            the NewMessage primitive.
     * @return an instance of message.
     */
    private Message extractMessage(Primitive primitive) {
        String msgBody = primitive.getElementContents(ImpsTags.ContentData);
        if (msgBody == null) {
            msgBody = "";
        }
        Message msg = new Message(msgBody);

        PrimitiveElement msgInfo = primitive.getElement(ImpsTags.MessageInfo);

        String id = msgInfo.getChildContents(ImpsTags.MessageID);
        msg.setID(id);

        PrimitiveElement sender = msgInfo.getChild(ImpsTags.Sender);
        msg.setFrom(ImpsAddress.fromPrimitiveElement(sender.getFirstChild()));

        PrimitiveElement recipent = msgInfo.getChild(ImpsTags.Recipient);
        if (recipent != null && recipent.getFirstChild() != null) {
            msg.setTo(ImpsAddress.fromPrimitiveElement(recipent.getFirstChild()));
        } else {
            msg.setTo(mConnection.getLoginUser().getAddress());
        }

        String dateTime = msgInfo.getChildContents(ImpsTags.DateTime);
        if (dateTime != null) {
            try {
                Time t = new Time();
                t.parse(dateTime);
                msg.setDateTime(new Date(t.toMillis(false /* use isDst */)));
            } catch (TimeFormatException e) {
                msg.setDateTime(new Date());
            }
        } else {
            msg.setDateTime(new Date());
        }
        return msg;
    }

    /**
     * Creates a SendMessage-Request primitive to send message.
     *
     * @param message the message to send.
     * @return the SendMessage-Request primitive.
     */
    private Primitive createSendMessagePrimitive(Message message) {
        Primitive primitive = new Primitive(ImpsTags.SendMessage_Request);

        primitive.addElement(ImpsTags.DeliveryReport,
                mConnection.getConfig().needDeliveryReport());

        PrimitiveElement msgInfo = primitive.addElement(ImpsTags.MessageInfo);
        PrimitiveElement recipient = msgInfo.addChild(ImpsTags.Recipient);
        recipient.addChild(((ImpsAddress)message.getTo()).toPrimitiveElement());
        PrimitiveElement sender = msgInfo.addChild(ImpsTags.Sender);
        sender.addChild(((ImpsAddress)message.getFrom()).toPrimitiveElement());

        // XXX: ContentType is optional and by default is "text/plain".
        // However without this the OZ IMPS server wouldn't reply to our
        // SendMessage requests and just let the HTTP connection times out.
        msgInfo.addChild(ImpsTags.ContentType, "text/plain");

        // optional
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(message.getDateTime());
//        msgInfo.addChild(ImpsTags.DateTime, DateUtils.writeDateTime(calendar));

        String msgBody = message.getBody();
        msgInfo.addChild(ImpsTags.ContentSize, Integer.toString(msgBody.length()));
        primitive.addElement(ImpsTags.ContentData, msgBody);

        return primitive;
    }

    /**
     * Processes an incoming message. Called by the sub protocol implementation
     * when an incoming message arrived.
     *
     * @param msg the incoming message.
     */
    void processMessage(Message msg) {
        ImpsAddress from = (ImpsAddress) msg.getFrom();
        ImpsAddress to = (ImpsAddress) msg.getTo();

        ImpsAddress address = (to instanceof ImpsGroupAddress) ? to : from;

        synchronized (this) {
            ChatSession ses = findSession(address);
            if (ses == null) {
                ImEntity participant = address.getEntity(mConnection);
                if (participant != null) {
                    ses = createChatSession(address.getEntity(mConnection));
                } else {
                    ImpsLog.log("Message from unknown sender");
                    return;
                }
            }
            ses.onReceiveMessage(msg);
        }
    }

    /**
     * Finds the ChatSession which the message belongs to.
     *
     * @param msg the message.
     * @return the ChatSession or <code>null</code> if the session not exists.
     */
    private ChatSession findSession(Address address) {
        for(ChatSession session : mSessions) {
            ImEntity participant = session.getParticipant();
            if(participant.getAddress().equals(address)) {
                return session;
            }
        }
        return null;
    }
}
