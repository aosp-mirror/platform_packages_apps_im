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

import com.android.im.imps.ImpsConnectionConfig.CirMethod;
import com.android.im.imps.ImpsConnectionConfig.TransportType;

/**
 * The configuration of the capabilities of the client.
 */
final class ImpsClientCapability {

    private ImpsClientCapability() {
    }

    /**
     * Gets the type of the client.
     *
     * @return the type of the client.
     */
    public static String getClientType() {
        return "MOBILE_PHONE";
    }

    /**
     * Get the maximum number of bytes of XML (WBXML, SMS - depending on the
     * actual encoding) primitive that the client-side parser can handle.
     *
     * @return the maximum number of bytes that the parser can handle.
     */
    public static int getParserSize() {
        // TODO: we do not really have a limit for this for now. Just return
        // a number big enough.
        return 256 * 1024;
    }

    /**
     * Get the maximum number of bytes of the message content that the client
     * can handle.
     *
     * @return the maximum number of bytes of the message content that the
     * client can handle.
     */
    public static int getAcceptedContentLength() {
        return 256 * 1024;
    }

    /**
     * Gets the maximum number of open transactions from both client and
     * server side at any given time.
     *
     * @return the maximum number of open transactions.
     */
    public static int getMultiTrans() {
        return 1;
    }

    /**
     * Gets the maximum number of primitives that the client can handle within
     * the same transport message at any given time.
     *
     * @return the maximum number of primitives within the same transport
     *         message.
     */
    public static int getMultiTransPerMessage() {
        return 1;
    }

    /**
     * Gets the initial IM delivery method that the recipient client prefers in
     * the set of "PUSH" and "Notify/Get".
     *
     * @return "P" if prefers "PUSH", or "N" if prefers "Notify/Get".
     */
    public static String getInitialDeliveryMethod() {
        return "P";
    }

    /**
     * Get supported CIR methods in preferred order.
     *
     * @return a array of supported CIR methods.
     */
    public static CirMethod[] getSupportedCirMethods() {
        return new CirMethod[] {
                CirMethod.STCP,
                CirMethod.SSMS,
                CirMethod.SHTTP,
        };
    }

    /**
     * Get supported bearers (HTTP(S), WSP, SMS).
     *
     * @return the array of supported bearers.
     */
    public static TransportType[] getSupportedBearers() {
        return new TransportType[] {
                TransportType.HTTP
        };
    }

    /**
     * Get supported Presence attributes
     *
     * @return the array of supported Presence attributes
     */
    public static String[] getSupportedPresenceAttribs() {
        return new String[] {
                ImpsTags.OnlineStatus,
                ImpsTags.ClientInfo,
                ImpsTags.UserAvailability,
                ImpsTags.StatusText,
                ImpsTags.StatusContent,
        };
    };

    /**
     * Gets the basic presence attributes.
     *
     * @return an array of the basic Presence attributes.
     */
    public static String[] getBasicPresenceAttributes() {
        return new String[] {
                ImpsTags.OnlineStatus,
                ImpsTags.ClientInfo,
                ImpsTags.UserAvailability,
        };
    }

}
