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
import java.io.OutputStream;

import com.android.im.imps.ImpsConstants.ImpsVersion;

/*
 * NOT thread-safe. Always use this in one thread.
 */
final class WbxmlSerializer {
    private OutputStream mOut;
    private int mNativeHandle;

    private static int PUBLIC_ID_IMPS_11 = 0x10;
    private static int PUBLIC_ID_IMPS_12 = 0x11;
    private static int PUBLIC_ID_IMPS_13 = 0x12;

    public WbxmlSerializer(ImpsVersion impsVersion) {
        if (impsVersion == ImpsVersion.IMPS_VERSION_11) {
            mNativeHandle = nativeCreate(PUBLIC_ID_IMPS_11);
        } else if (impsVersion == ImpsVersion.IMPS_VERSION_12) {
            mNativeHandle = nativeCreate(PUBLIC_ID_IMPS_12);
        } else if (impsVersion == ImpsVersion.IMPS_VERSION_13) {
            mNativeHandle = nativeCreate(PUBLIC_ID_IMPS_13);
        } else {
            throw new IllegalArgumentException("Unsupported IMPS version");
        }
        if (mNativeHandle == 0) {
            throw new OutOfMemoryError();
        }
    }

    @Override
    protected void finalize() {
        if (mNativeHandle != 0) {
            nativeRelease(mNativeHandle);
        }
    }

    public void reset() {
        nativeReset(mNativeHandle);
        mOut = null;
    }

    public void setOutput(OutputStream out) {
        mOut = out;
    }

    // XXX: These should throw ParserException but the dalvik seems to have
    // problem throwing non-system exceptions from JNI code. Use IAE for now
    // and file a bug report for this.
    public void startElement(String name, String[] atts) throws IOException,
            SerializerException {
        try {
            nativeStartElement(mNativeHandle, name, atts);
        } catch (IllegalArgumentException e) {
            throw new SerializerException(e);
        }
    }

    public void characters(String chars) throws IOException, SerializerException {
        try {
            nativeCharacters(mNativeHandle, chars);
        } catch (IllegalArgumentException e) {
            throw new SerializerException(e);
        }
    }

    public void endElement() throws IOException, SerializerException {
        try {
            nativeEndElement(mNativeHandle);
        } catch (IllegalArgumentException e) {
            throw new SerializerException(e);
        }
    }

    /**
     * Called by native encoder to send result data.
     * @param data
     * @param len
     * @throws IOException
     */
    void onWbxmlData(byte[] data, int len) throws IOException {
        if (mOut != null) {
            mOut.write(data, 0, len);
        }
    }

    native int nativeCreate(int publicId);

    native void nativeReset(int nativeHandle);
    native void nativeRelease(int nativeHandle);

    // FIXME: These should throw ParserException but the dalvik seems to have
    // problem throwing non-system exceptions from JNI code. Use IAE for now
    // and file a bug report for this.
    native void nativeStartElement(int nativeHandle, String name, String[] atts)
            throws IOException, IllegalArgumentException;
    native void nativeCharacters(int nativeHandle, String characters)
            throws IOException, IllegalArgumentException;
    native void nativeEndElement(int nativeHandle)
            throws IOException, IllegalArgumentException;

    static {
        try {
            System.loadLibrary("wbxml_jni");
        }catch(UnsatisfiedLinkError ule) {
            System.err.println("WARNING: Could not load library libwbxml_jni.so");
        }
    }
}
