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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.im.imps.ImpsConstants.ImpsVersion;

public class PtsPrimitiveSerializer implements PrimitiveSerializer {

    private final String mPreampleHead;

    // The ccc is the Transaction-ID in range 0-999 without preceding zero.
    private static final Pattern sTxIdPattern = Pattern.compile("(0|[1-9]\\d{0,2})");

    // If the value of the parameter contains spaces ( ), quotes ("),
    // commas (,),parentheses (()),equal (=) or ampersand (&) characters,
    // it SHALL be wrapped with quotes (").
    private static final Pattern sCharsToBeQuoted = Pattern.compile("[ \",\\(\\)=&]");

    public PtsPrimitiveSerializer(ImpsVersion impsVersion) throws SerializerException {
        if (impsVersion == ImpsVersion.IMPS_VERSION_11) {
            mPreampleHead = "WV11";
        }else if (impsVersion == ImpsVersion.IMPS_VERSION_12) {
            mPreampleHead = "WV12";
        } else if (impsVersion == ImpsVersion.IMPS_VERSION_13) {
            mPreampleHead = "WV13";
        } else {
            throw new SerializerException("Unsupported IMPS version");
        }
    }

    public void serialize(Primitive p, OutputStream out)
            throws IOException, SerializerException {
        String txId = p.getTransactionID();
        if (txId == null) {
            if (!ImpsTags.Polling_Request.equals(p.getType())) {
                throw new SerializerException("null Transaction-ID for non polling request");
            }
            // FIXME: what should this be? Temporarily use 0
            txId = "0";
        } else {
            Matcher m = sTxIdPattern.matcher(txId);
            if (!m.matches()) {
                throw new SerializerException(
                        "Transaction-ID must be in range 0-999 without preceding zero");
            }
        }

        // TODO: use buffered writer?
        Writer writer = new OutputStreamWriter(out, "UTF-8");
        writer.write(mPreampleHead);

        String code = PtsCodes.getTxCode(p.getType());
        if (code == null) {
            throw new SerializerException("Unsupported transaction type "
                    + p.getType());
        }
        writer.write(code);
        writer.write(txId);

        if (p.getSessionId() != null) {
            writer.write(" SI=");
            writer.write(p.getSessionId());
        }

        PrimitiveElement content = p.getContentElement();
        if (content != null && content.getChildCount() > 0) {
            ArrayList<PrimitiveElement> infoElems = content.getChildren();
            ArrayList<String> users = new ArrayList<String>();
            ArrayList<String> lists = new ArrayList<String>();

            int len = infoElems.size();
            for (int i = 0; i < len; i++) {
                PrimitiveElement elem = infoElems.get(i);
                String elemName = elem.getTagName();

                // workaround for multiple elements
                if (ImpsTags.User.equals(elemName)) {
                    users.add(elem.getChildContents(ImpsTags.UserID));
                    continue;
                } else if (ImpsTags.UserID.equals(elemName)) {
                    users.add(elem.getContents());
                    continue;
                } else if (ImpsTags.ContactList.equals(elemName)) {
                    lists.add(elem.getContents());
                    continue;
                }

                String elemCode = PtsCodes.getElementCode(elemName, p.getType());
                if (elemCode == null) {
                    throw new SerializerException("Don't know how to encode element "
                            + elemName);
                }
                writer.write(' ');
                writer.write(elemCode);
                // so far all top level information elements have values.
                writer.write('=');

                String value;
                ElemValueEncoder encoder = ElemValueEncoder.getEncoder(elemName);
                if (encoder == null) {
                    // default simple value
                    value = escapeValueString(elem.getContents());
                } else {
                    value = encoder.encodeValue(p, elem);
                }
                if (value == null) {
                    throw new SerializerException("Empty value for element "
                            + elemName);
                }
                writer.write(value);
            }

            writeMultiValue(writer, PtsCodes.getElementCode(ImpsTags.UserID, p.getType()), users);
            writeMultiValue(writer, PtsCodes.getElementCode(ImpsTags.ContactList, p.getType()), lists);
        }
        writer.close();
    }

    private void writeMultiValue(Writer writer, String code, ArrayList<String> values)
            throws IOException {
        if (values.size() == 0) {
            return;
        }

        writer.write(' ');
        writer.write(code);
        writer.write('=');
        if (values.size() == 1) {
            writer.write(escapeValueString(values.get(0)));
        } else {
            writer.write('(');
            int valueCount = values.size();
            for (int i = 0; i < valueCount; i++) {
                if (i > 0) {
                    writer.write(',');
                }
                writer.write(escapeValueString(values.get(i)));
            }
            writer.write(')');
        }
    }

    static String escapeValueString(String contents) {
        Matcher m = sCharsToBeQuoted.matcher(contents);
        if (m.find()) {
            if (contents.indexOf('"') != -1) {
                contents = contents.replace("\"", "\"\"");
            }
            return "\"" + contents + "\"";
        }
        return contents;
    }

    static void appendPairValue(StringBuilder buf, String first, String second) {
        buf.append('(');
        if (first != null) {
            buf.append(first);
        }
        buf.append(',');
        buf.append(second);
        buf.append(')');
    }

    /**
     * Appends a name and value pair like "(<name>,<value>)".
     */
    static boolean appendNameAndValue(StringBuilder buf, String name, String value,
            HashMap<String, String> nameCodes, HashMap<String, String> valueCodes,
            boolean ignoreUnsupportedValue) {
        String nameCode = nameCodes.get(name);
        if (nameCode == null) {
            ImpsLog.log("PTS: Ignoring value " + name);
            return false;
        }
        String valueCode = null;
        if (valueCodes != null) {
            valueCode = valueCodes.get(value);
        }
        if (valueCode != null) {
            value = valueCode;
        } else {
            if (ignoreUnsupportedValue) {
                return false;
            }

            value = escapeValueString(value);
        }
        appendPairValue(buf, nameCode, value);

        return true;
    }

    static abstract class ElemValueEncoder {
        public abstract String encodeValue(Primitive p, PrimitiveElement elem)
                throws SerializerException;

        public static ElemValueEncoder getEncoder(String elemName) {
            return sEncoders.get(elemName);
        }

        private static HashMap<String, ElemValueEncoder> sEncoders;
        static {
            sEncoders = new HashMap<String, ElemValueEncoder>();

            sEncoders.put(ImpsTags.ClientID, new ClientIdEncoder());
            sEncoders.put(ImpsTags.CapabilityList, new CapabilityEncoder());
            sEncoders.put(ImpsTags.Functions, new ServiceTreeEncoder());
            sEncoders.put(ImpsTags.Result, new ResultEncoder());
            sEncoders.put(ImpsTags.ContactListProperties, new ProperitiesEncoder(
                    PtsCodes.sContactListPropsToCode));
            sEncoders.put(ImpsTags.PresenceSubList, new PresenceSubListEncoder());

            ElemValueEncoder nickListEncoder = new NickListEncoder();
            sEncoders.put(ImpsTags.NickList, nickListEncoder);
            sEncoders.put(ImpsTags.AddNickList, nickListEncoder);
            sEncoders.put(ImpsTags.RemoveNickList, nickListEncoder);
        }
    }

    static class PresenceSubListEncoder extends ElemValueEncoder {
        private boolean mEncodePresenceValue;
        @Override
        public String encodeValue(Primitive p, PrimitiveElement elem)
                throws SerializerException {
            if (elem.getChildCount() == 0) {
                throw new SerializerException("No presence in the PresenceSubList");
            }

            StringBuilder buf = new StringBuilder();
            mEncodePresenceValue = ImpsTags.UpdatePresence_Request.equals(p.getType());

            ArrayList<PrimitiveElement> presences = elem.getChildren();
            int presenceCount = presences.size();
            if (presenceCount == 1) {
                if (mEncodePresenceValue) {
                    // Append an extra pair of braces according to the Spec
                    buf.append('(');
                    encodePresence(buf, presences.get(0));
                    buf.append(')');
                } else {
                    encodePresence(buf, presences.get(0));
                }
            } else {
                buf.append('(');
                for (int i = 0; i < presenceCount; i++) {
                    if (i > 0) {
                        buf.append(',');
                    }
                    encodePresence(buf, presences.get(i));
                }
                buf.append(')');
            }

            return buf.toString();
        }

        private void encodePresence(StringBuilder buf, PrimitiveElement p)
                throws SerializerException {
            boolean hasQualifier = p.getChild(ImpsTags.Qualifier) != null;
            String presenceName = p.getTagName();
            String presenceNameCode = getPresenceCode(presenceName);

            if (!mEncodePresenceValue) {
                encodeNoValuePresence(buf, p);
            } else {
                buf.append('(');
                buf.append(presenceNameCode);
                buf.append(',');
                if (hasQualifier) {
                    buf.append(p.getChildContents(ImpsTags.Qualifier));
                    buf.append(',');
                }
                // All the presences with value have this kind of structure:
                // <name, qualifier, value>
                // And for the values, there are three different hierarchies:
                // 1. Simply use PresenceValue to indicate the value, most of the
                //    presences has adapted this way. -> SingleValue
                // 2. Use special tags for multiple values of this presence, eg. ClientInfo
                //    has adapted this way. -> MultiValue
                // 3. Has one or more children for the presence, and each child have
                //    multiple values. eg. CommCap has adapted this way. -> ExtMultiValue
                if (isMultiValuePresence(presenceName)) {
                    // condition 2: multiple value
                    int emptyValueSize = hasQualifier ? 1 : 0;

                    ArrayList<PrimitiveElement> children = p.getChildren();
                    if (children.size() > emptyValueSize) {
                        buf.append('(');
                        int childCount = children.size();
                        int j = 0;  // used for first value check
                        for (int i = 0; i < childCount; i++, j++) {
                            PrimitiveElement value = children.get(i);
                            if (ImpsTags.Qualifier.equals(value.getTagName())) {
                                j--;
                                continue;
                            }

                            if (j > 0) {
                                buf.append(',');
                            }
                            buf.append('(');
                            buf.append(getPresenceCode(value.getTagName()));
                            buf.append(',');
                            buf.append(PtsCodes.getPAValueCode(value.getContents()));
                            buf.append(')');
                        }
                        buf.append(')');
                    }
                } else if (isExtMultiValuePresence(presenceName)) {
                    // condition 3: extended multiple value
                    // TODO: Implementation
                } else {
                    // Condition 1: single value
                    if (p.getChild(ImpsTags.PresenceValue) == null) {
                        throw new SerializerException("Can't find presence value for " + presenceName);
                    }
                    buf.append(PtsCodes.getPAValueCode(p.getChildContents(ImpsTags.PresenceValue)));
                }
                buf.append(')');
            }
        }

        private void encodeNoValuePresence(StringBuilder buf, PrimitiveElement p)
                throws SerializerException {
            if (p.getChildCount() == 0) {
                buf.append(getPresenceCode(p.getTagName()));
            } else {
                ArrayList<PrimitiveElement> children = p.getChildren();
                int childCount = children.size();
                buf.append('(');
                buf.append(getPresenceCode(p.getTagName()));
                buf.append(",(");
                for (int i = 0; i < childCount; i++) {
                    if (i > 0) {
                        buf.append(',');
                    }

                    encodeNoValuePresence(buf, children.get(i));
                }
                buf.append("))");
            }
        }

        private String getPresenceCode(String tagname) throws SerializerException {
            String code = PtsCodes.getPresenceAttributeCode(tagname);
            if (code == null) {
                throw new SerializerException("Unsupport presence attribute: " + tagname);
            }

            return code;
        }

        private boolean isMultiValuePresence(String presenceName) {
            if (ImpsTags.ClientInfo.equals(presenceName)) {
                return true;
            }

            // TODO: Add more supported extended multiple presence here
            return false;
        }

        private boolean isExtMultiValuePresence(String presenceName) {
            // TODO: Add supported extended multiple presence here
            return false;
        }
    }

    static class ClientIdEncoder extends ElemValueEncoder {
        @Override
        public String encodeValue(Primitive p, PrimitiveElement elem)
                throws SerializerException {
            String value = elem.getChildContents(ImpsTags.URL);
            if (value == null) {
                value = elem.getChildContents(ImpsTags.MSISDN);
            }

            return escapeValueString(value);
        }
    }

    static class CapabilityEncoder extends ElemValueEncoder {
        @Override
        public String encodeValue(Primitive p, PrimitiveElement elem)
                throws SerializerException {
            ArrayList<PrimitiveElement> caps = elem.getChildren();
            int i, len;
            StringBuilder result = new StringBuilder();
            result.append('(');
            for (i = 0, len = caps.size(); i < len; i++) {
                PrimitiveElement capElem = caps.get(i);
                String capName = capElem.getTagName();
                String capValue = capElem.getContents();

                if (i > 0) {
                    result.append(',');
                }
                if (!appendNameAndValue(result, capName, capValue,
                        PtsCodes.sCapElementToCode, PtsCodes.sCapValueToCode,
                        ImpsTags.SupportedCIRMethod.equals(capName))) {
                    result.deleteCharAt(result.length() - 1);
                }
            }
            result.append(')');
            return result.toString();
        }
    }

    static class ServiceTreeEncoder extends ElemValueEncoder {
        @Override
        public String encodeValue(Primitive p, PrimitiveElement elem)
                throws SerializerException {
            StringBuilder buf = new StringBuilder();
            buf.append('(');
            appendFeature(buf, elem.getFirstChild());
            buf.append(')');
            return buf.toString();
        }

        private void appendFeature(StringBuilder buf, PrimitiveElement elem)
                throws SerializerException {
            int childCount = elem.getChildCount();
            if (childCount > 0) {
                ArrayList<PrimitiveElement> children = elem.getChildren();
                for (int i = 0; i < childCount; i++) {
                    appendFeature(buf, children.get(i));
                }
            } else {
                String code = PtsCodes.getServiceTreeCode(elem.getTagName());
                if (code == null) {
                    throw new SerializerException("Invalid service tree tag:"
                            + elem.getTagName());
                }
                if (buf.length() > 1) {
                    buf.append(',');
                }
                buf.append(code);
            }
        }
    }

    static class ResultEncoder extends ElemValueEncoder {
        @Override
        public String encodeValue(Primitive p, PrimitiveElement elem)
                throws SerializerException {
            String code = elem.getChildContents(ImpsTags.Code);
            String desc = elem.getChildContents(ImpsTags.Description);
            // Client never sends partial success result, the DetailedResult is
            // ignored.
            if (desc == null) {
                return code;
            } else {
                StringBuilder res = new StringBuilder();
                appendPairValue(res, code, escapeValueString(desc));
                return res.toString();
            }
        }
    }

    static class NickListEncoder extends ElemValueEncoder {
        @Override
        public String encodeValue(Primitive p, PrimitiveElement elem)
                throws SerializerException {
            StringBuilder buf = new StringBuilder();
            ArrayList<PrimitiveElement> children = elem.getChildren();
            int count = children.size();
            buf.append('(');
            for (int i = 0; i < count; i++) {
                PrimitiveElement child = children.get(i);
                String tagName = child.getTagName();
                String nickName = null;
                String userId = null;
                if (tagName.equals(ImpsTags.NickName)) {
                    nickName = child.getChildContents(ImpsTags.Name);
                    userId = child.getChildContents(ImpsTags.UserID);
                } else if (tagName.equals(ImpsTags.UserID)) {
                    userId = child.getContents();
                }
                if (i > 0) {
                    buf.append(',');
                }
                if (nickName != null) {
                    nickName = escapeValueString(nickName);
                }
                appendPairValue(buf, nickName, escapeValueString(userId));
            }
            buf.append(')');
            return buf.toString();
        }
    }

    static class ProperitiesEncoder extends ElemValueEncoder {
        private HashMap<String, String> mPropNameCodes;

        public ProperitiesEncoder(HashMap<String, String> propNameCodes) {
            mPropNameCodes = propNameCodes;
        }

        @Override
        public String encodeValue(Primitive p, PrimitiveElement elem)
                throws SerializerException {
            ArrayList<PrimitiveElement> props = elem.getChildren();
            StringBuilder result = new StringBuilder();
            result.append('(');
            int count = props.size();
            for (int i = 0; i < count; i++) {
                PrimitiveElement property = props.get(i);
                String name;
                String value;
                if (property.getTagName().equals(ImpsTags.Property)) {
                    name = property.getChildContents(ImpsTags.Name);
                    value = property.getChildContents(ImpsTags.Value);
                } else {
                    name = property.getTagName();
                    value = property.getContents();
                }
                if (i > 0) {
                    result.append(',');
                }
                appendNameAndValue(result, name, value, mPropNameCodes, null, false);
            }
            result.append(')');
            return result.toString();
        }
    }
}
