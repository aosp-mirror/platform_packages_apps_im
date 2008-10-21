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

import com.android.internal.util.HexDump;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.util.Log;

public class ImpsLog {
    public static final String TAG = "IMPS";

    public static final String PACKET_TAG = "IMPS/Packet";

    public static final boolean DEBUG = true;

    private static PrimitiveSerializer mSerialzier;

    private ImpsLog() {
    }

    static {
        // we don't really care about the namespace values in the log
        mSerialzier = new XmlPrimitiveSerializer("", "");
    }

    public static void dumpRawPacket(byte[] bytes) {
        Log.d(PACKET_TAG, HexDump.dumpHexString(bytes));
    }

    public static void dumpPrimitive(Primitive p) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            mSerialzier.serialize(p, out);
        } catch (IOException e) {
            Log.e(PACKET_TAG, "Bad Primitive");
        } catch (SerializerException e) {
            Log.e(PACKET_TAG, "Bad Primitive");
        }
        Log.d(PACKET_TAG, out.toString());
    }

    public static void log(Primitive primitive) {
        if(DEBUG) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                mSerialzier.serialize(primitive, out);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (SerializerException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            Log.i(TAG, out.toString());
        }
    }

    public static void log(String info) {
        if(DEBUG) {
            Log.d(TAG, /* DateFormat.format("kk:mm:ss ", new Date()) + */ info);
        }
    }

    public static void logError(Throwable t) {
        Log.e(TAG, /* DateFormat.format("kk:mm:ss", new Date()).toString() */ "", t);
    }

    public static void logError(String info, Throwable t) {
        Log.e(TAG, /* DateFormat.format("kk:mm:ss ", new Date()) + */ info, t);
    }

    public static void logError(String info) {
        Log.e(TAG, /* DateFormat.format("kk:mm:ss ", new Date()) + */ info);
    }
}
