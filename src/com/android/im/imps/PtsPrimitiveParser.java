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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.im.imps.Primitive.TransactionMode;

/**
 * PTS/SMS encoded IMPS messages parser. Only response transactions and
 * server initiated requests are supported.
 */
public class PtsPrimitiveParser implements PrimitiveParser {

    // WVaaBBcccDD <parameters>
    //   aa - version number; 12 for 1.2, 13 for 1.3; "XX" for version discovery
    //   BB - message type, case insensitive
    //   ccc - transaction id in range 0-999 without preceding zero
    //   DD - multiple SMSes identifier
    private static final Pattern sPreamplePattern =
        Pattern.compile("\\AWV(\\d{2})(\\p{Alpha}{2})(\\d{1,3})(\\p{Alpha}{2})?(\\z| .*)");

    private char mReadBuf[] = new char[256];
    private StringBuilder mStringBuf = new StringBuilder();
    private int mPos;

    private static int UNCERTAIN_GROUP_SIZE = -1;

    public Primitive parse(InputStream in) throws ParserException, IOException {
        // assuming PTS data is always short
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, "UTF-8"), 128);
        mStringBuf.setLength(0);
        mPos = 0;
        int len;
        while ((len = reader.read(mReadBuf)) != -1) {
            mStringBuf.append(mReadBuf, 0, len);
        }
        return parsePrim();
    }

    private Primitive parsePrim() throws ParserException
    {
        Matcher m = sPreamplePattern.matcher(mStringBuf);
        if (!m.matches()) {
            throw new ParserException("Invalid PTS encoded message");
        }

        Primitive p = new Primitive();

        // TODO: handle WV version in m.group(1)

        String type = m.group(2).toUpperCase();
        String transactionType = PtsCodes.getTransaction(type);
        if (transactionType == null) {
            throw new ParserException("Unrecognized transaction code " + type);
        }
        p.setContentElement(transactionType);

        if (PtsCodes.isServerRequestCode(type)) {
            p.setTransactionMode(TransactionMode.Request);
        } else {
            p.setTransactionMode(TransactionMode.Response);
        }

        p.setTransactionId(m.group(3));
        mPos = m.start(5);

        if (mPos < mStringBuf.length()) {
            match(' ');

            HashMap<String, ParamValue> params = parseParams();
            for (Entry<String, ParamValue> param : params.entrySet()) {
                translateParam(p, param.getKey(), param.getValue());
            }
        }
        return p;
    }

    private static HashMap<String, Integer> sInfoElemTypeMap;
    private static final int ELEM_OTHER_SIMPLE         = 0;
    private static final int ELEM_SESSION_ID           = 1;
    private static final int ELEM_RESULT               = 2;
    private static final int ELEM_ALL_FUNCTIONS        = 3;
    private static final int ELEM_NOT_AVAIL_FUNCS      = 4;
    private static final int ELEM_CAPABILITY_LIST      = 5;
    private static final int ELEM_CONTACT_LIST         = 6;
    private static final int ELEM_DEFAULT_CONTACT_LIST = 7;
    private static final int ELEM_USER_NICK_LIST       = 8;
    private static final int ELEM_CONTACT_LIST_PROPS   = 9;
    private static final int ELEM_PRESENCE             = 10;

    /*
    private static final int ELEM_RESULT_CLIST  = 3;
    private static final int ELEM_RESULT_DOMAIN = 4;
    private static final int ELEM_RESULT_GROUP  = 5;
    private static final int ELEM_RESULT_MSGID  = 6;
    private static final int ELEM_RESULT_SCRNAME = 7;
    private static final int ELEM_RESULT_USER   = 8;
    */

    static {
        sInfoElemTypeMap = new HashMap<String, Integer>();
        sInfoElemTypeMap.put(PtsCodes.SessionID, ELEM_SESSION_ID);
        sInfoElemTypeMap.put(PtsCodes.Status, ELEM_RESULT);
        sInfoElemTypeMap.put(PtsCodes.NotAvailableFunctions, ELEM_NOT_AVAIL_FUNCS);
        sInfoElemTypeMap.put(PtsCodes.AllFunctions, ELEM_ALL_FUNCTIONS);
        sInfoElemTypeMap.put(PtsCodes.AgreedCapabilityList, ELEM_CAPABILITY_LIST);
        sInfoElemTypeMap.put(PtsCodes.ContactList, ELEM_CONTACT_LIST);
        sInfoElemTypeMap.put(PtsCodes.DefaultContactList, ELEM_DEFAULT_CONTACT_LIST);
        sInfoElemTypeMap.put(PtsCodes.UserNickList, ELEM_USER_NICK_LIST);
        sInfoElemTypeMap.put(PtsCodes.ContactListProps, ELEM_CONTACT_LIST_PROPS);
        sInfoElemTypeMap.put(PtsCodes.Presence, ELEM_PRESENCE);
    }

    private static void translateParam(Primitive p, String elemCode,
            ParamValue elemValue) throws ParserException {
        int type;
        elemCode = elemCode.toUpperCase();

        // FIXME: Should be refactored when we had concrete situation of the null value case
        if (elemValue == null) {
            throw new ParserException("Parameter " + elemCode + " must have value.");
        }

        if (sInfoElemTypeMap.containsKey(elemCode)) {
            type = sInfoElemTypeMap.get(elemCode);
            /*
            if (type == ELEM_RESULT_CLIST && p.getType().equals(ImpsTags.Login_Response)) {
                // Fix up DigestSchema which shares a same code with
                // ContactListID. It appears only in Login_Response.
                type = ELEM_OTHER_SIMPLE;
            }
            */
        } else {
            type = ELEM_OTHER_SIMPLE;
        }

        switch (type) {
        case ELEM_SESSION_ID:
            if (elemValue.mStrValue == null) {
                throw new ParserException("Element SessionID must have string value!");
            }

            if (p.getType().equals(ImpsTags.Login_Response)) {
                p.addElement(ImpsTags.SessionID, elemValue.mStrValue);
            } else {
                p.setSession(elemValue.mStrValue);
            }
            break;

        case ELEM_RESULT:
            // ST=<StatusCode>
            // ST=(<StatusCode>,<Description>)
            PrimitiveElement result = p.addElement(ImpsTags.Result);

            if (elemValue.mStrValue != null) {
                result.addChild(ImpsTags.Code, elemValue.mStrValue);
            } else {
                checkGroupValue(elemValue.mValueGroup, 2);

                result.addChild(ImpsTags.Code, elemValue.mValueGroup.get(0).mStrValue);
                result.addChild(ImpsTags.Description, elemValue.mValueGroup.get(1).mStrValue);
            }
            break;

        case ELEM_ALL_FUNCTIONS:
        case ELEM_NOT_AVAIL_FUNCS:
            p.addElement(translateServiceTree(elemCode, elemValue));
            break;

        case ELEM_CAPABILITY_LIST:
            p.addElement(translateCapabilityList(elemValue));
            break;

        case ELEM_CONTACT_LIST:
            if (elemValue.mStrValue != null) {
                p.addElement(ImpsTags.ContactList, elemValue.mStrValue);
            } else {
                checkGroupValue(elemValue.mValueGroup, UNCERTAIN_GROUP_SIZE);
                for (ParamValue value : elemValue.mValueGroup) {
                    p.addElement(ImpsTags.ContactList, value.mStrValue);
                }
            }
            break;

        case ELEM_DEFAULT_CONTACT_LIST:
            if (elemValue.mStrValue == null) {
                throw new ParserException("Deafult Contact List must have string value!");
            }

            p.addElement(ImpsTags.DefaultContactList, elemValue.mStrValue);
            break;

        case ELEM_USER_NICK_LIST:
        {
            checkGroupValue(elemValue.mValueGroup, UNCERTAIN_GROUP_SIZE);

            PrimitiveElement nicklistElem = p.addElement(ImpsTags.NickList);

            int groupSize = elemValue.mValueGroup.size();
            for (int i = 0; i < groupSize; i++) {
                ArrayList<ParamValue> valueGroup = elemValue.mValueGroup.get(i).mValueGroup;
                checkGroupValue(valueGroup, 2);

                String nickname = valueGroup.get(0).mStrValue;
                String address  = valueGroup.get(1).mStrValue;
                if (nickname == null || address == null) {
                    throw new ParserException("Null value found for NickName: " + nickname
                            + "-" + address);
                }

                PrimitiveElement nicknameElem = nicklistElem.addChild(ImpsTags.NickName);
                nicknameElem.addChild(ImpsTags.Name, "".equals(nickname) ? null : nickname);
                nicknameElem.addChild(ImpsTags.UserID, address);
            }
        }
            break;

        case ELEM_CONTACT_LIST_PROPS:
        {
            checkGroupValue(elemValue.mValueGroup, UNCERTAIN_GROUP_SIZE);

            PrimitiveElement propertiesElem = p.addElement(ImpsTags.ContactListProperties);

            int groupSize = elemValue.mValueGroup.size();
            for (int i = 0; i < groupSize; i++) {
                ArrayList<ParamValue> valueGroup = elemValue.mValueGroup.get(i).mValueGroup;
                checkGroupValue(valueGroup, 2);

                String name  = valueGroup.get(0).mStrValue;
                String value = valueGroup.get(1).mStrValue;
                if (name == null || value == null) {
                    throw new ParserException("Null value found for property: " + name + "-" + value);
                }

                if (PtsCodes.DisplayName.equals(name)) {
                    name = ImpsConstants.DisplayName;
                } else if (PtsCodes.Default.equals(name)) {
                    name = ImpsConstants.Default;
                } else {
                    throw new ParserException("Unrecognized property " + name);
                }

                PrimitiveElement propertyElem = propertiesElem.addChild(ImpsTags.Property);
                propertyElem.addChild(ImpsTags.Name, name);
                propertyElem.addChild(ImpsTags.Value, value);
            }
        }
            break;

        case ELEM_PRESENCE:
            //PR=(<UserID>[,<PresenceSubList>])
            //PR=((<UserID>[,<PresenceSubList>]),(<UserID>[,<PresenceSubList>]))
            checkGroupValue(elemValue.mValueGroup, UNCERTAIN_GROUP_SIZE);

            if (elemValue.mValueGroup.size() == 1) {
                // PR=(<UserID>)
                ParamValue value = elemValue.mValueGroup.get(0);
                if (value.mStrValue != null) {
                    p.addElement(ImpsTags.Presence).addChild(ImpsTags.UserID, value.mStrValue);
                } else {
                    // workaround for OZ server
                    p.addElement(translatePresence(value.mValueGroup));
                }

            } else {
                if (elemValue.mValueGroup.get(0).mStrValue == null) {
                    // PR=((<UserID>[,<PresenceSubList>]),(<UserID>[,<PresenceSubList>]))
                    int groupSize = elemValue.mValueGroup.size();
                    for (int i = 0; i < groupSize; i++) {
                        ParamValue value = elemValue.mValueGroup.get(i);
                        if (value.mStrValue != null) {
                            p.addElement(ImpsTags.Presence).addChild(ImpsTags.UserID, value.mStrValue);
                        } else {
                            p.addElement(translatePresence(value.mValueGroup));
                        }
                    }
                } else {
                    // PR=(<UserID>,<PresenceSubList>)
                    p.addElement(translatePresence(elemValue.mValueGroup));
                }
            }
            break;

        case ELEM_OTHER_SIMPLE:
            p.addElement(translateSimpleElem(elemCode, elemValue));
            break;

        default:
            throw new ParserException("Unsupported element " + elemValue);
        }
    }

    private static PrimitiveElement translatePresence(ArrayList<ParamValue> valueGroup)
            throws ParserException {
        checkGroupValue(valueGroup, UNCERTAIN_GROUP_SIZE);

        PrimitiveElement presence = new PrimitiveElement(ImpsTags.Presence);
        if (valueGroup.get(0).mStrValue == null) {
            throw new ParserException("UserID must have string value!");
        }
        presence.addChild(ImpsTags.UserID, valueGroup.get(0).mStrValue);

        if (valueGroup.size() > 1) {
            // has presence sub list
            presence.addChild(translatePresenceSubList(valueGroup.get(1)));
        }

        return presence;
    }

    private static PrimitiveElement translatePresenceSubList(ParamValue value)
            throws ParserException {
        checkGroupValue(value.mValueGroup, UNCERTAIN_GROUP_SIZE);

        PrimitiveElement presenceSubList = new PrimitiveElement(ImpsTags.PresenceSubList);

        int groupSize = value.mValueGroup.size();
        for (int i = 0; i < groupSize; i++) {
            ParamValue v = value.mValueGroup.get(i);
            if (v.mStrValue != null) {
                throw new ParserException("Unexpected string value for presence attribute");
            }

            presenceSubList.addChild(translatePresenceAttribute(v.mValueGroup));
        }

        return presenceSubList;
    }

    // <attribute>[,<qualifier>][,<value>]
    // <attribute>[,<qualifier>,<sub-attribute>]
    private static PrimitiveElement translatePresenceAttribute(
            ArrayList<ParamValue> valueGroup) throws ParserException {
        String type = valueGroup.get(0).mStrValue;
        if (type == null) {
            return null;
        }

        String tag = PtsCodes.getPresenceAttributeElement(type);
        if (tag == null) {
            return null;
        }

        PrimitiveElement paElem = new PrimitiveElement(tag);
        if (valueGroup.size() == 2) {
            // no qualifier
            translateAttributeValue(paElem, valueGroup.get(1), false);
        }else if (valueGroup.size() == 3) {
            // has qualifier, and it should has no group value
            ParamValue qualifierValue = valueGroup.get(1);
            if (qualifierValue.mStrValue == null) {
                throw new ParserException("Qualifier value can't be group value!");
            }

            if (!"".equals(qualifierValue.mStrValue)) {
                paElem.addChild(ImpsTags.Qualifier, qualifierValue.mStrValue);
            }

            translateAttributeValue(paElem, valueGroup.get(2), true);
        } else {
            return null;
        }

        return paElem;
    }

    private static void translateAttributeValue(PrimitiveElement paElem,
            ParamValue v, boolean hasQualifier) throws ParserException {
        if (v.mStrValue == null) {
            // sub-attribute as value
            checkGroupValue(v.mValueGroup, UNCERTAIN_GROUP_SIZE);
            if (v.mValueGroup.get(0).mStrValue != null) {
                paElem.addChild(translatePresenceAttribute(v.mValueGroup));
            } else {
                int groupSize = v.mValueGroup.size();
                for (int i = 0; i < groupSize; i++) {
                    ParamValue value = v.mValueGroup.get(i);
                    if (value.mStrValue != null) {
                        throw new ParserException("Presence Attribute value error!");
                    }

                    checkGroupValue(value.mValueGroup, UNCERTAIN_GROUP_SIZE);
                    paElem.addChild(translatePresenceAttribute(value.mValueGroup));
                }
            }
        } else {
            // single simple value
            if (hasQualifier) {
                paElem.addChild(ImpsTags.PresenceValue, PtsCodes.getPAValue(v.mStrValue));
            } else {
                paElem.setContents(PtsCodes.getPAValue(v.mStrValue));
            }
        }
    }

    private static void checkGroupValue(ArrayList<ParamValue> valueGroup,
            int expectedGroupSize) throws ParserException {
        if (valueGroup == null
                || (expectedGroupSize != UNCERTAIN_GROUP_SIZE
                        && valueGroup.size() != expectedGroupSize)) {
            throw new ParserException("Invalid group value!");
        }

        int groupSize = valueGroup.size();
        for (int i = 0; i < groupSize; i++) {
            if (valueGroup.get(i) == null) {
                throw new ParserException("Invalid group value!");
            }
        }
    }

    private static PrimitiveElement translateCapabilityList(ParamValue elemValue)
            throws ParserException {
        PrimitiveElement elem = new PrimitiveElement(ImpsTags.AgreedCapabilityList);
        ArrayList<ParamValue> params = elemValue.mValueGroup;
        if (params != null) {
            checkGroupValue(params, UNCERTAIN_GROUP_SIZE);
            int paramsSize = params.size();
            for (int i = 0; i < paramsSize; i++) {
                ArrayList<ParamValue> capElemGroup = params.get(i).mValueGroup;
                checkGroupValue(capElemGroup, 2);

                String capElemCode = capElemGroup.get(0).mStrValue;
                String capElemName;
                if (capElemCode == null
                        || (capElemName = PtsCodes.getCapElement(capElemCode)) == null) {
                    throw new ParserException("Unknown capability element "
                            + capElemCode);
                }
                String capElemValue = capElemGroup.get(1).mStrValue;
                if (capElemValue == null) {
                    throw new ParserException("Illegal capability value for "
                            + capElemCode);
                }
                capElemValue = PtsCodes.getCapValue(capElemValue);

                elem.addChild(capElemName, capElemValue);
            }
        }
        return elem;
    }

    private static PrimitiveElement translateServiceTree(String elemCode,
            ParamValue elemValue) throws ParserException {
        String elemName = PtsCodes.getElement(elemCode);
        PrimitiveElement elem = new PrimitiveElement(elemName);
        // TODO: translate the service tree.
        return elem;
    }

    private static PrimitiveElement translateSimpleElem(String elemCode, ParamValue value)
            throws ParserException {
        String elemName = PtsCodes.getElement(elemCode);
        if (elemName == null) {
            throw new ParserException("Unrecognized parameter " + elemCode);
        }

        PrimitiveElement elem = new PrimitiveElement(elemName);
        if (value.mStrValue != null) {
            elem.setContents(value.mStrValue);
        } else {
            throw new ParserException("Don't know how to handle parameters for "
                    + elemName);
        }

        return elem;
    }

    private HashMap<String, ParamValue> parseParams() throws ParserException {
        int pos = mPos;
        StringBuilder buf = mStringBuf;
        int len = buf.length();
        HashMap<String, ParamValue> ret = new HashMap<String, ParamValue>();

        String paramName;
        ParamValue paramValue;

        while (pos < len) {
            int nameStart = pos;
            while (pos < len) {
                char ch = buf.charAt(pos);
                if (ch == ' ' || ch == '=') {
                    break;
                }
                pos++;
            }
            if (nameStart == pos) {
                throw new ParserException("Missing parameter name near " + pos);
            }
            paramName = buf.substring(nameStart, pos);
            if (pos < len && buf.charAt(pos) == '=') {
                pos++;
                mPos = pos;
                paramValue = parseParamValue();
                pos = mPos;
            } else {
                paramValue = null;
            }
            ret.put(paramName, paramValue);

            if (pos < len) {
                // more parameters ahead
                match(' ');
                pos = mPos;
            }
        }

        return ret;
    }

    private ParamValue parseParamValue() throws ParserException {
        int pos = mPos;
        StringBuilder buf = mStringBuf;
        int len = buf.length();

        if (pos == len) {
            throw new ParserException("Missing parameter value near " + pos);
        }
        ParamValue value = new ParamValue();

        char ch = buf.charAt(pos);
        if (ch == '(') {
            // value list
            pos++;
            ArrayList<ParamValue> valueGroup = new ArrayList<ParamValue>();
            while (pos < len) {
                mPos = pos;
                valueGroup.add(parseParamValue());
                pos = mPos;
                if (pos == len) {
                    throw new ParserException("Unexpected parameter end");
                }
                if (buf.charAt(pos) != ',') {
                    break;
                }
                pos++;
            }
            mPos = pos;
            match(')');
            if (valueGroup.isEmpty()) {
                throw new ParserException("Empty value group near " + mPos);
            }
            value.mValueGroup = valueGroup;
        } else {
            // single value
            if (ch == '"') {
                // quoted value
                pos++;
                StringBuilder escapedValue = new StringBuilder();
                boolean quotedEnd = false;
                while (pos < len) {
                    ch = buf.charAt(pos);
                    pos++;
                    if (ch == '"') {
                        if (pos < len && buf.charAt(pos) == '"') {
                            // "doubled" quote
                            pos++;
                        } else {
                            quotedEnd = true;
                            break;
                        }
                    }
                    escapedValue.append(ch);
                }
                if (!quotedEnd) {
                    throw new ParserException("Unexpected quoted parameter end");
                }
                value.mStrValue = escapedValue.toString();
            } else {
                int valueStart = pos;
                while (pos < len) {
                    ch = buf.charAt(pos);
                    if (ch == ',' || ch == ')' || ch == ' ') {
                        break;
                    }
                    if ("\"(=&".indexOf(ch) != -1) {
                        throw new ParserException("Special character " + ch
                                + " must be quoted");
                    }
                    pos++;
                }
                value.mStrValue = buf.substring(valueStart, pos);
            }
            mPos = pos;
        }

        return value;
    }

    private void match(char c) throws ParserException {
        if (mStringBuf.charAt(mPos) != c) {
            throw new ParserException("Expected " + c + " at pos " + mPos);
        }
        mPos++;
    }

    /**
     * Detect if this short message is a PTS encoded WV-primitive.
     */
    public static boolean isPtsPrimitive(CharSequence msg)
    {
        if (msg == null) {
            return false;
        }
        Matcher m = sPreamplePattern.matcher(msg);
        return m.matches();
    }

    static final class ParamValue {
        public String mStrValue;
        public ArrayList<ParamValue> mValueGroup;
    }
}
