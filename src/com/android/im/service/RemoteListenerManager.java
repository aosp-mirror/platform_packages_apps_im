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

package com.android.im.service;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import android.os.RemoteException;
import android.os.IInterface;
import android.util.Log;

/**
 * Manages and notifies remote listeners.
 *
 * @param <E>
 */
class RemoteListenerManager<E extends IInterface> {
    private static final String TAG = RemoteImService.TAG;

    private final CopyOnWriteArrayList<E> mRemoteListeners;

    public RemoteListenerManager() {
        mRemoteListeners = new CopyOnWriteArrayList<E>();
    }

    public void addRemoteListener(E listener) {
        mRemoteListeners.add(listener);
    }

    public void removeRemoteListener(E listener) {
        // we may have duplicated listeners, or different listeners that
        // are actually a same binder.
        ArrayList<E> listeners = new ArrayList<E>();
        for (E remoteListener : mRemoteListeners) {
            if (remoteListener.asBinder() == listener.asBinder()) {
                listeners.add(remoteListener);
            }
        }
        if (!listeners.isEmpty()) {
            mRemoteListeners.removeAll(listeners);
        }
    }

    /* This method does not hold a lock on the listener list during the
     * traversal in order not to block {@link #addRemoteListener(E)} or
     * {@link #removeRemoteListener(E)}.
     */
    protected boolean notifyRemoteListeners(ListenerInvocation<E> invocation) {
        boolean notified = false;
        ArrayList<E> deadListeners = null;

        for (E remoteListener : mRemoteListeners) {
            try {
                invocation.invoke(remoteListener);
                notified = true;
            } catch (RemoteException e) {
                Log.i(TAG, remoteListener.getClass().getName()
                        + ": removing dead listener ");
                if (deadListeners == null) {
                    deadListeners = new ArrayList<E>();
                }
                deadListeners.add(remoteListener);
            }
        }
        if (deadListeners != null) {
            mRemoteListeners.removeAll(deadListeners);
        }
        return notified;
    }

    // a simplified solution for java.reflect.Method
    static interface ListenerInvocation<E extends IInterface > {
        public void invoke(E remoteListener) throws RemoteException;
    }

}
