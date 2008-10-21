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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;

public class XmlPrimitiveSerializer implements PrimitiveSerializer {
    private String mVersionNs;
    private String mTransacNs;

    public XmlPrimitiveSerializer(String versionNs, String transacNs) {
        mVersionNs = versionNs;
        mTransacNs = transacNs;
    }

    public void serialize(Primitive primitive, OutputStream out) throws IOException {
        try {
            Writer writer = 
                new BufferedWriter(new OutputStreamWriter(out, "UTF-8"), 8192);

            PrimitiveElement elem = primitive.createMessage(mVersionNs,
                    mTransacNs);
            writeElement(writer, elem);
            writer.flush();
        } catch (UnsupportedEncodingException e) {
            // Impossible. UTF-8 is always supported
            ImpsLog.logError(e);
        }
    }

    private void writeElement(Writer writer, PrimitiveElement element)
            throws IOException {
        writer.write('<');
        writer.write(element.getTagName());

        Map<String, String> attrMap = element.getAttributes();
        if(attrMap != null && attrMap.size() > 0) {
            for (Map.Entry<String, String> entry : attrMap.entrySet()) {
                writer.write(' ');
                writer.write(entry.getKey());
                writer.write("=\"");
                writeEncoded(writer, entry.getValue());
                writer.write('"');
            }
        }

        if (element.getContents() != null) {
            writer.write('>');
            writeEncoded(writer, element.getContents());

            writer.write("</");
            writer.write(element.getTagName());
            writer.write('>');
        } else if (element.getChildCount() > 0) {
            writer.write('>');

            for (PrimitiveElement child : element.getChildren()) {
                writeElement(writer, child);
            }
            writer.write("</");
            writer.write(element.getTagName());
            writer.write('>');
        } else {
            writer.write("/>");
        }
    }

    private void writeEncoded(Writer writer, String str) throws IOException {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);

            switch (ch) {
                case '<':
                    writer.write("&lt;");
                    break;

                case '>':
                    writer.write("&gt;");
                    break;

                case '&':
                    writer.write("&amp;");
                    break;

                case '"':
                    writer.write("&quot;");
                    break;

                case '\'':
                    writer.write("&apos;");
                    break;

                default:
                    writer.write(ch);
            }
        }
    }
}
