/*
 * Copyright (C) 2007 Esmertec AG.
 * Copyright (C) 2007 The Android Open Source Project
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

import com.android.im.engine.Address;
import com.android.im.engine.ChatGroup;
import com.android.im.engine.ChatGroupManager;
import com.android.im.engine.Contact;
import com.android.im.engine.GroupListener;
import com.android.im.engine.GroupMemberListener;
import com.android.im.engine.Invitation;

/**
 * The implementation of ChatGroupManager with IMPS protocol.
 */
public class ImpsChatGroupManager extends ChatGroupManager implements
        ServerTransactionListener {
    private ImpsConnection mConnection;
    private ImpsTransactionManager mTransactionManager;

    ImpsChatGroupManager(ImpsConnection connection) {
        mConnection = connection;

        mTransactionManager = connection.getTransactionManager();
        mTransactionManager.setTransactionListener(ImpsTags.GroupChangeNotice, this);
        mTransactionManager.setTransactionListener(ImpsTags.LeaveGroup_Response, this);
        mTransactionManager.setTransactionListener(ImpsTags.InviteUser_Request, this);
        mTransactionManager.setTransactionListener(ImpsTags.Invite_Response, this);
    }

    @Override
    protected void addGroupMemberAsync(final ChatGroup group, final Contact contact){
        Primitive request = buildAddGroupMemberRequest(group, contact);

        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                notifyGroupMemberError(group, error);
            }

            @Override
            public void onResponseOk(Primitive response) {
                notifyMemberJoined(group, contact);
            }
        };
        tx.sendRequest(request);
    }

    @Override
    public void createChatGroupAsync(final String name) {
        final ImpsAddress loginUserAddress = mConnection.getSession()
                .getLoginUserAddress();
        final ImpsAddress groupAddress = new ImpsGroupAddress(loginUserAddress, name);

        Primitive primitive = buildCreateGroupRequest(name, groupAddress);

        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                notifyGroupError(GroupListener.ERROR_CREATING_GROUP, name, error);
            }

            @Override
            public void onResponseOk(Primitive response) {
                // The current user is joined into the group automatically.
                ArrayList<Contact> members = new ArrayList<Contact>();
                members.add(new Contact(loginUserAddress, loginUserAddress
                        .getUser()));

                ChatGroup group = new ChatGroup(groupAddress, name, members,
                        ImpsChatGroupManager.this);
                notifyGroupCreated(group);
            }
        };
        tx.sendRequest(primitive);
    }

    @Override
    public void deleteChatGroupAsync(final ChatGroup group) {
        Primitive request = new Primitive(ImpsTags.DeleteGroup_Request);
        request.addElement(ImpsTags.GroupID, group.getAddress().getFullName());

        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                notifyGroupError(GroupListener.ERROR_DELETING_GROUP,
                    group.getName(), error);
            }

            @Override
            public void onResponseOk(Primitive response) {
                notifyGroupDeleted(group);
            }
        };
        tx.sendRequest(request);
    }

    @Override
    public void inviteUserAsync(final ChatGroup group, Contact contact) {
        Primitive request = buildInviteUserRequest(group, contact);

        AsyncTransaction tx = new AsyncTransaction(mTransactionManager){
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                notifyGroupMemberError(group, error);
            }

            @Override
            public void onResponseOk(Primitive response) {
                //Do nothing
            }
        };
        tx.sendRequest(request);
    }

    @Override
    public void acceptInvitationAsync(Invitation invitation){
        joinChatGroupAsync(invitation.getGroupAddress());
        sendInvitationResposne(invitation, true);
    }

    @Override
    public void rejectInvitationAsync(Invitation invitation) {
        sendInvitationResposne(invitation, false);
    }

    private void sendInvitationResposne(Invitation invitation, boolean accept) {
        Primitive response = new Primitive(ImpsTags.InviteUser_Response);
        response.addElement(ImpsTags.InviteID, invitation.getInviteID());
        response.addElement(ImpsTags.Acceptance, ImpsUtils.toImpsBool(accept));
        ImpsAddress sender = (ImpsAddress) invitation.getSender();
        response.addElement(sender.toPrimitiveElement());
        AsyncTransaction tx = new AsyncTransaction(mTransactionManager){
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                // Ignore
            }
            @Override
            public void onResponseOk(Primitive res) {
                // Ignore
            }
        };
        tx.sendRequest(response);
    }

    @Override
    public void joinChatGroupAsync(final Address address) {
        Primitive request = buildJoinGroupRequest(address);

        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                notifyGroupError(GroupListener.ERROR_JOINING_IN_GROUP,
                    address.getScreenName(), error);
            }

            @Override
            public void onResponseOk(Primitive response) {
                ArrayList<Contact> members = new ArrayList<Contact>();
                // FIXME: UserMapList is a child of Joined in version 1.3
                PrimitiveElement userMapping = response
                        .getElement(ImpsTags.UserMapList);
                extractUserMapList(userMapping, members);

                ChatGroup group = new ChatGroup(address, address
                        .getScreenName(), members, ImpsChatGroupManager.this);
                notifyJoinedGroup(group);
            }
        };
        tx.sendRequest(request);
    }

    @Override
    public void leaveChatGroupAsync(final ChatGroup group) {
        Primitive leaveRequest = buildLeaveGroupRequest(group);
        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                notifyGroupError(GroupListener.ERROR_LEAVING_GROUP,
                    group.getName(), error);
            }

            @Override
            public void onResponseOk(Primitive response) {
                notifyLeftGroup(group);
            }
        };
        tx.sendRequest(leaveRequest);
    }

    @Override
    protected void removeGroupMemberAsync(final ChatGroup group, final Contact contact) {
        Primitive request = buildRemoveGroupMemberRequest(group, contact);

        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                notifyGroupMemberError(group, error);
            }

            @Override
            public void onResponseOk(Primitive response) {
                notifyMemberLeft(group, contact);
            }
        };
        tx.sendRequest(request);
    }

    /**
     * Loads the members of a ChatGroup asynchronously. This method will create
     * a ChatGroup and return immediately. After the members are fetched from
     * the server,
     * {@link GroupMemberListener#onMemberJoined(ChatGroup, Contact)}} will be
     * called.
     *
     * @param address
     */
    ChatGroup loadGroupMembersAsync(ImpsGroupAddress address) {
        //FIXME: NowIMP Server doesn't support this primitive.
//        Primitive request = new Primitive(ImpsTags.GetJoinedUsers_Request);
//        request.addElement(ImpsTags.GroupID, address.getFullName());
//        Primitive response = mConnection.sendRequest(request);
//        ImpsUtils.checkResult(response);
//
//        ChatGroup group = new ChatGroup(address, address.getScreenName(), this);
        throw new RuntimeException("Not implemented yet");
    }

    public void notifyServerTransaction(ServerTransaction tx) {
        final Primitive primitive = tx.getRequest();
        String type = primitive.getType();
        if (ImpsTags.GroupChangeNotice.equals(type)) {
            tx.sendStatusResponse(ImpsConstants.SUCCESS_CODE);
            handleGroupChange(primitive);
        } else if (ImpsTags.InviteUser_Request.equals(type)) {
            tx.sendStatusResponse(ImpsConstants.SUCCESS_CODE);

            String inviteType = primitive.getElementContents(ImpsTags.InviteType);
            // We only handle Group invitation right now.
            if (ImpsConstants.GROUP_INVITATION.equals(inviteType)) {
                handleInvitation(primitive);
            }
        } else if (ImpsTags.LeaveGroup_Response.equals(type)) {
            tx.sendStatusResponse(ImpsConstants.SUCCESS_CODE);
            String groupId = primitive.getElementContents(ImpsTags.GroupID);
            ChatGroup group = mGroups.get(new ImpsGroupAddress(groupId));
            if(group != null) {
                notifyLeftGroup(group);
            } else {
                ImpsLog.log("Leave unknown group:" + groupId);
            }
        } else if (ImpsTags.Invite_Response.equals(type)) {
            tx.sendStatusResponse(ImpsConstants.SUCCESS_CODE);
            //TODO: notify the user?
        }
    }

    void handleGroupChange(final Primitive primitive) {
        String groupId = primitive.getElementContents(ImpsTags.GroupID);
        ImpsGroupAddress address = new ImpsGroupAddress(groupId);

        ArrayList<Contact> joined = new ArrayList<Contact>();
        PrimitiveElement joinedElem = primitive.getElement(ImpsTags.Joined);
        if (joinedElem != null) {
            extractUserMapList(joinedElem.getFirstChild(), joined);
        }
        ArrayList<Contact> left = new ArrayList<Contact>();
        PrimitiveElement leftElem = primitive.getElement(ImpsTags.Left);
        if (leftElem != null) {
            extractUserMapList(leftElem.getFirstChild(), left);
        }

        notifyGroupChanged(address, joined, left);
    }

    void handleInvitation(final Primitive primitive) {
        String inviteId = primitive.getElementContents(ImpsTags.InviteID);
        PrimitiveElement sender = primitive.getElement(ImpsTags.Sender);
        ImpsAddress senderAddress = ImpsAddress.fromPrimitiveElement(sender
                .getFirstChild());
        String groupId = primitive.getElementContents(ImpsTags.GroupID);
        Address groupAddress = new ImpsGroupAddress(groupId);
        String inviteNote = primitive.getElementContents(ImpsTags.InviteNote);

        Invitation invitation = new Invitation(inviteId, groupAddress,
                senderAddress, inviteNote);
        notifyGroupInvitation(invitation);
    }

    private Primitive buildAddGroupMemberRequest(final ChatGroup group, final Contact contact) {
        Primitive request = new Primitive(ImpsTags.AddGroupMembers_Request);
        request.addElement(ImpsTags.GroupID, group.getAddress().getFullName());

        // FIXME: It's UserIDList in 1.3
        PrimitiveElement userList = request.addElement(ImpsTags.UserList);
        PrimitiveElement user = userList.addChild(ImpsTags.User);
        user.addChild(ImpsTags.UserID, contact.getAddress().getFullName());
        return request;
    }

    private Primitive buildCreateGroupRequest(String name, ImpsAddress groupAddress) {
        Primitive primitive = new Primitive(ImpsTags.CreateGroup_Request);
        primitive.addElement(ImpsTags.GroupID, groupAddress.getFullName());
        PrimitiveElement propertiesElem = primitive.addElement(ImpsTags.GroupProperties);
        propertiesElem.addPropertyChild(ImpsTags.Name, name);
        propertiesElem.addPropertyChild(ImpsTags.Accesstype, ImpsConstants.Open);
        propertiesElem.addPropertyChild(ImpsTags.PrivateMessaging, false);
        propertiesElem.addPropertyChild(ImpsTags.Searchable, false);
        propertiesElem.addPropertyChild(ImpsTags.AutoDelete, true);

        // TODO: welcome note?
        primitive.addElement(ImpsTags.JoinGroup, true);

        PrimitiveElement screenName = primitive.addElement(ImpsTags.ScreenName);
        screenName.addChild(ImpsTags.SName, mConnection.getLoginUserName());
        screenName.addChild(ImpsTags.GroupID, groupAddress.getFullName());

        primitive.addElement(ImpsTags.SubscribeNotification, true);

        return primitive;
    }

    private Primitive buildInviteUserRequest(ChatGroup group, Contact contact) {
        String inviteId = nextInviteID();
        Primitive request = new Primitive(ImpsTags.Invite_Request);
        request.addElement(ImpsTags.InviteID, inviteId);
        request.addElement(ImpsTags.InviteType, ImpsConstants.GROUP_INVITATION);

        // FIXME: <Sender> is only required in IMPS1.3
        // ImpsGroupAddress groupAddress = (ImpsGroupAddress)group.getAddress();
        // groupAddress.setScreenName(mConnection.getLoginUser());
        // request.addElement(ImpsTags.Sender).addChild(groupAddress.toPrimitiveElement());

        request.addElement(ImpsTags.Recipient).addChild(
                ((ImpsAddress)contact.getAddress()).toPrimitiveElement());
        request.addElement(ImpsTags.GroupID, group.getAddress().getFullName());

        // FIXME: <ScreenName> is only needed in IMPS1.2
        PrimitiveElement screenName = request.addElement(ImpsTags.ScreenName);
        screenName.addChild(ImpsTags.SName, mConnection.getLoginUserName());
        screenName.addChild(ImpsTags.GroupID, group.getAddress().getFullName());
        return request;
    }

    private Primitive buildJoinGroupRequest(Address address) {
        Primitive request = new Primitive(ImpsTags.JoinGroup_Request);
        request.addElement(ImpsTags.GroupID, address.getFullName());
        PrimitiveElement screenName = request.addElement(ImpsTags.ScreenName);
        screenName.addChild(ImpsTags.SName, mConnection.getLoginUserName());
        screenName.addChild(ImpsTags.GroupID, address.getFullName());

        request.addElement(ImpsTags.JoinedRequest, true);
        request.addElement(ImpsTags.SubscribeNotification, true);
        return request;
    }

    private Primitive buildLeaveGroupRequest(ChatGroup group) {
        Primitive leaveRequest = new Primitive(ImpsTags.LeaveGroup_Request);
        leaveRequest.addElement(ImpsTags.GroupID, group.getAddress().getFullName());
        return leaveRequest;
    }

    private Primitive buildRemoveGroupMemberRequest(ChatGroup group, Contact contact) {
        Primitive request = new Primitive(ImpsTags.RemoveGroupMembers_Request);
        request.addElement(ImpsTags.GroupID, group.getAddress().getFullName());

        // FIXME: It's UserIDList in 1.3
        PrimitiveElement userList = request.addElement(ImpsTags.UserList);
        PrimitiveElement user = userList.addChild(ImpsTags.User);
        user.addChild(ImpsTags.UserID, contact.getAddress().getFullName());
        return request;
    }

    private void extractUserMapList(PrimitiveElement userMapList,
            ArrayList<Contact> list) {
        // FIXME: UserMapping is the child of UserMapList in version 1.3
        PrimitiveElement userMapping = userMapList;

        if (userMapping != null) {
            for (PrimitiveElement mapping : userMapping.getChildren()) {
                String name = mapping.getChildContents(ImpsTags.SName);
                String id = mapping.getChildContents(ImpsTags.UserID);
                if (id == null) {
                    id = name;
                }
                list.add((Contact)(new ImpsUserAddress(id)).getEntity(mConnection));
            }
        }
    }
    private static int sInviteID = 0;

    private synchronized String nextInviteID() {
        return "invite" + System.currentTimeMillis() + (sInviteID++);
    }
}
