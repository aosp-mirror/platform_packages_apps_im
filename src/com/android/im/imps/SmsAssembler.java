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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.im.engine.SmsService.SmsListener;

public class SmsAssembler implements SmsListener {
    // WVaaBBcccDD
    //   aa - version number; 12 for 1.2, 13 for 1.3; "XX" for version discovery
    //   BB - message type, case insensitive
    //   ccc - transaction id in range 0-999 without preceding zero
    //   DD - multiple SMSes identifier
    private static final Pattern sPreamplePattern =
        Pattern.compile("\\AWV(\\d{2})(\\p{Alpha}{2})(\\d{1,3})(\\p{Alpha}{2})?");

    private SmsListener mListener;
    private HashMap<String, RawPtsData> mPtsCache;

    public SmsAssembler() {
        mPtsCache = new HashMap<String, RawPtsData>();
    }

    public void setSmsListener(SmsListener listener) {
        mListener = listener;
    }

    public void onIncomingSms(byte[] data) {
        String preamble = extractPreamble(data);
        if (preamble == null) {
            ImpsLog.logError("Received non PTS SMS");
            return;
        }

        Matcher m = sPreamplePattern.matcher(preamble);
        if (!m.matches()) {
            ImpsLog.logError("Received non PTS SMS");
            return;
        }
        String dd = m.group(4);
        if (dd == null || dd.length() == 0) {
            notifyAssembledSms(data);
        } else {
            int totalSegmentsCount = dd.charAt(1) - 'a' + 1;
            int index = dd.charAt(0) - 'a';
            if (index < 0 || index >= totalSegmentsCount) {
                ImpsLog.logError("Invalid multiple SMSes identifier");
                return;
            }

            String transId = m.group(3);
            RawPtsData pts = mPtsCache.get(transId);
            if (pts == null) {
                pts = new RawPtsData(preamble.length(), totalSegmentsCount);
                mPtsCache.put(transId, pts);
            }

            pts.setSegment(index, data);
            if (pts.isAllSegmentsReceived()) {
                mPtsCache.remove(transId);
                notifyAssembledSms(pts.assemble());
            }
        }
    }

    private String extractPreamble(byte[] data) {
        int N = data.length;
        int preambleIndex = 0;
        while (data[preambleIndex] != ' ' && preambleIndex < N) {
            preambleIndex++;
        }

        if (preambleIndex >= N) {
            return null;
        }

        try {
            return new String(data, 0, preambleIndex, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // impossible
            return null;
        }
    }

    private void notifyAssembledSms(byte[] data) {
        if (mListener != null) {
            mListener.onIncomingSms(data);
        }
    }

    private static class RawPtsData {
        private int mOrigPreambeLen;
        private byte[][] mSegments;

        public RawPtsData(int origPreambleLen, int totalSegments) {
            mOrigPreambeLen = origPreambleLen;
            mSegments = new byte[totalSegments][];
        }

        public void setSegment(int index, byte[] segment) {
            mSegments[index] = segment;
        }

        public boolean isAllSegmentsReceived() {
            for (byte[] segment : mSegments) {
                if (segment == null) {
                    return false;
                }
            }
            return true;
        }

        public byte[] assemble() {
            int len = calculateLength();
            byte[] res = new byte[len];
            int index = 0;
            // copy the preamble
            System.arraycopy(mSegments[0], 0, res, index, mOrigPreambeLen - 2);
            index += mOrigPreambeLen - 2;
            res[index++] = ' ';

            for (byte[] segment : mSegments) {
                int payloadStart = mOrigPreambeLen + 1;
                int payloadLen = segment.length - payloadStart;
                System.arraycopy(segment, payloadStart, res, index, payloadLen);
                index += payloadLen;
            }
            return res;
        }

        private int calculateLength() {
            // don't have 'dd' in assembled data
            int preambleLen = mOrigPreambeLen - 2;

            int total = preambleLen + 1;// a space after preamble
            for (byte[] segment : mSegments) {
                int segmentPayload = segment.length - (mOrigPreambeLen + 1);
                total += segmentPayload;
            }
            return total;
        }
    }

}
