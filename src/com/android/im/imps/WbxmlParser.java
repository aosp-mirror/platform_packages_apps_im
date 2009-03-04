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

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*
 * NOT thread-safe. Always use this in one thread.
 */
final class WbxmlParser {
    private static final int BUFFER_SIZE = 1024;

    private ContentHandler mContentHandler;
    private int mNativeParser;
    private AttributesImpl atts;

    public WbxmlParser() {
        atts = new AttributesImpl();
        mNativeParser = nativeCreate("UTF-8");
        if (mNativeParser == 0) {
            throw new OutOfMemoryError();
        }
    }

    @Override
    protected void finalize() {
        if (mNativeParser != 0) {
            nativeRelease(mNativeParser);
        }
    }

    public void setContentHandler(ContentHandler contentHandler) {
        mContentHandler = contentHandler;
    }

    public void reset() {
        if (mNativeParser != 0) {
            nativeReset(mNativeParser);
        }
        atts.names = null;
        atts.values = null;
        mContentHandler = null;
    }

    public void parse(InputSource in) throws ParserException, SAXException, IOException {
        InputStream byteStream = in.getByteStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        // FIXME: nativeParse should throw ParserException but the dalvik
        // seems to have problem throwing non-system exceptions from JNI
        // code. Use IAE for now and file a bug report for this.
        try {
            while ((length = byteStream.read(buffer)) != -1) {
                nativeParse(mNativeParser, buffer, length, false);
            }
            nativeParse(mNativeParser, new byte[1], 0, true);
        } catch (IllegalArgumentException e) {
            throw new ParserException(e);
        }
    }

    void startElement(String name, String[] attrNames, String[] attrValues)
            throws SAXException {
        atts.names = attrNames;
        atts.values = attrValues;
        if(mContentHandler != null) {
            mContentHandler.startElement("", name, name, atts);
        }
    }

    void endElement(String name) throws SAXException {
        if(mContentHandler != null) {
            mContentHandler.endElement("", name, name);
        }
    }

    void characters(char[] ch, int length) throws SAXException {
        if(mContentHandler != null) {
            mContentHandler.characters(ch, 0, length);
        }
    }

    static native void nativeStaticInitialize();

    native int nativeCreate(String encoding);

    native void nativeRelease(int nativeParser);

    native void nativeReset(int nativeParser);

    // XXX: nativeParse should throw ParserException but the dalvik seems to
    // have problem throwing non-system exceptions from JNI code. Use IAE
    // for now and file a bug report for this.
    native void nativeParse(int nativeParser, byte[] ch, int length,
            boolean isEnd) throws IllegalArgumentException, SAXException, IOException;

    static {
        try {
            System.loadLibrary("wbxml_jni");
            nativeStaticInitialize();
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("WARNING: Could not load library libwbxml_jni.so");
        }
    }

    static class AttributesImpl implements Attributes {
        String[] names = null;
        String[] values = null;

        public int getIndex(String qName) {
            if(names == null) {
                return -1;
            }
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(qName)) {
                    return i;
                }
            }
            return -1;
        }

        public int getIndex(String uri, String localName) {
            if(!"".equals(uri)) {
                return -1;
            }
            return getIndex(localName);
        }

        public int getLength() {
            return names == null ? 0 : names.length;
        }

        public String getLocalName(int index) {
            if(index < 0 || index >= getLength()) {
                return null;
            }
            return names[index];
        }

        public String getQName(int index) {
            if(index < 0 || index >= getLength()) {
                return null;
            }
            return names[index];
        }

        public String getType(int index) {
            if(index < 0 || index >= getLength()) {
                return null;
            }
            return "CDATA";
        }

        public String getType(String qName) {
            return getIndex(qName) == -1 ? null : "CDATA";
        }

        public String getType(String uri, String localName) {
            return getIndex(uri, localName) == -1 ? null : "CDATA";
        }

        public String getURI(int index) {
            if(index < 0 || index >= getLength()) {
                return null;
            }
            return "";
        }

        public String getValue(int index) {
            if(index < 0 || index >= getLength()) {
                return null;
            }
            return values[index];
        }

        public String getValue(String qName) {
            int index = getIndex(qName);
            return index == -1 ? null : values[index];
        }

        public String getValue(String uri, String localName) {
            int index = getIndex(uri, localName);
            return index == -1 ? null : values[index];
        }
    }
}
