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

/**
 * Manage presence polling from the server. If the server does not support
 * subscribing presence change or prefer the client polling presence, the client
 * should send GetPresence-Request periodically. 
 */
public class PresencePollingManager implements Runnable {
    private boolean mStopped;
    private boolean mFinished;

    private long mPollingInterval;
    private Object mLock = new Object();

    private ImpsAddress[] mPollingAddress;
    private ImpsAddress[] mContactLists;

    private ImpsContactListManager mManager;
    private Thread mPollingThread;

    public PresencePollingManager(ImpsContactListManager manager,
            long pollingIntervalMillis) {
        mManager = manager;
        mPollingInterval = pollingIntervalMillis;
        mStopped = true;
        mFinished = false;
    }

    public void resetPollingContacts() {
        synchronized (mLock) {
            mContactLists = null;
        }
    }

    public void startPolling() {
        synchronized (mLock) {
            // Clear the polling address; the polling thread will fetch the
            // presence of all the contacts in lists.
            mPollingAddress = null;
        }
        doStartPolling();
    }

    public void startPolling(ImpsUserAddress user){
        synchronized (mLock) {
            mPollingAddress = new ImpsAddress[] { user };
        }
        doStartPolling();
    }

    public void stopPolling() {
        mStopped = true;
    }

    public void shutdownPolling() {
        mFinished = true;
        synchronized (mLock) {
            mLock.notify();
        }
    }

    public void run() {
        while (!mFinished) {
            synchronized (mLock) {
                if (!mStopped) {
                    ImpsAddress[] pollingAddress = mPollingAddress;
                    if (pollingAddress == null) {
                        // Didn't specify of which contacts the presence will
                        // poll. Fetch the presence of all contacts in list.
                        pollingAddress = getContactLists();
                    }
                    if (pollingAddress != null) {
                        mManager.fetchPresence(pollingAddress);
                    }
                }

                try {
                    mLock.wait(mPollingInterval);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

    private void doStartPolling() {
        mStopped = false;
        if (mPollingThread == null) {
            mPollingThread = new Thread(this, "PollingThread");
            mPollingThread.setDaemon(true);
            mPollingThread.start();
        } else {
            synchronized (mLock) {
                mLock.notify();
            }
        }
    }

    private ImpsAddress[] getContactLists() {
        if (mContactLists == null) {
            mContactLists = mManager.getAllListAddress();
        }
        return mContactLists;
    }

}
