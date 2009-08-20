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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import com.android.im.engine.HeartbeatService;
import com.android.im.engine.ImErrorInfo;
import com.android.im.engine.ImException;
import com.android.im.engine.SystemService;

import android.os.SystemClock;
import android.util.Log;

/**
 * An implementation of CIR channel with standalone TCP/IP banding.
 */
class TcpCirChannel extends CirChannel implements Runnable, HeartbeatService.Callback {
    public static final int PING_INTERVAL = 20 * 60 * 1000; // 20 min

    private static final int OK_TIMEOUT = 30000;

    private String mAddress;
    private int mPort;
    private boolean mDone;
    private boolean mReconnecting;
    private Object mReconnectLock = new Object();
    private Socket mSocket;

    private boolean mWaitForOK;
    private long mLastActive;
    private String mUser;
    private BufferedReader mReader;

    private Thread mCirThread;

    protected TcpCirChannel(ImpsConnection connection) {
        super(connection);
        mAddress = connection.getSession().getCirTcpAddress();
        mPort = connection.getSession().getCirTcpPort();
        mUser = connection.getSession().getLoginUser().getName();
    }

    @Override
    public synchronized void connect() throws ImException {
        try {
            mDone = false;
            connectServer();
            mCirThread = new Thread(this, "TcpCirChannel");
            mCirThread.setDaemon(true);
            mCirThread.start();
            HeartbeatService heartbeatService
                    = SystemService.getDefault().getHeartbeatService();
            if (heartbeatService != null) {
                heartbeatService.startHeartbeat(this, PING_INTERVAL);
            }
        } catch (UnknownHostException e) {
            throw new ImException(ImErrorInfo.UNKNOWN_SERVER,
                    "Can't find the TCP CIR server");
        } catch (IOException e) {
            throw new ImException(ImErrorInfo.CANT_CONNECT_TO_SERVER,
                    "Can't connect to the TCP CIR server");
        }
    }

    @Override
    public synchronized void shutdown() {
        if (Log.isLoggable(ImpsLog.TAG, Log.DEBUG)) {
            ImpsLog.log(mUser + " Shutting down CIR channel");
        }
        mDone = true;
        if (mReconnecting) {
            synchronized (mReconnectLock) {
                mReconnecting = false;
                mReconnectLock.notify();
            }
        }
        try {
            if(mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            // ignore
        }
        HeartbeatService heartbeatService
                = SystemService.getDefault().getHeartbeatService();
        if (heartbeatService != null) {
            heartbeatService.stopHeartbeat(this);
        }
    }

    public boolean isShutdown() {
        return mDone;
    }

    public void run() {
        while (!mDone) {
            try {
                if (mWaitForOK && SystemClock.elapsedRealtime() - mLastActive
                        > OK_TIMEOUT) {
                    // OMA-TS-IMPS_CSP_Transport-V1_3-20070123-A 8.1.3:
                    // If client doesn't receive an "OK" message or detects
                    // that the connection is broken, it MUST open a new
                    // TCP/IP connection and send the "HELO" message again.
                    reconnectAndWait();
                }

                String line = mReader.readLine();
                mLastActive = SystemClock.elapsedRealtime();

                if (line == null) {
                    if (Log.isLoggable(ImpsLog.TAG, Log.DEBUG)) {
                        ImpsLog.log(mUser + " TCP CIR: socket closed by server.");
                    }
                    reconnectAndWait();
                } else if ("OK".equals(line)) {
                    mWaitForOK = false;
                    if (Log.isLoggable(ImpsLog.TAG, Log.DEBUG)) {
                        ImpsLog.log(mUser + " << TCP CIR: OK Received");
                    }
                    // TODO: Since we just have one thread per TCP CIR
                    // connection now, the session cookie is ignored.
                } else if (line.startsWith("WVCI")) {
                    if (Log.isLoggable(ImpsLog.TAG, Log.DEBUG)) {
                        ImpsLog.log(mUser + " << TCP CIR: CIR Received");
                    }
                    if (!mDone) {
                        mConnection.sendPollingRequest();
                    }
                }
            } catch (IOException e) {
                ImpsLog.logError("TCP CIR channel get:" + e);
                if(!mDone){
                    reconnectAndWait();
                }
            }
        }
        if (mReader != null) {
            try {
                mReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (Log.isLoggable(ImpsLog.TAG, Log.DEBUG)) {
            ImpsLog.log(mUser + " CIR channel thread quit");
        }
    }

    @Override
    public void reconnect() {
        synchronized (mReconnectLock) {
            if (mReconnecting) {
                return;
            } else {
                mReconnecting = true;
            }
        }

        if (Log.isLoggable(ImpsLog.TAG, Log.DEBUG)) {
            ImpsLog.log(mUser + " CIR channel reconnecting");
        }
        long waitTime = 3000;
        while (!mDone) { // Keep trying to connect the server until shutdown
            try {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                }
                connectServer();
                // Send a polling request to make sure we don't miss anything
                // while CIR is down.
                if(!mDone) {
                    mConnection.sendPollingRequest();
                }
                break;
            } catch (IOException e) {
                waitTime *= 3;
                if(waitTime > 27000) {
                    waitTime = 3000;
                    if(!mDone){
                        mConnection.sendPollingRequest();
                    }
                }
                if (Log.isLoggable(ImpsLog.TAG, Log.DEBUG)) {
                    ImpsLog.log(mUser + " CIR channel reconnect fail, retry after "
                        + waitTime / 1000 + " seconds");
                }
            }
        }
        synchronized (mReconnectLock) {
            mReconnecting = false;
            mReconnectLock.notify();
        }
    }

    private void reconnectAndWait() {
        reconnect();
        // in case reconnect() has already been called in another thread, wait
        // for it to finish
        while (!mDone) {
            synchronized (mReconnectLock) {
                if (mReconnecting) {
                    try {
                        mReconnectLock.wait();
                    } catch (InterruptedException e) {
                        // ignore
                    }
                } else {
                    break;
                }
            }
        }
    }

    private synchronized void connectServer() throws IOException {
        if(!mDone) {
            if (mSocket != null) {
                if (Log.isLoggable(ImpsLog.TAG, Log.DEBUG)) {
                    ImpsLog.log(mUser + " TCP CIR: close previous socket");
                }
                try {
                    mSocket.close();
                } catch (IOException e) {
                    // ignore
                }
            }

            mSocket = new Socket(mAddress, mPort);
            if (Log.isLoggable(ImpsLog.TAG, Log.DEBUG)) {
                ImpsLog.log(mUser + " >> TCP CIR: HELO");
            }
            sendData("HELO " + mConnection.getSession().getID() + "\r\n");
            if (mReader != null) {
                try {
                    mReader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            mReader = new BufferedReader(
                    new InputStreamReader(mSocket.getInputStream(), "UTF-8"),
                    8192);
        }
    }

    public synchronized long sendHeartbeat() {
        if (mDone) {
            return 0;
        }

        long inactiveTime = SystemClock.elapsedRealtime() - mLastActive;
        if(needSendPing(inactiveTime)) {
            if (Log.isLoggable(ImpsLog.TAG, Log.DEBUG)) {
                ImpsLog.log(mUser + " >> TCP CIR: PING");
            }
            try {
                sendData("PING \r\n");
            } catch (IOException e) {
                if (Log.isLoggable(ImpsLog.TAG, Log.DEBUG)) {
                    ImpsLog.log("Failed to send PING, try to reconnect");
                }
                reconnect();
            }
            return PING_INTERVAL;
        } else {
            return PING_INTERVAL - inactiveTime;
        }
    }

    private boolean needSendPing(long inactiveTime) {
        return (PING_INTERVAL - inactiveTime) < 500;
    }

    private void sendData(String s) throws IOException {
        mSocket.getOutputStream().write(s.getBytes("UTF-8"));
        mWaitForOK = true;
        mLastActive = SystemClock.elapsedRealtime();
    }

}
