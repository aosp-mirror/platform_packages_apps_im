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

import java.util.Map;

import com.android.im.engine.ConnectionConfig;
import com.android.im.engine.ImException;
import com.android.im.imps.ImpsConstants.ImpsVersion;
import com.android.im.plugin.ImpsConfigNames;
import com.android.im.plugin.PasswordDigest;
import com.android.im.plugin.PresenceMapping;

/**
 * The configuration for IMPS connection.
 */
public class ImpsConnectionConfig extends ConnectionConfig {
    private static final int DEFAULT_KEEPALIVE_SECONDS  = 2 * 60 * 60; // 2 hour
    private static final int DEFAULT_MIN_SERVER_POLL    = 30;    // seconds

    private static final int DEFAULT_SMS_PORT = 3590;
    private static final int DEFAULT_SMS_CIR_PORT = 3716;
    private static final long DEFAULT_PRESENCE_POLL_INTERVAL = 60 * 1000; // 1 minute

    // DeliveryReport is good for acknowledgment but consumes 2 extra
    // transactions + 1 possible CIR notification per message. Should not
    // be enabled on limited/slow connections.
    private static final boolean NEED_DELIVERY_REPORT   = false;

    private ImpsVersion mImpsVersion = ImpsVersion.IMPS_VERSION_12;

    private TransportType mDataChannelBinding = TransportType.HTTP;
    private CirMethod mCirChannelBinding = CirMethod.STCP;
    private EncodingType mDataEncoding = EncodingType.WBXML;
    private boolean mSecureLogin = true;    // default to use 4-way login
    private boolean mBasicPresenceOnly = false;
    private String mHost         = "";
    private String mClientId     = "JiMMY";
    private String mMsisdn;

    private int mUdpPort = 3717;
    private int mReplyTimeout = 45 * 1000;

    private String mContentType;
    private String mVersionNs;
    private String mTransactionNs;
    private String mPresenceNs;

    private PresenceMapping mPresenceMapping;
    private PasswordDigest mPasswordDigest;

    private String mDefaultDomain;

    private String mCustomPasswordDigest;
    private String mCustomPresenceMapping;

    private String mPluginPath;

    private Map<String, String> mOthers;

    public ImpsConnectionConfig() {
        setupVersionStrings();
    }

    public ImpsConnectionConfig(Map<String, String> map) {
        String dataChannel = map.get(ImpsConfigNames.DATA_CHANNEL);
        try {
            mDataChannelBinding = TransportType.valueOf(dataChannel);
        } catch (IllegalArgumentException e) {
            ImpsLog.log("Unknown DataChannel: " + dataChannel +", using HTTP");
            mDataChannelBinding = TransportType.HTTP;
        }

        String dataEncoding = map.get(ImpsConfigNames.DATA_ENCODING);
        try {
            mDataEncoding = EncodingType.valueOf(dataEncoding);
        } catch (IllegalArgumentException e) {
            ImpsLog.log("Unknown DataEncoding: " + dataEncoding +", using WBXML");
            mDataEncoding = EncodingType.WBXML;
        }

        String cirChannel = map.get(ImpsConfigNames.CIR_CHANNEL);
        try {
            mCirChannelBinding = CirMethod.valueOf(cirChannel);
        } catch (IllegalArgumentException e) {
            ImpsLog.log("Unknown CirChannel: " + cirChannel +", using TCP");
            mCirChannelBinding = CirMethod.STCP;
        }

        mHost = map.get(ImpsConfigNames.HOST);

        if (map.get(ImpsConfigNames.CLIENT_ID) != null) {
            mClientId = map.get(ImpsConfigNames.CLIENT_ID);
        }

        if (map.get(ImpsConfigNames.MSISDN) != null) {
            mMsisdn = map.get(ImpsConfigNames.MSISDN);
        }
        if (map.get(ImpsConfigNames.SECURE_LOGIN) != null) {
            mSecureLogin = isTrue(map.get(ImpsConfigNames.SECURE_LOGIN));
        }
        if (map.get(ImpsConfigNames.BASIC_PA_ONLY) != null) {
            mBasicPresenceOnly = isTrue(map.get(ImpsConfigNames.BASIC_PA_ONLY));
        }
        if (map.containsKey(ImpsConfigNames.VERSION)) {
            mImpsVersion = ImpsVersion.fromString(
                    map.get(ImpsConfigNames.VERSION));
        }
        setupVersionStrings();

        mDefaultDomain = map.get(ImpsConfigNames.DEFAULT_DOMAIN);

        mPluginPath = map.get(ImpsConfigNames.PLUGIN_PATH);
        mCustomPasswordDigest = map.get(ImpsConfigNames.CUSTOM_PASSWORD_DIGEST);
        mCustomPresenceMapping = map.get(ImpsConfigNames.CUSTOM_PRESENCE_MAPPING);

        mOthers = map;
    }

    @Override
    public String getProtocolName() {
        return "IMPS";
    }

    private void setupVersionStrings() {
        if (mImpsVersion == ImpsVersion.IMPS_VERSION_11) {
            if (mDataEncoding == EncodingType.XML) {
                mContentType = "application/vnd.wv.csp.xml";
            } else if (mDataEncoding == EncodingType.WBXML) {
                mContentType = "application/vnd.wv.csp.wbxml";
            } else if (mDataEncoding == EncodingType.SMS) {
                mContentType = "application/vnd.wv.csp.sms";
            }
            mVersionNs = ImpsConstants.VERSION_11_NS;
            mTransactionNs = ImpsConstants.TRANSACTION_11_NS;
            mPresenceNs = ImpsConstants.PRESENCE_11_NS;
        } else if (mImpsVersion == ImpsVersion.IMPS_VERSION_12) {
            if (mDataEncoding == EncodingType.XML) {
                mContentType = "application/vnd.wv.csp.xml";
            } else if (mDataEncoding == EncodingType.WBXML) {
                mContentType = "application/vnd.wv.csp.wbxml";
            } else if (mDataEncoding == EncodingType.SMS) {
                mContentType = "application/vnd.wv.csp.sms";
            }

            mVersionNs = ImpsConstants.VERSION_12_NS;
            mTransactionNs = ImpsConstants.TRANSACTION_12_NS;
            mPresenceNs = ImpsConstants.PRESENCE_12_NS;
        } else if (mImpsVersion == ImpsVersion.IMPS_VERSION_13){
            if (mDataEncoding == EncodingType.XML) {
                mContentType = "application/vnd.wv.csp+xml";
            } else if (mDataEncoding == EncodingType.WBXML) {
                mContentType = "application/vnd.wv.csp+wbxml";
            } else if (mDataEncoding == EncodingType.SMS) {
                mContentType = "application/vnd.wv.csp.sms";
            }

            mVersionNs = ImpsConstants.VERSION_13_NS;
            mTransactionNs = ImpsConstants.TRANSACTION_13_NS;
            mPresenceNs = ImpsConstants.PRESENCE_13_NS;
        }
    }

    public String getClientId() {
        return mClientId;
    }

    public String getMsisdn() {
        return mMsisdn;
    }

    public boolean use4wayLogin() {
        return mSecureLogin;
    }

    public boolean useSmsAuth() {
        return isTrue(mOthers.get(ImpsConfigNames.SMS_AUTH));
    }

    public boolean needDeliveryReport() {
        return NEED_DELIVERY_REPORT;
    }

    /**
     * Gets the type of protocol binding for data channel.
     *
     * @return the type of protocol binding for data channel.
     */
    public TransportType getDataChannelBinding() {
        return mDataChannelBinding;
    }

    /**
     * Gets the type of protocol binding for CIR channel.
     *
     * @return the type of protocol binding for CIR channel.
     */
    public CirMethod getCirChannelBinding() {
        return mCirChannelBinding;
    }

    /**
     * Gets the host name of the server.
     *
     * @return the host name of the server.
     */
    public String getHost() {
        return mHost;
    }

    /**
     * Sets the host name of the server.
     *
     * @param host the host name.
     */
    public void setHost(String host) {
        mHost = host;
    }

    /**
     * Gets the number of milliseconds to wait for a response from the server.
     * The default value is 45 seconds.
     *
     * @return the milliseconds to wait for a response from the server
     */
    public int getReplyTimeout() {
        return mReplyTimeout;
    }

    /**
     * XXX: Workaround for the OZ IMPS GTalk server which supports only basic
     * presence attributes and don't support GetAttributeList.
     *
     * @return <code>true</code> if only basic presence attribute is
     *         supported.
     */
    public boolean supportBasicPresenceOnly() {
        return mBasicPresenceOnly;
    }

    public String getTransportContentType() {
        return mContentType;
    }

    public ImpsVersion getImpsVersion() {
        return mImpsVersion;
    }

    // TODO: should remove this and let the serializer to handle all the name
    // spaces.
    public String getPresenceNs() {
        return mPresenceNs;
    }

    public PrimitiveParser createPrimitiveParser() throws ImException {
        if(mDataEncoding == EncodingType.WBXML) {
            return new WbxmlPrimitiveParser();
        } else if (mDataEncoding == EncodingType.XML) {
            return new XmlPrimitiveParser();
        } else if (mDataEncoding == EncodingType.SMS) {
            return new PtsPrimitiveParser();
        }

        ImpsLog.log("Unknown DataEncoding: " + mDataEncoding);
        return null;
    }

    public PrimitiveSerializer createPrimitiveSerializer() {
        if(mDataEncoding == EncodingType.WBXML) {
            return new WbxmlPrimitiveSerializer(mImpsVersion,
                    mVersionNs, mTransactionNs);
        } else if (mDataEncoding == EncodingType.XML) {
            return new XmlPrimitiveSerializer(mVersionNs, mTransactionNs);
        } else if (mDataEncoding == EncodingType.SMS) {
            try {
                return new PtsPrimitiveSerializer(mImpsVersion);
            } catch (SerializerException e) {
                ImpsLog.logError(e);
                return null;
            }
        }

        ImpsLog.log("Unknown DataEncoding: " + mDataEncoding);
        return null;
    }

    /**
     * Gets the port number for the standalone UDP/IP CIR method. This is only
     * useful when UDP CIR method is used. The default port number is 3717.
     *
     * @return the port number for the standalone UDP/IP CIR method.
     */
    public int getUdpPort() {
        return mUdpPort;
    }

    public String getSmsAddr() {
        return mOthers.get(ImpsConfigNames.SMS_ADDR);
    }

    public int getSmsPort() {
        String value = mOthers.get(ImpsConfigNames.SMS_PORT);
        if (value == null) {
            return DEFAULT_SMS_PORT;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return DEFAULT_SMS_PORT;
        }
    }

    public String getSmsCirAddr() {
        return mOthers.get(ImpsConfigNames.SMS_CIR_ADDR);
    }

    public int getSmsCirPort() {
        String value = mOthers.get(ImpsConfigNames.SMS_CIR_PORT);
        if (value == null) {
            return DEFAULT_SMS_CIR_PORT;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return DEFAULT_SMS_CIR_PORT;
        }
    }

    public boolean usePrensencePolling() {
        String value = mOthers.get(ImpsConfigNames.POLL_PRESENCE);
        return isTrue(value);
    }

    public long getPresencePollInterval() {
        String value = mOthers.get(ImpsConfigNames.PRESENCE_POLLING_INTERVAL);
        if (value == null) {
            return DEFAULT_PRESENCE_POLL_INTERVAL;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return DEFAULT_PRESENCE_POLL_INTERVAL;
        }
    }

    public int getDefaultServerPollMin() {
        return DEFAULT_MIN_SERVER_POLL;
    }

    public int getDefaultKeepAliveInterval() {
        return DEFAULT_KEEPALIVE_SECONDS;
    }

    public PresenceMapping getPresenceMapping() {
        if (mPresenceMapping != null) {
            return mPresenceMapping;
        }

        if (mCustomPresenceMapping != null) {
            try {
                mPresenceMapping = new CustomPresenceMapping(mPluginPath,
                        mCustomPresenceMapping);
            } catch (ImException e) {
                ImpsLog.logError("Failed to load custom presence mapping", e);
            }
        }

        if (mPresenceMapping == null) {
            mPresenceMapping = new DefaultPresenceMapping();
        }
        return mPresenceMapping;
    }

    public PasswordDigest getPasswordDigest() {
        if (mPasswordDigest != null) {
            return mPasswordDigest;
        }

        if (mCustomPasswordDigest != null) {
            try {
                mPasswordDigest = new CustomPasswordDigest(mPluginPath, mCustomPasswordDigest);
            } catch (ImException e) {
                ImpsLog.logError("Can't load custom password digest method", e);
            }
        }

        if (mPasswordDigest == null) {
            mPasswordDigest = new StandardPasswordDigest();
        }
        return mPasswordDigest;
    }

    public String getDefaultDomain() {
        return mDefaultDomain;
    }

    private boolean isTrue(String value) {
        return "true".equalsIgnoreCase(value);
    }

    /**
     * Represents the type of protocol binding for data channel.
     */
    public static enum TransportType {
        WAP, HTTP, HTTPS, SMS,
    }

    /**
     * Represents the type of the data encoding.
     */
    public static enum EncodingType {
        XML, WBXML, SMS,
    }

    /**
     * Represents the type of protocol binding for CIR channel.
     */
    public static enum CirMethod {
        /**
         * WAP 1.2 or WAP 2.0 push using WSP unit push message and SMS as a
         * bearer
         */
        WAPSMS,

        /**
         * WAP 1.2 or WAP 2.0 push using WSP unit push message and UDP/IP as a
         * bearer
         */
        WAPUDP,

        /**
         * Standalone SMS binding
         */
        SSMS,

        /**
         * Standalone UDP/IP binding
         */
        SUDP,

        /**
         * Standalone TCP/IP binding
         */
        STCP,

        /**
         * Standalone HTTP binding
         */
        SHTTP,

        /**
         * No CIR channel
         */
        NONE,
    }
}
