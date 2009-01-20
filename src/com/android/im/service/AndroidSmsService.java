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
package com.android.im.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.Telephony;
import static android.provider.Telephony.Sms.Intents.DATA_SMS_RECEIVED_ACTION;
import android.telephony.PhoneNumberUtils;
import android.telephony.gsm.SmsManager;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

import com.android.im.engine.SmsService;

public class AndroidSmsService implements SmsService {
    private static final String TAG = RemoteImService.TAG;

    private static final String SMS_STATUS_RECEIVED_ACTION =
        "com.android.im.SmsService.SMS_STATUS_RECEIVED";

    private static final int sMaxSmsLength =
        SmsMessage.MAX_USER_DATA_BYTES - 7/* UDH size */;

    private Context mContext;
    private SmsReceiver mSmsReceiver;
    private IntentFilter mIntentFilter;
    /*package*/HashMap<Integer, ListenerList> mListeners;
    /*package*/HashMap<Long, SmsSendFailureCallback> mFailureCallbacks;

    public AndroidSmsService(Context context) {
        mContext = context;
        mSmsReceiver = new SmsReceiver();
        mIntentFilter = new IntentFilter(
                Telephony.Sms.Intents.DATA_SMS_RECEIVED_ACTION);
        mIntentFilter.addDataScheme("sms");
        mListeners = new HashMap<Integer, ListenerList>();
        mFailureCallbacks = new HashMap<Long, SmsSendFailureCallback>();
    }

    public int getMaxSmsLength() {
        return sMaxSmsLength;
    }

    public void sendSms(String dest, int port, byte[] data) {
        sendSms(dest, port, data, null);
    }

    public void sendSms(String dest, int port, byte[] data,
            SmsSendFailureCallback callback) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            try {
                log(dest + ":" + port + " >>> " + new String(data, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
            }
        }
        if (data.length > sMaxSmsLength) {
            Log.e(TAG, "SMS data message can only contain " + sMaxSmsLength
                    + " bytes");
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();
        PendingIntent sentIntent;
        if (callback == null) {
            sentIntent = null;
        } else {
            long msgId = genMsgId();
            mFailureCallbacks.put(msgId, callback);

            sentIntent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(
                        SMS_STATUS_RECEIVED_ACTION,
                        Uri.parse("content://sms/" + msgId), /*uri*/
                        mContext, SmsReceiver.class),
                0);
        }
        smsManager.sendDataMessage(dest, null/*use the default SMSC*/,
                (short) port, data,
                sentIntent,
                null/*do not require delivery report*/);
    }

    public void addSmsListener(String from, int port, SmsListener listener) {
        ListenerList l = mListeners.get(port);
        if (l == null) {
            l = new ListenerList(port);
            mListeners.put(port, l);

            // We didn't listen on the port yet, register the receiver with the
            // additional port.
            mIntentFilter.addDataAuthority("*", String.valueOf(port));
            mContext.registerReceiver(mSmsReceiver, mIntentFilter);
        }
        l.addListener(from, listener);
    }

    public void removeSmsListener(SmsListener listener) {
        Iterator<ListenerList> iter = mListeners.values().iterator();
        while (iter.hasNext()) {
            ListenerList l = iter.next();
            l.removeListener(listener);
            if (l.isEmpty()) {
                iter.remove();
            }
        }
    }

    public void stop() {
        mContext.unregisterReceiver(mSmsReceiver);
    }

    private static long sNextMsgId = 0;
    private static synchronized long genMsgId() {
        return sNextMsgId++;
    }

    private static void log(String msg) {
        Log.d(TAG, "[SmsService]" + msg);
    }

    private final class SmsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SMS_STATUS_RECEIVED_ACTION.equals(intent.getAction())) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    log("send status received");
                }
                long id = ContentUris.parseId(intent.getData());
                SmsSendFailureCallback callback = mFailureCallbacks.get(id);
                if (callback == null) {
                    return;
                }

                int resultCode = getResultCode();
                if (resultCode == SmsManager.RESULT_ERROR_GENERIC_FAILURE) {
                    callback.onFailure(SmsSendFailureCallback.ERROR_GENERIC_FAILURE);
                } else if (resultCode == SmsManager.RESULT_ERROR_RADIO_OFF) {
                    callback.onFailure(SmsSendFailureCallback.ERROR_RADIO_OFF);
                }
                mFailureCallbacks.remove(id);
            } else if (DATA_SMS_RECEIVED_ACTION.equals(intent.getAction())){
                Uri uri = intent.getData();
                int port = uri.getPort();
                ListenerList listeners = mListeners.get(port);
                if (listeners == null) {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        log("No listener on port " + port + ", ignore");
                    }
                    return;
                }

                SmsMessage[] receivedSms
                    = Telephony.Sms.Intents.getMessagesFromIntent(intent);

                for (SmsMessage msg : receivedSms) {
                    String from = msg.getOriginatingAddress();
                    byte[] data = msg.getUserData();
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        try {
                            log(from + ":" + port + " <<< " + new String(data, "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                        }
                    }
                    listeners.notifySms(from, data);
                }
            }
        }
    }

    private final static class ListenerList {
        private int mPort;
        private ArrayList<String> mAddrList;
        private ArrayList<SmsListener> mListenerList;

        public ListenerList(int port) {
            mPort = port;
            mAddrList = new ArrayList<String>();
            mListenerList = new ArrayList<SmsListener>();
        }

        public synchronized void addListener(String addr, SmsListener listener) {
            mAddrList.add(addr);
            mListenerList.add(listener);
        }

        public synchronized void removeListener(SmsListener listener) {
            int index = -1;
            while ((index = mListenerList.indexOf(listener)) != -1) {
                mAddrList.remove(index);
                mListenerList.remove(index);
            }
        }

        public void notifySms(String addr, byte[] data) {
            int N = mListenerList.size();
            for (int i = 0; i < N; i++) {
                if (PhoneNumberUtils.compare(addr, mAddrList.get(i))) {
                    mListenerList.get(i).onIncomingSms(data);
                }
            }
        }

        public boolean isEmpty() {
            return mListenerList.isEmpty();
        }

        public int getPort() {
            return mPort;
        }
    }
}
