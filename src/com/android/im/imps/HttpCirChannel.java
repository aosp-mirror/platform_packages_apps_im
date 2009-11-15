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

import android.os.SystemClock;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * An implementation of CIR channel with standalone HTTP binding.
 */
class HttpCirChannel extends CirChannel implements Runnable {
    private static final int HTTP_CIR_PING_INTERVAL = 10000;

    private DataChannel mDataChannel;

    private boolean mStopped;
    private Thread mPollingTask;

    private URL mCirUrl;

    private long mServerPollMin;

    public HttpCirChannel(ImpsConnection connection, DataChannel dataChannel) {
        super(connection);
        this.mDataChannel = dataChannel;
    }

    @Override
    public synchronized void connect() {
        ImpsSession session = mConnection.getSession();
        try {
            if (session.getCirHttpAddress() != null) {
                mCirUrl = new URL(session.getCirHttpAddress());
            }
        } catch (MalformedURLException e) {
            // Ignore
        }
        mServerPollMin = session.getServerPollMin() * 1000;

        mStopped = false;
        mPollingTask = new Thread(this, "HTTPCIRChannel");
        mPollingTask.setDaemon(true);
        mPollingTask.start();
    }

    public synchronized boolean isShutdown() {
        return mStopped;
    }

    @Override
    public synchronized void shutdown() {
        mStopped = true;
    }

    public void run() {
        while (!mStopped) {
            long lastActive = mDataChannel.getLastActiveTime();

            if (mCirUrl != null) {
                if (SystemClock.elapsedRealtime() - lastActive >= HTTP_CIR_PING_INTERVAL) {
                    HttpURLConnection urlConnection;
                    try {
                        urlConnection = (HttpURLConnection) mCirUrl
                                .openConnection();

                        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            mConnection.sendPollingRequest();
                        }
                    } catch (IOException e) {
                        mStopped = true;
                    }
                }

                try {
                    Thread.sleep(HTTP_CIR_PING_INTERVAL);
                } catch (InterruptedException e) {
                    // Ignore.
                }
            } else {
                // The server didn't provide a URL for CIR poll in the
                // capability negotiation, just send PollingRequest.
                if (SystemClock.elapsedRealtime() - lastActive >= mServerPollMin
                        && mDataChannel.isSendingQueueEmpty()) {
                    mConnection.sendPollingRequest();
                }

                try {
                    Thread.sleep(mServerPollMin);
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
        }
    }
}
