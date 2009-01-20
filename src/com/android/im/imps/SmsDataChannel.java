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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import android.os.SystemClock;

import com.android.im.engine.HeartbeatService;
import com.android.im.engine.ImErrorInfo;
import com.android.im.engine.ImException;
import com.android.im.engine.SmsService;
import com.android.im.engine.SystemService;
import com.android.im.engine.SmsService.SmsListener;
import com.android.im.engine.SmsService.SmsSendFailureCallback;

public class SmsDataChannel extends DataChannel
        implements SmsListener, HeartbeatService.Callback {
    private SmsService mSmsService;
    private String mSmsAddr;
    private short mSmsPort;

    private long mLastActive;

    private SmsSplitter mSplitter;
    private SmsAssembler mAssembler;

    private LinkedBlockingQueue<Primitive> mReceiveQueue;

    private ImpsTransactionManager mTxManager;
    private boolean mConnected;
    private long mKeepAliveMillis;
    private Primitive mKeepAlivePrimitive;

    private long mReplyTimeout;
    private LinkedList<PendingTransaction> mPendingTransactions;
    private Timer mTimer;

    protected SmsDataChannel(ImpsConnection connection) throws ImException {
        super(connection);

        mTxManager = connection.getTransactionManager();

        ImpsConnectionConfig config = connection.getConfig();
        mReplyTimeout = config.getReplyTimeout();
        mSmsAddr = config.getSmsAddr();
        mSmsPort = (short) config.getSmsPort();
        mSmsService = SystemService.getDefault().getSmsService();

        mParser = new PtsPrimitiveParser();
        try {
            mSerializer = new PtsPrimitiveSerializer(config.getImpsVersion());
        } catch (SerializerException e) {
            throw new ImException(e);
        }
        mSplitter = new SmsSplitter(mSmsService.getMaxSmsLength());
        mAssembler = new SmsAssembler();
        mAssembler.setSmsListener(this);
    }

    @Override
    public void connect() throws ImException {
        mSmsService.addSmsListener(mSmsAddr, mSmsPort, mAssembler);
        mReceiveQueue = new LinkedBlockingQueue<Primitive>();
        mPendingTransactions = new LinkedList<PendingTransaction>();
        mTimer = new Timer(mReplyTimeout);
        new Thread(mTimer, "SmsDataChannel timer").start();
        mConnected = true;
    }

    @Override
    public long getLastActiveTime() {
        return mLastActive;
    }

    @Override
    public boolean isSendingQueueEmpty() {
        // Always true since we don't have a sending queue.
        return true;
    }

    @Override
    public Primitive receivePrimitive() throws InterruptedException {
        return mReceiveQueue.take();
    }

    @Override
    public void sendPrimitive(Primitive p) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            mSerializer.serialize(p, out);
            mSplitter.split(out.toByteArray());
            SmsService smsService =  SystemService.getDefault().getSmsService();
            SendFailureCallback sendFailureCallback
                    = new SendFailureCallback(p.getTransactionID());
            while (mSplitter.hasNext()) {
                smsService.sendSms(mSmsAddr, mSmsPort, mSplitter.getNext(),
                        sendFailureCallback);
            }
            mLastActive = SystemClock.elapsedRealtime();
            addPendingTransaction(p.getTransactionID());
        } catch (IOException e) {
            mTxManager.notifyErrorResponse(p.getTransactionID(),
                    ImpsErrorInfo.SERIALIZER_ERROR, e.getLocalizedMessage());
        } catch (SerializerException e) {
            mTxManager.notifyErrorResponse(p.getTransactionID(),
                    ImpsErrorInfo.SERIALIZER_ERROR, e.getLocalizedMessage());
        }
    }

    @Override
    public void shutdown() {
        mSmsService.removeSmsListener(this);
        mTimer.stop();
        mConnected = false;
    }

    @Override
    public void startKeepAlive(long interval) {
        if (!mConnected) {
            throw new IllegalStateException();
        }

        if (interval <= 0) {
            interval = mConnection.getConfig().getDefaultKeepAliveInterval();
        }

        mKeepAliveMillis = interval * 1000;
        if (mKeepAliveMillis < 0) {
            ImpsLog.log("Negative keep alive time. Won't send keep-alive");
        }
        mKeepAlivePrimitive = new Primitive(ImpsTags.KeepAlive_Request);

        HeartbeatService heartbeatService
            = SystemService.getDefault().getHeartbeatService();
        if (heartbeatService != null) {
            heartbeatService.startHeartbeat(this, mKeepAliveMillis);
        }
    }

    public long sendHeartbeat() {
        if (!mConnected) {
            return 0;
        }

        long inactiveTime = SystemClock.elapsedRealtime() - mLastActive;
        if (needSendKeepAlive(inactiveTime)) {
            sendKeepAlive();
            return mKeepAliveMillis;
        } else {
            return mKeepAliveMillis - inactiveTime;
        }
    }

    private void sendKeepAlive() {
        ImpsTransactionManager tm = mConnection.getTransactionManager();
        AsyncTransaction tx = new AsyncTransaction(tm) {
            @Override
            public void onResponseError(ImpsErrorInfo error) {
            }

            @Override
            public void onResponseOk(Primitive response) {
                // Since we never request a new timeout value, the response
                // can be ignored
            }
        };
        tx.sendRequest(mKeepAlivePrimitive);
    }

    private boolean needSendKeepAlive(long inactiveTime) {
        return mKeepAliveMillis - inactiveTime <= 500;
    }

    @Override
    public boolean resume() {
        return true;
    }

    @Override
    public void suspend() {
        // do nothing.
    }

    public void onIncomingSms(byte[] data) {
        try {
            Primitive p = mParser.parse(new ByteArrayInputStream(data));
            mReceiveQueue.put(p);
            removePendingTransaction(p.getTransactionID());
        } catch (ParserException e) {
            handleError(data, ImpsErrorInfo.PARSER_ERROR, e.getLocalizedMessage());
        } catch (IOException e) {
            handleError(data, ImpsErrorInfo.PARSER_ERROR, e.getLocalizedMessage());
        } catch (InterruptedException e) {
            handleError(data, ImpsErrorInfo.UNKNOWN_ERROR, e.getLocalizedMessage());
        }
    }

    private void handleError(byte[] data, int errCode, String info) {
        String trId = extractTrId(data);
        if (trId != null) {
            mTxManager.notifyErrorResponse(trId, errCode, info);
            removePendingTransaction(trId);
        }
    }

    private String extractTrId(byte[] data) {
        int transIdStart = 4;
        int index = transIdStart;
        while(Character.isDigit(data[index])) {
            index++;
        }
        int transIdLen = index - transIdStart;
        try {
            return new String(data, transIdStart, transIdLen, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private void addPendingTransaction(String transId) {
        synchronized (mPendingTransactions) {
            mPendingTransactions.add(new PendingTransaction(transId));
        }
    }

    private void removePendingTransaction(String transId) {
        synchronized (mPendingTransactions) {
            Iterator<PendingTransaction> iter = mPendingTransactions.iterator();
            while (iter.hasNext()) {
                PendingTransaction tx = iter.next();
                if (tx.mTransId.equals(transId)) {
                    iter.remove();
                    break;
                }
            }
        }
    }

    /*package*/void checkTimeout() {
        synchronized (mPendingTransactions) {
            Iterator<PendingTransaction> iter = mPendingTransactions.iterator();
            while (iter.hasNext()) {
                PendingTransaction tx = iter.next();
                if (tx.isExpired(mReplyTimeout)) {
                    notifyTimeout(tx);
                } else {
                    break;
                }
            }
        }
    }

    private void notifyTimeout(PendingTransaction tx) {
        String transId = tx.mTransId;
        mTxManager.notifyErrorResponse(transId, ImpsErrorInfo.TIMEOUT,
                "Timeout");
        removePendingTransaction(transId);
    }

    private class SendFailureCallback implements SmsSendFailureCallback {
        private String mTransId;

        public SendFailureCallback(String transId) {
            mTransId = transId;
        }

        public void onFailure(int errorCode) {
            mTxManager.notifyErrorResponse(mTransId, ImErrorInfo.NETWORK_ERROR, null);
        }
    }

    private class Timer implements Runnable {
        private boolean mStopped;
        private long mInterval;

        public Timer(long interval) {
            mInterval = interval;
            mStopped = false;
        }

        public void stop() {
            mStopped = true;
        }

        public void run() {
            while (!mStopped) {
                try {
                    Thread.sleep(mInterval);
                } catch (InterruptedException e) {
                    continue;
                }
                checkTimeout();
            }
        }
    }

    private static class PendingTransaction {
        private String mTransId;
        private long mSentTime;

        public PendingTransaction(String transId) {
            mTransId = transId;
        }

        public boolean isExpired(long timeout) {
            return SystemClock.elapsedRealtime() - mSentTime >= timeout;
        }
    }
}
