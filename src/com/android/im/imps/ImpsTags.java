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

public interface ImpsTags {
    public static final String WV_CSP_Message = "WV-CSP-Message";
    public static final String XMLNS = "xmlns";
    public static final String Session = "Session";
    public static final String SessionDescriptor = "SessionDescriptor";
    public static final String SessionType = "SessionType";
    public static final String SessionID = "SessionID";
    public static final String Transaction = "Transaction";
    public static final String TransactionDescriptor = "TransactionDescriptor";
    public static final String TransactionMode = "TransactionMode";
    public static final String TransactionID = "TransactionID";
    public static final String TransactionContent = "TransactionContent";
    public static final String Poll = "Poll";
    public static final String CIR = "CIR";

    // Fundamental primitives and transactions
    public static final String Status = "Status";
    public static final String Polling_Request = "Polling-Request";
    public static final String Login_Request = "Login-Request";
    public static final String Login_Response = "Login-Response";
    public static final String Logout_Request = "Logout-Request";
    public static final String Disconnect = "Disconnect";
    public static final String KeepAlive_Request = "KeepAlive-Request";
    public static final String KeepAlive_Response = "KeepAlive-Response";
    public static final String Service_Request = "Service-Request";
    public static final String Service_Response = "Service-Response";
    public static final String ClientCapability_Request = "ClientCapability-Request";
    public static final String ClientCapability_Response = "ClientCapability-Response";
//    public static final String GetSPInfo_Request = "GetSPInfo-Request";
//    public static final String GetSPInfo_Response = "GetSPInfo-Response";
//    public static final String GetSegment_Request = "GetSegment-Request";
//    public static final String GetSegment_Response = "GetSegment-Response";
//    public static final String DropSegment_Request = "DropSegment-Request";

    // Common primitives and transactions
    public static final String SubscribeNotification_Request = "SubscribeNotification-Request";
    public static final String UnsubscribeNotification_Request = "UnsubscribeNotification-Request";
    public static final String Invite_Request = "Invite-Request";
    public static final String Invite_Response = "Invite-Response";
    public static final String InviteUser_Request = "InviteUser-Request";
    public static final String InviteUser_Response = "InviteUser-Response";
    public static final String CancelInvite_Request = "CancelInvite-Request";
    public static final String CancelInviteUser_Request = "CancelInviteUser-Request";
//    public static final String SystemMessage_Request = "SystemMessage-Request";
//    public static final String SystemMessage_User = "SystemMessage-User";
//    public static final String Notification_Request = "Notification-Request";
//    public static final String GetPublicProfile_Request = "GetPublicProfile-Request";
//    public static final String GetPublicProfile_Response = "GetPublicProfile-Response";
//    public static final String UpdatePublicProfile_Request = "UpdatePublicProfile-Request";
//    public static final String Search_Request = "Search-Request";
//    public static final String Search_Response = "Search-Response";
//    public static final String StopSearch_Request = "StopSearch-Request";
//    public static final String GetMap_Request = "GetMap-Request";
//    public static final String GetMap_Response = "GetMap-Response";
//    public static final String VerifyID_Request = "VerifyID-Request";

    // Presence-related primitives and transactions
    public static final String GetList_Request = "GetList-Request";
    public static final String GetList_Response = "GetList-Response";
    public static final String CreateList_Request = "CreateList-Request";
    public static final String DeleteList_Request = "DeleteList-Request";
    public static final String ListManage_Request = "ListManage-Request";
    public static final String ListManage_Response = "ListManage-Response";
    public static final String CreateAttributeList_Request = "CreateAttributeList-Request";
    public static final String DeleteAttributeList_Request = "DeleteAttributeList-Request";
    public static final String GetAttributeList_Request = "GetAttributeList-Request";
    public static final String GetAttributeList_Response = "GetAttributeList-Response";
    public static final String SubscribePresence_Request = "SubscribePresence-Request";
    public static final String UnsubscribePresence_Request = "UnsubscribePresence-Request";
    public static final String PresenceNotification_Request = "PresenceNotification-Request";
    public static final String GetPresence_Request = "GetPresence-Request";
    public static final String GetPresence_Response = "GetPresence-Response";
    public static final String UpdatePresence_Request = "UpdatePresence-Request";
    public static final String GetBlockedList_Request = "GetBlockedList-Request";
    public static final String GetBlockedList_Response = "GetBlockedList-Response";
    public static final String BlockEntity_Request = "BlockEntity-Request";
    public static final String PresenceAuth_Request = "PresenceAuth-Request";
    public static final String PresenceAuthUser = "PresenceAuth-User";
    public static final String CancelAuth_Request = "CancelAuth-Request";
//  public static final String GetWatcherList_Request = "GetWatcherList-Request";
//  public static final String GetWatcherList_Response = "GetWatcherList-Response";

    // Messaging-related primitives and transactions
    public static final String SendMessage_Request = "SendMessage-Request";
    public static final String SendMessage_Response = "SendMessage-Response";
    public static final String NewMessage = "NewMessage";
    public static final String MessageDelivered = "MessageDelivered";
    public static final String MessageNotification = "MessageNotification";
    public static final String DeliveryReport_Request = "DeliveryReport-Request";
//    public static final String SetDeliveryMethod_Request = "SetDeliveryMethod-Request";
//    public static final String GetMessageList_Request = "GetMessageList-Request";
//    public static final String GetMessageList_Response = "GetMessageList-Response";
//    public static final String RejectMessage_Request = "RejectMessage-Request";
//    public static final String GetMessage_Request = "GetMessage-Request";
//    public static final String GetMessage_Response = "GetMessage-Response";
//    public static final String ForwardMessage_Request = "ForwardMessage-Request";
//    public static final String ForwardMessage_Response = "ForwardMessage-Response";
//    public static final String ExtendConversation_Request = "ExtendConversation-Request";
//    public static final String ExtendConversation_Response = "ExtendConversation-Response";

    // Group-related primitives and transactions
    public static final String CreateGroup_Request = "CreateGroup-Request";
    public static final String DeleteGroup_Request = "DeleteGroup-Request";
    public static final String JoinGroup_Request = "JoinGroup-Request";
    public static final String JoinGroup_Response = "JoinGroup-Response";
    public static final String LeaveGroup_Request = "LeaveGroup-Request";
    public static final String LeaveGroup_Response = "LeaveGroup-Response";
    public static final String GetGroupMembers_Request = "GetGroupMembers-Request";
    public static final String GetGroupMembers_Response = "GetGroupMembers-Response";
    public static final String GetJoinedUsers_Request = "GetJoinedUsers-Request";
    public static final String GetJoinedUsers_Response = "GetJoinedUsers-Response";
    public static final String AddGroupMembers_Request = "AddGroupMembers-Request";
    public static final String RemoveGroupMembers_Request = "RemoveGroupMembers-Request";
    public static final String SubscribeGroupNotice_Request = "SubscribeGroupNotice-Request";
    public static final String SubscribeGroupNotice_Response = "SubscribeGroupNotice-Response";
    public static final String GroupChangeNotice = "GroupChangeNotice";
//    public static final String MemberAccess_Request = "MemberAccess-Request";
//    public static final String GetGroupProps_Request = "GetGroupProps-Request";
//    public static final String GetGroupProps_Response = "GetGroupProps-Response";
//    public static final String SetGroupProps_Request = "SetGroupProps-Request";
//    public static final String RejectList_Request = "RejectList-Request";
//    public static final String RejectList_Response = "RejectList-Response";

    // Extension-related primitives and transactions
//    public static final String Extended_Request = "Extended-Request";
//    public static final String Extended_Response = "Extended-Response";

    public static final String UserID = "UserID";
    public static final String Password = "Password";
    public static final String Nonce = "Nonce";
    public static final String CapabilityRequest = "CapabilityRequest";
    public static final String KeepAliveTime = "KeepAliveTime";
    public static final String Code = "Code";
    public static final String Result = "Result";
    public static final String SessionCookie = "SessionCookie";
    public static final String DigestSchema = "DigestSchema";
    public static final String ClientID = "ClientID";
    public static final String URL = "URL";
    public static final String MSISDN = "MSISDN";
    public static final String TimeToLive = "TimeToLive";
    public static final String DigestBytes = "DigestBytes";
    public static final String Description = "Description";
    public static final String AllFunctionsRequest = "AllFunctionsRequest";
    public static final String AllFunctions = "AllFunctions";
    public static final String NotAvailableFunctions= "NotAvailableFunctions";

    // Presence Attribute
    public static final String PresenceSubList = "PresenceSubList";
    public static final String DefaultList = "DefaultList";
    public static final String DefaultAttributeList = "DefaultAttributeList";

    public static final String OnlineStatus = "OnlineStatus";
    public static final String ClientInfo = "ClientInfo";
    public static final String UserAvailability = "UserAvailability";
    public static final String StatusText = "StatusText";
    public static final String CommCap = "CommCap";
    public static final String CommC = "CommC";
    public static final String CAP = "Cap";

    // TODO: clean up these uncategorized tags
    public static final String DefaultContactList = "DefaultContactList";
    //public static final String ContactListIDList = "ContactListIDList";   // 1.3
    public static final String ContactList = "ContactList";
    public static final String ReceiveList = "ReceiveList";
    public static final String ContactListProperties = "ContactListProperties";
    public static final String Property = "Property";
    public static final String Name = "Name";
    public static final String Value = "Value";
    public static final String AcceptedContentType = "AcceptedContentType";
    public static final String NickList = "NickList";
    public static final String NickName = "NickName";
    public static final String CapabilityList = "CapabilityList";
    public static final String AgreedCapabilityList = "AgreedCapabilityList";
    public static final String ClientType = "ClientType";
    public static final String ParserSize = "ParserSize";
    public static final String MultiTrans = "MultiTrans";
    public static final String MultiTransPerMessage = "MultiTransPerMessage";
    public static final String InitialDeliveryMethod = "InitialDeliveryMethod";
    public static final String ServerPollMin = "ServerPollMin";
    public static final String MessageInfo = "MessageInfo";
    public static final String User = "User";
    public static final String DeliveryReport = "DeliveryReport";
    public static final String ContentSize = "ContentSize";
    public static final String Sender = "Sender";
    public static final String Recipient = "Recipient";
    public static final String ContentData = "ContentData";
    public static final String MessageID = "MessageID";
    public static final String CIRHTTPAddress = "CIRHTTPAddress";
    public static final String DefaultLanguage = "DefaultLanguage";
    public static final String TCPPort = "TCPPort";
    public static final String TCPAddress = "TCPAddress";
    public static final String UDPPort = "UDPPort";
    public static final String SupportedCIRMethod = "SupportedCIRMethod";
    public static final String SupportedBearer = "SupportedBearer";
    public static final String Default = "Default";
    public static final String ContentType = "ContentType";
    public static final String ContentEncoding = "ContentEncoding";
    public static final String DateTime = "DateTime";
    public static final String Font = "Font";
    public static final String Size = "Size";
    public static final String Style = "Style";
    public static final String Color = "Color";
    public static final String Validity = "Validity";
    public static final String Group = "Group";
    public static final String GroupID = "GroupID";
    public static final String AutoDelete = "AutoDelete";
    public static final String Searchable = "Searchable";
    public static final String PrivateMessaging = "PrivateMessaging";
    public static final String Accesstype = "Accesstype";
    public static final String SubscribeNotification = "SubscribeNotification";
    public static final String JoinGroup = "JoinGroup";
    public static final String GroupProperties = "GroupProperties";
    public static final String SName = "SName";
    public static final String Admin = "Admin";
    public static final String Mod = "Mod";
    public static final String UserList = "UserList";
    public static final String UserIDList = "UserIDList";
    public static final String JoinedRequest = "JoinedRequest";
    public static final String ScreenName = "ScreenName";
    public static final String InviteID = "InviteID";
    public static final String InviteType = "InviteType";
    public static final String Joined = "Joined";
    public static final String Left = "Left";
    public static final String UserMapList = "UserMapList";
    public static final String InviteNote = "InviteNote";
    public static final String Acceptance = "Acceptance";
    public static final String AddNickList = "AddNickList";
    public static final String RemoveNickList = "RemoveNickList";
    public static final String Presence = "Presence";
    public static final String Qualifier = "Qualifier";
    public static final String PresenceValue = "PresenceValue";
    public static final String BlockList = "BlockList";
    public static final String InUse = "InUse";
    public static final String EntityList = "EntityList";
    public static final String GrantList = "GrantList";
    public static final String AddList = "AddList";
    public static final String RemoveList = "RemoveList";
    public static final String BlockListInUse = "BlockListInUse";
    public static final String GrantListInUse = "GrantListInUse";
    public static final String ClientProducer = "ClientProducer";
    public static final String ClientVersion = "ClientVersion";
    public static final String AutoSubscribe = "AutoSubscribe";
    public static final String StatusContent = "StatusContent";
    public static final String DirectContent = "DirectContent";
    public static final String ReferredContent = "ReferredContent";
    public static final String AcceptedContentLength = "AcceptedContentLength";
    public static final String Functions = "Functions";
    public static final String WVCSPFeat = "WVCSPFeat";
    public static final String FundamentalFeat = "FundamentalFeat";
    public static final String PresenceFeat = "PresenceFeat";
    public static final String IMFeat = "IMFeat";
    public static final String GroupFeat = "GroupFeat";
    public static final String CIRURL = "CIRURL";
}
