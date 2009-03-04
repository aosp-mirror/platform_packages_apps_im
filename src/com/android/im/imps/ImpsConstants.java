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

public class ImpsConstants {

    public static enum ImpsVersion {
        IMPS_VERSION_11,
        IMPS_VERSION_12,
        IMPS_VERSION_13;

        public static ImpsVersion fromString(String value) {
            if ("1.1".equals(value)) {
                return IMPS_VERSION_11;
            } else if ("1.2".equals(value)) {
                return IMPS_VERSION_12;
            } else if ("1.3".equals(value)) {
                return IMPS_VERSION_13;
            } else {
                // Unknown version, use 1.2 as default
                return IMPS_VERSION_12;
            }
        }
    }

    // TODO: move these to some place else?
    public static final String CLIENT_PRODUCER = "MOKIA";
    public static final String CLIENT_VERSION = "0.1";

    public static final String VERSION_11_NS
        = "http://www.wireless-village.org/CSP1.1";
    public static final String TRANSACTION_11_NS
        = "http://www.wireless-village.org/TRC1.1";
    public static final String PRESENCE_11_NS
        = "http://www.wireless-village.org/PA1.1";

    public static final String VERSION_12_NS
        = "http://www.openmobilealliance.org/DTD/WV-CSP1.2";
    public static final String TRANSACTION_12_NS
        = "http://www.openmobilealliance.org/DTD/WV-TRC1.2";
    public static final String PRESENCE_12_NS
        = "http://www.openmobilealliance.org/DTD/WV-PA1.2";

    public static final String VERSION_13_NS
        = "http://www.openmobilealliance.org/DTD/IMPS-CSP1.3";
    public static final String TRANSACTION_13_NS
        = "http://www.openmobilealliance.org/DTD/IMPS-TRC1.3";
    public static final String PRESENCE_13_NS
        = "http://www.openmobilealliance.org/DTD/IMPS-PA1.3";

    public static final String ADDRESS_PREFIX = "wv:";

    public static final String TRUE = "T";
    public static final String FALSE = "F";
    public static final String SUCCESS_CODE = "200";
    public static final String Open = "Open";
    public static final String DisplayName = "DisplayName";
    public static final String GROUP_INVITATION = "GR";
    public static final String Default = "Default";

    public static final int STATUS_UNAUTHORIZED  = 401;
    public static final int STATUS_NOT_IMPLEMENTED = 501;
    public static final int STATUS_COULD_NOT_RECOVER_SESSION = 502;
    // status 760 is IMPS 1.2 only
    public static final int STATUS_AUTO_SUBSCRIPTION_NOT_SUPPORTED = 760;

    /** presence UserAvailability values */
    public static final String PRESENCE_AVAILABLE = "AVAILABLE";
    public static final String PRESENCE_NOT_AVAILABLE = "NOT_AVAILABLE";
    public static final String PRESENCE_DISCREET = "DISCREET";

    /** presence ClientType values */
    public static final String PRESENCE_MOBILE_PHONE = "MOBILE_PHONE";
    public static final String PRESENCE_COMPUTER     = "COMPUTER";
    public static final String PRESENCE_PDA          = "PDA";
    public static final String PRESENCE_CLI          = "CLI";
    public static final String PRESENCE_OTHER        = "OTHER";

    public static final String COMMC_CAP_IM      = "IM";
}
