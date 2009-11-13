/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
import java.util.Collection;
import java.util.Vector;

import com.android.im.engine.Address;
import com.android.im.engine.Contact;
import com.android.im.engine.ContactList;
import com.android.im.engine.ContactListListener;
import com.android.im.engine.ContactListManager;
import com.android.im.engine.ImConnection;
import com.android.im.engine.ImErrorInfo;
import com.android.im.engine.ImException;
import com.android.im.engine.Presence;
import com.android.im.engine.SubscriptionRequestListener;
import com.android.im.imps.ImpsConstants.ImpsVersion;
import com.android.im.plugin.PresenceMapping;

/**
 * An implementation of ContactListManager of Wireless Village IMPS protocol.
 */
public class ImpsContactListManager extends ContactListManager
        implements ServerTransactionListener {
    private ImpsConnection mConnection;
    private String mDefaultDomain;
    ImpsTransactionManager mTransactionManager;
    ImpsConnectionConfig mConfig;

    boolean mAllowAutoSubscribe = true;

    ArrayList<Contact> mSubscriptionRequests;

    /**
     * Constructs the manager with specific connection.
     *
     * @param connection the connection related to the manager
     */
    ImpsContactListManager(ImpsConnection connection) {
        mConnection = connection;
        mConfig = connection.getConfig();
        mDefaultDomain = mConfig.getDefaultDomain();
        mTransactionManager = connection.getTransactionManager();

        mTransactionManager.setTransactionListener(
                ImpsTags.PresenceNotification_Request, this);
        mTransactionManager.setTransactionListener(
                ImpsTags.PresenceAuth_Request, this);
    }

    @Override
    public Contact createTemporaryContact(String address){
        ImpsAddress impsAddr = new ImpsUserAddress(normalizeAddress(address));
        return new Contact(impsAddr, impsAddr.getScreenName());
    }

    @Override
    public String normalizeAddress(String address) {
        String s = address.toLowerCase();
        if (!s.startsWith(ImpsConstants.ADDRESS_PREFIX)) {
            s = ImpsConstants.ADDRESS_PREFIX + s;
        }
        if (mDefaultDomain != null && s.indexOf('@') == -1) {
            s = s + "@" + mDefaultDomain;
        }
        return s;
    }

    @Override
    public synchronized void loadContactListsAsync() {
        if (getState() != LISTS_NOT_LOADED) {
            return;
        }

        setState(LISTS_LOADING);

        // load blocked list first
        Primitive request = new Primitive(ImpsTags.GetBlockedList_Request);

        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                // don't notify the 501 not implemented error
                if (error.getCode() != ImpsConstants.STATUS_NOT_IMPLEMENTED) {
                    notifyContactError(
                            ContactListListener.ERROR_LOADING_BLOCK_LIST,
                            error, null, null);
                }

                next();
            }

            @Override
            public void onResponseOk(Primitive response) {
                extractBlockedContacts(response);

                next();
            }

            private void next() {
                setState(BLOCKED_LIST_LOADED);
                new LoadContactsTransaction().startGetContactLists();
                //createDefaultAttributeListAsync();
            }
        };

        tx.sendRequest(request);
    }

    Vector<ImpsContactListAddress> extractListAddresses(Primitive response){
        Vector<ImpsContactListAddress> addresses = new Vector<ImpsContactListAddress>();

        for (PrimitiveElement child : response.getContentElement()
                .getChildren()) {
            if (child.getTagName().equals(ImpsTags.ContactList)) {
                // FIXME: ignore the PEP contact lists for now
                // PEP: "Presence Enhanced Phonebook and Instant Messaging
                // Application Category" specification from Nokia and SonyEricsson.
                //  ~IM_subscriptions
                //  ~pep1.0_privatelist
                //  ~pep1.0_blocklist
                //  ~pep1.0_friendlist
                //  ~pep1.0_subscriptions-*
                if (child.getContents().contains("/~pep1.0_")) {
                    continue;
                }
                addresses.add(new ImpsContactListAddress(child.getContents()));
            }
        }

        String defaultListAddress = response.getElementContents(ImpsTags.DefaultContactList);
        if (null != defaultListAddress) {
            addresses.add(new ImpsContactListAddress(defaultListAddress));
        }

        return addresses;
    }

    public void fetchPresence(ImpsAddress[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return;
        }

        Primitive request = new Primitive(ImpsTags.GetPresence_Request);
        for (ImpsAddress addr : addresses) {
            request.addElement(addr.toPrimitiveElement());
        }
        AsyncTransaction tx = new AsyncTransaction(mTransactionManager){
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                ImpsLog.logError("Failed to get presence:" + error.toString());
            }

            @Override
            public void onResponseOk(Primitive response) {
                extractAndNotifyPresence(response.getContentElement());
            }
        };
        tx.sendRequest(request);
    }

    public ImpsAddress[] getAllListAddress() {
        int count = mContactLists.size();
        ImpsAddress[] res = new ImpsContactListAddress[count];

        int index = 0;
        for (ContactList l : mContactLists) {
            res[index++] = (ImpsContactListAddress) l.getAddress();
        }

        return res;
    }

//    void createDefaultAttributeListAsync() {
//        Primitive request = new Primitive(ImpsTags.CreateAttributeList_Request);
//
//        PrimitiveElement presenceList = request.addElement(ImpsTags.PresenceSubList);
//        presenceList.setAttribute(ImpsTags.XMLNS, mConnection.getConfig().getPresenceNs());
//
//        for (String tagName : ImpsClientCapability.getSupportedPresenceAttribs()) {
//            presenceList.addChild(tagName);
//        }
//
//        request.addElement(ImpsTags.DefaultList, true);
//
//        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {
//            @Override
//            public void onResponseError(ImpsErrorInfo error) {
//                // don't notify the 501 not implemented error
//                if(error.getCode() != ImpsConstants.STATUS_NOT_IMPLEMENTED) {
//                    // TODO: not ERROR_RETRIEVING_PRESENCE exactly here...
//                    notifyContactError(
//                            ContactListListener.ERROR_RETRIEVING_PRESENCE,
//                            error, null, null);
//                }
//            }
//
//            @Override
//            public void onResponseOk(Primitive response) {}
//        };
//
//        tx.sendRequest(request);
//    }

    @Override
    public void approveSubscriptionRequest(String contact) {
        handleSubscriptionRequest(contact, true);
    }

    @Override
    public void declineSubscriptionRequest(String contact) {
        handleSubscriptionRequest(contact, false);
    }

    private void handleSubscriptionRequest(final String contact, final boolean accept) {

        Primitive request = new Primitive(ImpsTags.PresenceAuthUser);
        request.addElement(ImpsTags.UserID, contact);
        request.addElement(ImpsTags.Acceptance, accept);
        AsyncTransaction tx = new AsyncTransaction(mTransactionManager){
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                SubscriptionRequestListener listener = getSubscriptionRequestListener();
                if (listener != null) {
                    if (accept) {
                        listener.onApproveSubScriptionError(contact, error);
                    } else {
                        listener.onDeclineSubScriptionError(contact, error);
                    }
                }
            }

            @Override
            public void onResponseOk(Primitive response) {
                SubscriptionRequestListener listener = getSubscriptionRequestListener();
                if (listener != null) {
                    if (accept) {
                        listener.onSubscriptionApproved(contact);
                    } else {
                        listener.onSubscriptionDeclined(contact);
                    }
                }
            }
        };
        tx.sendRequest(request);
    }

    void subscribeToAllListAsync() {
        AsyncCompletion completion = new AsyncCompletion(){
            public void onComplete() {
                // do nothing
            }
            public void onError(ImErrorInfo error) {
                notifyContactError(ContactListListener.ERROR_RETRIEVING_PRESENCE,
                        error, null, null);
            }
        };
        subscribeToListsAsync(mContactLists,completion);
    }

    void subscribeToListAsync(final ContactList list, final AsyncCompletion completion) {
        Vector<ContactList> lists = new Vector<ContactList>();

        lists.add(list);

        subscribeToListsAsync(lists, completion);
    }

    void subscribeToListsAsync(final Vector<ContactList> contactLists,
            final AsyncCompletion completion) {
        if (contactLists.isEmpty()) {
            return;
        }

        Primitive request = buildSubscribeToListsRequest(contactLists);

        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                if (error.getCode()
                        == ImpsConstants.STATUS_AUTO_SUBSCRIPTION_NOT_SUPPORTED) {
                    mAllowAutoSubscribe = false;
                    ArrayList<Contact> contacts = new ArrayList<Contact>();
                    for (ContactList list : contactLists) {
                        contacts.addAll(list.getContacts());
                    }

                    subscribeToContactsAsync(contacts, completion);
                } else {
                    if (completion != null) {
                        completion.onError(error);
                    }
                }
            }

            @Override
            public void onResponseOk(Primitive response) {
                if (completion != null) {
                    completion.onComplete();
                }
            }

        };

        tx.sendRequest(request);
    }

    void subscribeToContactsAsync(ArrayList<Contact> contacts, AsyncCompletion completion) {
        Primitive request = buildSubscribeToContactsRequest(contacts);

        SimpleAsyncTransaction tx = new SimpleAsyncTransaction(mTransactionManager, completion);

        tx.sendRequest(request);
    }

    void unsubscribeToListAsync(ContactList list, AsyncCompletion completion) {
        Primitive request = new Primitive(ImpsTags.UnsubscribePresence_Request);
        request.addElement(ImpsTags.ContactList, list.getAddress().getFullName());

        SimpleAsyncTransaction tx = new SimpleAsyncTransaction(
                mTransactionManager, completion);

        tx.sendRequest(request);
    }

    void unsubscribeToContactAsync(Contact contact, AsyncCompletion completion) {
        Primitive request = new Primitive(ImpsTags.UnsubscribePresence_Request);
        request.addElement(ImpsTags.User).addPropertyChild(ImpsTags.UserID,
                contact.getAddress().getFullName());

        SimpleAsyncTransaction tx = new SimpleAsyncTransaction(
                mTransactionManager, completion);

        tx.sendRequest(request);
    }

    private Primitive buildSubscribeToContactsRequest(ArrayList<Contact> contacts) {
        ArrayList<ImpsAddress> addresses = new ArrayList<ImpsAddress>();

        for (Contact contact : contacts) {
            addresses.add((ImpsAddress)contact.getAddress());
        }

        Primitive request = buildSubscribePresenceRequest(addresses);
        return request;
    }

    @Override
    protected void doCreateContactListAsync(final String name,
            Collection<Contact> contacts,
            final boolean isDefault) {
        ImpsAddress selfAddress = mConnection.getSession().getLoginUserAddress();
        ImpsAddress listAddress = new ImpsContactListAddress(selfAddress, name);

        final ContactList list = new ContactList(listAddress, name,
                isDefault, contacts, this);

        Primitive createListRequest = buildCreateListReq(name, contacts,
                isDefault, listAddress);

        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {

            @Override
            public void onResponseError(ImpsErrorInfo error) {
                notifyContactError(ContactListListener.ERROR_CREATING_LIST,
                        error, name, null);
            }

            @Override
            public void onResponseOk(Primitive response) {
                notifyContactListCreated(list);

                if (mConfig.usePrensencePolling()) {
                    getPresencePollingManager().resetPollingContacts();
                } else {
                    subscribeToListAsync(list, null);
                }
            }
        };

        tx.sendRequest(createListRequest);
    }

    private Primitive buildCreateListReq(String name,
            Collection<Contact> contacts, boolean isDefault,
            ImpsAddress listAddress) {
        Primitive createListRequest = new Primitive(ImpsTags.CreateList_Request);
        createListRequest.addElement(listAddress.toPrimitiveElement());

        // add initial contacts, if any
        if (null != contacts && !contacts.isEmpty()) {
            PrimitiveElement nickList = createListRequest.addElement(ImpsTags.NickList);

            for (Contact contact : contacts) {
                nickList.addChild(buildNickNameElem(contact));
            }
        }

        PrimitiveElement contactListProp = createListRequest.addElement(
                ImpsTags.ContactListProperties);

        contactListProp.addPropertyChild(ImpsConstants.DisplayName, name);
        contactListProp.addPropertyChild(ImpsConstants.Default,
                ImpsUtils.toImpsBool(isDefault));

        return createListRequest;
    }

    /**
     * Delete a specified contact list asyncLoginWrapper.
     *
     * @param list the contact list to be deleted
     */
    @Override
    public void doDeleteContactListAsync(final ContactList list) {
        Primitive delListRequest = buildDelListReq(list);

        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {

            @Override
            public void onResponseError(ImpsErrorInfo error) {
                notifyContactError(ContactListListener.ERROR_DELETING_LIST,
                        error, list.getName(), null);
            }

            @Override
            public void onResponseOk(Primitive response) {
                notifyContactListDeleted(list);
                if (mConfig.usePrensencePolling()) {
                    getPresencePollingManager().resetPollingContacts();
                } else if (!mAllowAutoSubscribe) {
                    unsubscribeToListAsync(list, new AsyncCompletion(){
                        public void onComplete() {}

                        public void onError(ImErrorInfo error) {
                            // don't bother to alert this error since the
                            // list has already been removed.
                            ImpsLog.log("Warning: unsubscribing list presence failed");
                        }
                    });
                }
            }
        };

        tx.sendRequest(delListRequest);
    }

    private Primitive buildDelListReq(final ContactList list) {
        Primitive delListRequest = new Primitive(ImpsTags.DeleteList_Request);
        delListRequest.addElement(((ImpsAddress)list.getAddress())
                .toPrimitiveElement());
        return delListRequest;
    }

    private Primitive buildListManageRequest(ContactList list, Collection<Contact> contactsToAdd,
            Collection<Contact> contactsToRemove, String listName) {
        // Create ListManage request
        Primitive req = new Primitive(ImpsTags.ListManage_Request);
        req.addElement(((ImpsAddress)list.getAddress()).toPrimitiveElement());
        req.addElement(ImpsTags.ReceiveList, false);

        // If there are any pending added contacts, add them to the addNickList
        if (contactsToAdd != null && !contactsToAdd.isEmpty()) {
            PrimitiveElement addList = req.addElement(ImpsTags.AddNickList);

            for (Contact c : contactsToAdd) {
                PrimitiveElement nickNameElem = addList.addChild(ImpsTags.NickName);
                nickNameElem.addChild(ImpsTags.Name, c.getName());
                nickNameElem.addChild(ImpsTags.UserID, c.getAddress().getFullName());
            }
        }

        // If there are any pending removed contacts, add them to the removeNickList
        if (contactsToRemove != null && !contactsToRemove.isEmpty()) {
            PrimitiveElement removeList = req.addElement(ImpsTags.RemoveNickList);

            for (Contact c : contactsToRemove) {
                removeList.addChild(ImpsTags.UserID, c.getAddress().getFullName());
            }
        }

        // Add the list properties
        if (listName != null) {
            PrimitiveElement requestProps = req.addElement(ImpsTags.ContactListProperties);
            requestProps.addPropertyChild(ImpsConstants.DisplayName, listName);
        }

        return req;
    }

    private Primitive buildSubscribeToListsRequest(Collection<ContactList> lists) {
        ArrayList<ImpsAddress> addresses = new ArrayList<ImpsAddress>();

        for (ContactList list : lists) {
            addresses.add((ImpsAddress)list.getAddress());
        }

        Primitive subscribePresenceRequest = buildSubscribePresenceRequest(addresses);

        subscribePresenceRequest.addElement(ImpsTags.AutoSubscribe, true);

        return subscribePresenceRequest;
    }

    private Primitive buildSubscribePresenceRequest(ArrayList<ImpsAddress> addresses) {
        Primitive request = new Primitive(ImpsTags.SubscribePresence_Request);

        // XXX: Workaround on OZ IMPS GTalk server which only supports a few
        // basic presence attributes. The PresenceSubList is optional and an
        // empty List or missing list indicates all available presence
        // attributes are desired but the OZ server doens't quite follow the
        // spec here. It won't send any PresenceNotification either when we
        // don't send PresenceSubList or we request more PA than it supports.
        if(mConfig.supportBasicPresenceOnly()){
            PrimitiveElement presenceList = request.addElement(ImpsTags.PresenceSubList);
            presenceList.setAttribute(ImpsTags.XMLNS, mConfig.getPresenceNs());
            for(String pa : ImpsClientCapability.getBasicPresenceAttributes()) {
                presenceList.addChild(pa);
            }
        }

        for (ImpsAddress address : addresses) {
            request.addElement(address.toPrimitiveElement());
        }

        return request;
    }

    public void notifyServerTransaction(ServerTransaction tx) {
        Primitive request = tx.getRequest();
        String type = request.getType();
        if (ImpsTags.PresenceNotification_Request.equals(type)) {
            tx.sendStatusResponse(ImpsConstants.SUCCESS_CODE);

            PrimitiveElement content = request.getContentElement();
            extractAndNotifyPresence(content);
        } else if (ImpsTags.PresenceAuth_Request.equals(type)) {
            tx.sendStatusResponse(ImpsConstants.SUCCESS_CODE);

            String userId = request.getElementContents(ImpsTags.UserID);
            Contact contact = getContact(userId);
            if (contact == null) {
                ImpsAddress address = new ImpsUserAddress(userId);
                contact = new Contact(address, address.getScreenName());
            }
            if (getState() < LISTS_LOADED) {
                if (mSubscriptionRequests == null) {
                    mSubscriptionRequests = new ArrayList<Contact>();
                }
                mSubscriptionRequests.add(contact);
            } else {
                SubscriptionRequestListener listener = getSubscriptionRequestListener();
                if (listener != null) {
                    listener.onSubScriptionRequest(contact);
                }
            }
        }
    }

    private void extractAndNotifyPresence(PrimitiveElement content) {
        ArrayList<Contact> updated = new ArrayList<Contact>();
        PresenceMapping presenceMapping = mConfig.getPresenceMapping();

        ArrayList<PrimitiveElement> presenceList = content.getChildren(ImpsTags.Presence);
        for (PrimitiveElement presenceElem : presenceList) {
            String userId = presenceElem.getChildContents(ImpsTags.UserID);
            if (userId == null) {
                continue;
            }
            PrimitiveElement presenceSubList = presenceElem.getChild(ImpsTags.PresenceSubList);
            Presence presence = ImpsPresenceUtils.extractPresence(presenceSubList, presenceMapping);
            // Find out the contact in all lists and update their presence
            for(ContactList list : mContactLists) {
                Contact contact = list.getContact(userId);
                if (contact != null) {
                    contact.setPresence(presence);
                    updated.add(contact);
                }
            }
        }
        if (!updated.isEmpty()) {
            notifyContactsPresenceUpdated(updated.toArray(new Contact[updated.size()]));
        }
    }

    void loadContactsOfListAsync(final ImpsAddress address, final
            AsyncCompletion completion) {
        Primitive listManageRequest = new Primitive(ImpsTags.ListManage_Request);

        listManageRequest.addElement(address.toPrimitiveElement());
        listManageRequest.addElement(ImpsTags.ReceiveList, true);

        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                completion.onError(error);
            }

            @Override
            public void onResponseOk(Primitive response) {
                final ContactList list = extractContactList(response, address);

                mContactLists.add(list);
                if (list.isDefault()) {
                    mDefaultContactList = list;
                }

                if (mConfig.usePrensencePolling()) {
                    completion.onComplete();
                } else {
                    subscribeToListAsync(list, completion);
                }
            }
        };

        tx.sendRequest(listManageRequest);
    }

    private Primitive buildBlockContactReq(String address, boolean block) {
        Primitive request = new Primitive(ImpsTags.BlockEntity_Request);
        ImpsVersion version = mConfig.getImpsVersion();

        if (version == ImpsVersion.IMPS_VERSION_13) {
            request.addElement(ImpsTags.BlockListInUse, true);
            request.addElement(ImpsTags.GrantListInUse, false);
        }

        PrimitiveElement blockList = request.addElement(ImpsTags.BlockList);
        if (version != ImpsVersion.IMPS_VERSION_13) {
            blockList.addChild(ImpsTags.InUse, true);
        }
        PrimitiveElement entityList = blockList.addChild(block ?
                ImpsTags.AddList : ImpsTags.RemoveList);
        entityList.addChild(ImpsTags.UserID, address);
        return request;
    }

    @Override
    protected void doBlockContactAsync(String address, final boolean block) {
        Primitive request = buildBlockContactReq(address, block);
        final Address contactAddress = new ImpsUserAddress(address);
        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {

            @Override
            public void onResponseError(ImpsErrorInfo error) {
                Contact c = getContact(contactAddress);
                if(c == null) {
                    c = new Contact(contactAddress, contactAddress.getScreenName());
                }
                notifyContactError(
                        block ? ContactListListener.ERROR_BLOCKING_CONTACT
                                : ContactListListener.ERROR_UNBLOCKING_CONTACT,
                        error, null, c);
            }

            @Override
            public void onResponseOk(Primitive response) {
                Contact c = getContact(contactAddress);
                if(c == null) {
                    c = new Contact(contactAddress, contactAddress.getScreenName());
                }
                notifyBlockContact(c, block);
            }
        };
        tx.sendRequest(request);
    }

    void extractBlockedContacts(Primitive response) {
        mBlockedList.clear();
        PrimitiveElement blockList = response.getElement(ImpsTags.BlockList);
        if(blockList == null) {
            return;
        }
        PrimitiveElement entityList = blockList.getChild(ImpsTags.EntityList);
        if(entityList == null) {
            return;
        }
        for (PrimitiveElement entity : entityList.getChildren()) {
            if (ImpsTags.UserID.equals(entity.getTagName())) {
                String userId = entity.getContents();
                if (userId == null || userId.length() == 0) {
                    ImpsLog.logError("Empty UserID in BlockList");
                    continue;
                }
                ImpsAddress userAddress = new ImpsUserAddress(entity.getContents());
                notifyBlockContact(new Contact(userAddress, userAddress.getScreenName()),
                        true);
            }
        }
    }

    /**
     * Generate NickName element for a specific contact.
     *
     * @param contact the contact which provides the info of the NickName elem
     * @return
     */
    private PrimitiveElement buildNickNameElem(Contact contact) {
        PrimitiveElement nickName = new PrimitiveElement(ImpsTags.NickName);

        nickName.addChild(ImpsTags.Name, contact.getName());
        nickName.addChild(((ImpsAddress)contact.getAddress()).toPrimitiveElement());

        return nickName;
    }

    ContactList extractContactList(Primitive response, final ImpsAddress address) {
        String screenName = address.getScreenName();
        boolean isDefault = false;
        PrimitiveElement propertyElem = response.getElement(ImpsTags.ContactListProperties);
        if (null != propertyElem) {
            for (PrimitiveElement elem : propertyElem.getChildren()) {
                if (elem.getTagName().equals(ImpsTags.Property)) {
                    String name = elem.getChildContents(ImpsTags.Name);
                    String value = elem.getChildContents(ImpsTags.Value);

                    if (name.equals(ImpsConstants.DisplayName)) {
                        screenName = value;
                    } else if (name.equals(ImpsTags.Default)) {
                        isDefault = ImpsUtils.isTrue(value);
                    }
                }
            }
        }

        PrimitiveElement nickListElem = response.getElement(ImpsTags.NickList);
        if (null == nickListElem) {
            return new ContactList(address, screenName, isDefault, null, this);
        }

        Vector<Contact> contacts = new Vector<Contact>();
        for (PrimitiveElement elem : nickListElem.getChildren()) {
            String id = null;
            String name = null;

            String tag = elem.getTagName();
            if (tag.equals(ImpsTags.NickName)) {
                id = elem.getChild(ImpsTags.UserID).getContents();
                name = elem.getChild(ImpsTags.Name).getContents();
            } else if (tag.equals(ImpsTags.UserID)){
                id = elem.getContents();
            }

            if (id != null) {
                Address contactAddress = new ImpsUserAddress(id);
                Contact c = getContact(contactAddress);
                if (c == null) {
                    if (name == null) {
                        name = contactAddress.getScreenName();
                    }
                    c = new Contact(contactAddress, name);
                }
                contacts.add(c);
            }
        }
        return new ContactList(address, screenName, isDefault, contacts, this);
    }

    private class LoadContactsTransaction extends AsyncTransaction {
        Vector<ImpsContactListAddress> mListAddresses;

        LoadContactsTransaction() {
            super(mTransactionManager);

            mListAddresses = new Vector<ImpsContactListAddress>();
        }

        @Override
        public void onResponseError(ImpsErrorInfo error) {
            notifyContactError(ContactListListener.ERROR_LOADING_LIST,
                    error, null, null);
        }

        @Override
        public void onResponseOk(Primitive response) {
            mContactLists.clear();

            mListAddresses = extractListAddresses(response);

            if (!mListAddresses.isEmpty()) {
                fetchContacts();
            } else {
                onContactListsLoaded();
            }
        }

        void startGetContactLists() {
            Primitive getListRequest = new Primitive(ImpsTags.GetList_Request);

            sendRequest(getListRequest);
        }

        private void fetchContacts() {
            ImpsContactListAddress fisrtListAddress = mListAddresses.firstElement();

            loadContactsOfListAsync(fisrtListAddress, new LoadListCompletion());
        }

        private final class LoadListCompletion implements AsyncCompletion {
            private int mListIndex;
            LoadListCompletion() {
                mListIndex = 0;
            }

            public void onComplete() {
                processResult(null);
            }

            public void onError(ImErrorInfo error) {
                processResult(error);
            }

            private void processResult(ImErrorInfo error) {
                ImpsAddress addr = mListAddresses.get(mListIndex);

                if (error == null) {
                    notifyContactListLoaded(getContactList(addr));
                } else {
                    notifyContactError(ContactListListener.ERROR_LOADING_LIST,
                            error, addr.getScreenName(), null);
                }

                mListIndex++;
                if (mListIndex < mListAddresses.size()) {
                    loadContactsOfListAsync(mListAddresses.get(mListIndex), this);
                } else {
                    onContactListsLoaded();
                }
            }
        }
    }

    void onContactListsLoaded() {
        notifyContactListsLoaded();

        if (mConfig.usePrensencePolling()) {
            fetchPresence(getAllListAddress());
        }

        // notify the pending subscription requests received before contact
        // lists has been loaded.
        SubscriptionRequestListener listener = getSubscriptionRequestListener();
        if (mSubscriptionRequests != null && listener != null) {
            for (Contact c : mSubscriptionRequests) {
                listener.onSubScriptionRequest(c);
            }
        }
        ((ImpsChatSessionManager) mConnection.getChatSessionManager()).start();
    }

    @Override
    protected void doAddContactToListAsync(String addressStr, ContactList list)
        throws ImException {
        ImpsUserAddress address = new ImpsUserAddress(addressStr);

        Contact contact;
        if (getContact(address) != null) {
            contact = getContact(address);
        } else {
            contact = new Contact(address, address.getScreenName());
        }

        if (isBlocked(contact)) {
            throw new ImException(ImErrorInfo.CANT_ADD_BLOCKED_CONTACT,
            "Contact has been blocked");
        }

        addContactToListAsync(contact, list);
    }

    private void addContactToListAsync(final Contact contact,
            final ContactList list) {
        final ArrayList<Contact> contacts = new ArrayList<Contact>();

        contacts.add(contact);
        updateContactListAsync(list, contacts, null, null, new AsyncCompletion(){
            public void onComplete() {
                notifyContactListUpdated(list,
                        ContactListListener.LIST_CONTACT_ADDED, contact);

                if (mConfig.usePrensencePolling()) {
                    fetchPresence(new ImpsAddress[]{
                            (ImpsAddress) contact.getAddress()});
                } else {
                    AsyncCompletion subscribeCompletion =  new AsyncCompletion(){
                        public void onComplete() {}

                        public void onError(ImErrorInfo error) {
                            notifyContactError(
                                    ContactListListener.ERROR_RETRIEVING_PRESENCE,
                                    error, list.getName(), contact);
                        }
                    };

                    if (mAllowAutoSubscribe) {
                        // XXX Send subscription again after add contact to make sure we
                        // can get the presence notification. Although the we set
                        // AutoSubscribe True when subscribe presence after load contacts,
                        // the server might not send presence notification.
                        subscribeToListAsync(list, subscribeCompletion);
                    } else {
                        subscribeToContactsAsync(contacts, subscribeCompletion);
                    }
                }
            }

            public void onError(ImErrorInfo error) {
                // XXX Workaround to convert 402 error to 531. Some
                // servers might return 402 - Bad parameter instead of
                // 531 - Unknown user if the user input an invalid user ID.
                if (error.getCode() == ImpsErrorInfo.BAD_PARAMETER) {
                    error = new ImErrorInfo(ImpsErrorInfo.UNKNOWN_USER,
                            error.getDescription());
                }
                notifyContactError(ContactListListener.ERROR_ADDING_CONTACT,
                        error, list.getName(), contact);
            }
        });
    }

    @Override
    protected void doRemoveContactFromListAsync(final Contact contact, final ContactList list) {
        ArrayList<Contact> contacts = new ArrayList<Contact>();

        contacts.add(contact);
        updateContactListAsync(list, null, contacts, null, new AsyncCompletion(){
            public void onComplete() {
                ImpsLog.log("removed contact");
                notifyContactListUpdated(list,
                        ContactListListener.LIST_CONTACT_REMOVED, contact);

                if (!mAllowAutoSubscribe) {
                    unsubscribeToContactAsync(contact, new AsyncCompletion(){
                        public void onComplete() {}

                        public void onError(ImErrorInfo error) {
                            // don't bother to alert this error since the
                            // contact has already been removed.
                            ImpsLog.log("Warning: unsubscribing contact presence failed");
                        }
                    });
                }
            }

            public void onError(ImErrorInfo error) {
                ImpsLog.log("remove contact error:" + error);
                notifyContactError(ContactListListener.ERROR_REMOVING_CONTACT,
                        error, list.getName(), contact);
            }
        });
    }

    @Override
    protected void setListNameAsync(final String name, final ContactList list) {
        updateContactListAsync(list, null, null, name, new AsyncCompletion(){
            public void onComplete() {
                notifyContactListNameUpdated(list, name);
            }

            public void onError(ImErrorInfo error) {
                notifyContactError(ContactListListener.ERROR_RENAMING_LIST,
                        error, list.getName(), null);
            }
        });
    }

    private void updateContactListAsync(final ContactList list, final ArrayList<Contact>
            contactsToAdd, final ArrayList<Contact> contactsToRemove,
            final String listName, AsyncCompletion completion) {
        Primitive request = buildListManageRequest(list, contactsToAdd,
                contactsToRemove, listName);

        SimpleAsyncTransaction tx = new SimpleAsyncTransaction(mTransactionManager, completion);
        tx.sendRequest(request);
    }

    String getPropertyValue(String propertyName, PrimitiveElement properties) {
        for (PrimitiveElement property : properties.getChildren(ImpsTags.Property)) {
            if (propertyName.equals(property.getChildContents(ImpsTags.Name))) {
                return property.getChildContents(ImpsTags.Value);
            }
        }

        return null;
    }

    void reset() {
        setState(LISTS_NOT_LOADED);
    }

    @Override
    protected ImConnection getConnection() {
        return mConnection;
    }

    private PresencePollingManager mPollingMgr;
    /*package*/PresencePollingManager getPresencePollingManager() {
        if (mPollingMgr == null) {
            mPollingMgr = new PresencePollingManager(this,
                    mConfig.getPresencePollInterval());
        }
        return mPollingMgr;
    }

}
