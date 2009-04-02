/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

import com.android.im.engine.ImErrorInfo;
import com.android.im.engine.ImException;
import com.android.im.engine.SmsService;
import com.android.im.engine.SystemService;
import com.android.im.engine.SmsService.SmsListener;
import com.android.im.engine.SmsService.SmsSendFailureCallback;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;

public class SmsCirChannel extends CirChannel
        implements SmsListener, SmsSendFailureCallback {
    private String mAddr;
    private int mPort;

    private SmsService mSmsService;

    protected SmsCirChannel(ImpsConnection connection) {
        super(connection);
        ImpsConnectionConfig cfg = connection.getConfig();
        mAddr = cfg.getSmsCirAddr();
        mPort = cfg.getSmsCirPort();
    }

    @Override
    public void connect() throws ImException {
        if (mAddr == null || mAddr.length() == 0) {
            throw new ImException(ImpsErrorInfo.UNKNOWN_SERVER,
                    "Invalid sms addr");
        }
        mSmsService = SystemService.getDefault().getSmsService();
        mSmsService.addSmsListener(mAddr, mPort, this);
        sendHelo();
    }

    @Override
    public void shutdown() {
        mSmsService.removeSmsListener(this);
    }

    public void onIncomingSms(byte[] data) {
        // It's safe to assume that each character is encoded into 7-bit since
        // all characters in CIR are in gsm 7-bit alphabet.
        int lengthSeptets = data.length * 8 / 7;
        int numPaddingBits = data.length * 8 % 7;
        String s = GsmAlphabet.gsm7BitPackedToString(data, 0,
                lengthSeptets, numPaddingBits);
        // CIR format: WVCI <version> <session cookie>
        if (!s.startsWith("WVCI")) {
            // not a valid CIR, ignore.
            return;
        }
        String[] fields = s.split(" ");
        if (fields.length != 3) {
            // Not a valid CIR, ignore
            return;
        }
        String sessionCookie = mConnection.getSession().getCookie();
        if (sessionCookie.equalsIgnoreCase(fields[2])) {
            mConnection.sendPollingRequest();
        }
    }

    public void onFailure(int errorCode) {
        mConnection.shutdownOnError(new ImErrorInfo(ImpsErrorInfo.NETWORK_ERROR,
                "Could not establish SMS CIR channel"));
    }

    private void sendHelo() {
        String data = "HELO " + mConnection.getSession().getID();
        try {
            byte[] bytes = GsmAlphabet.stringToGsm7BitPacked(data);
            mSmsService.sendSms(mAddr, mPort, bytes, this);
        } catch (EncodeException ignore) {
        }
    }

}
