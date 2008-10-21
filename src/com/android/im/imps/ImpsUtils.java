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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.android.im.engine.ImErrorInfo;

public class ImpsUtils {

    private static final HashMap<String, String> sClientInfo;
    private static String sSessionCookie;
    private static int sSessionCookieNumber;

    private ImpsUtils() {
    }

    static {
        // TODO: v1.2 doesn't support ClientContentLimit
        sClientInfo = new HashMap<String, String>();
        sClientInfo.put(ImpsTags.ClientType, ImpsClientCapability.getClientType());
        sClientInfo.put(ImpsTags.ClientProducer, ImpsConstants.CLIENT_PRODUCER);
        sClientInfo.put(ImpsTags.ClientVersion, ImpsConstants.CLIENT_VERSION);
    }

    /**
     * Checks if a string is a boolean value of true IMPS.
     *
     * @param value the string value.
     * @return <code>true</code> if it's true in IMPS.
     */
    public static boolean isTrue(String value) {
        return ImpsConstants.TRUE.equalsIgnoreCase(value);
    }

    /**
     * Checks if a string is a boolean value of false in IMPS.
     *
     * @param value the string value.
     * @return true if it's false in IMPS
     */
    public static boolean isFalse(String value) {
        return ImpsConstants.FALSE.equalsIgnoreCase(value);
    }

    /**
     * Return the IMPS String presentation of the boolean value
     *
     * @param isTrue the boolean value
     * @return the String presentation
     */
    public static String toImpsBool(boolean isTrue) {
        if (isTrue) {
            return ImpsConstants.TRUE;
        }

        return ImpsConstants.FALSE;
    }

    /**
     * Checks if the response primitive indicates successful.
     *
     * @param response the response primitive.
     * @returns <code>null</code> if the status code is 200 or an ImpsErrorInfo instance
     */
    public static ImpsErrorInfo checkResultError(Primitive response) {
        PrimitiveElement result = response.getElement(ImpsTags.Result);
        if (result == null) {
            return null;
        }

        String resultCode = result.getChild(ImpsTags.Code).getContents();
        if (!ImpsConstants.SUCCESS_CODE.equals(resultCode)) {
            PrimitiveElement descElem = result.getChild(ImpsTags.Description);
            String errorDesc = (descElem == null) ? "" : descElem.getContents();
            int statusCode = parseInt(resultCode, ImErrorInfo.ILLEGAL_SERVER_RESPONSE);
            return new ImpsErrorInfo(statusCode, errorDesc, response);
        }
        return null;
    }

    /**
     * Returns a copy of the string, with leading and trailing whitespace
     * omitted. Unlike the standard trim which just removes '\u0020'(the space
     * character), it removes all possible leading and trailing whitespace
     * character.
     *
     * @param str the string.
     * @return a copy of the string, with leading and trailing whitespace
     *         omitted.
     */
    public static String trim(String str) {
        if (null == str || "".equals(str))
            return str;

        int strLen = str.length();
        int start = 0;
        while (start < strLen && Character.isWhitespace(str.charAt(start)))
            start++;
        int end = strLen - 1;
        while (end >= 0 && Character.isWhitespace(str.charAt(end)))
            end--;
        if (end < start)
            return "";
        str = str.substring(start, end + 1);
        return str;
    }

    /**
     * Check whether the presence element has a qualified attribute value.
     * An attribute value is invalid when:
     *  1. An attribute is authorized but not yet updated for the first time
     *  2. The user wants to indicate that the value of the attribute is unknown.
     *
     * @param elem the presence element
     * @return <code>true</code> if the value of attribute is valid.
     */
    public static boolean isQualifiedPresence(PrimitiveElement elem) {
        if (null == elem || null == elem.getChild(ImpsTags.Qualifier)) {
            return false;
        }

        return ImpsUtils.isTrue(elem.getChildContents(ImpsTags.Qualifier));
    }

    public static Map<String, String> getClientInfo() {
        return Collections.unmodifiableMap(sClientInfo);
    }

    synchronized static String genSessionCookie() {
        if(sSessionCookie == null) {
            Random random = new Random();
            sSessionCookie = System.currentTimeMillis() + "" + random.nextInt();
        }
        return sSessionCookie + (sSessionCookieNumber++);
    }

    public static int parseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            // ignore
            return defaultValue;
        }
    }

    public static long parseLong(String s, long defaultValue) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            // ignore
            return defaultValue;
        }
    }
}
