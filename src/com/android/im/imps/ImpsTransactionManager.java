/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.im.imps;

import java.util.HashMap;

import com.android.im.engine.ImErrorInfo;
import com.android.im.imps.Primitive.TransactionMode;

public class ImpsTransactionManager {
    private ImpsConnection mConnection;
    private int mTransactionId;

    /** Keep track of the client initialized transactions. */
    private HashMap<String, AsyncTransaction> mClientTransactions;

    private HashMap<String, ServerTransactionListener> mServerTransactionListeners;

    /**
     * Constructs an instance of transaction manager.
     */
    ImpsTransactionManager(ImpsConnection connection) {
        this.mConnection = connection;
        mClientTransactions = new HashMap<String, AsyncTransaction>();
        mServerTransactionListeners = new HashMap<String, ServerTransactionListener>();
    }

    /**
     * Sets a ServerTransactionListener on this manager so that it will be
     * notified when a specified transaction has been initialized by the server.
     *
     * @param type the primitive type of the transaction.
     * @param listener the ServerTransactionListener to be notified, or
     *            <code>null</code> to clear the listener on specified type.
     */
    public void setTransactionListener(String type, ServerTransactionListener listener) {
        synchronized(mServerTransactionListeners) {
            if (listener == null) {
                mServerTransactionListeners.remove(type);
            } else {
                mServerTransactionListeners.put(type, listener);
            }
        }
    }

    /**
     * Originates an async transaction from the client.
     * @param tx
     */
    void beginClientTransaction(AsyncTransaction tx) {
        synchronized(mClientTransactions) {
            tx.setTransactionInfo(nextTransactionId(), mConnection);
            mClientTransactions.put(tx.getId(), tx);
        }
    }

    /**
     * Terminates a transaction which was originated from the client.
     *
     * @param tx the transaction to terminate.
     */
    void endClientTransaction(AsyncTransaction tx) {
        synchronized(mClientTransactions) {
            mClientTransactions.remove(tx.getId());
        }
    }

    void reassignTransactionId(Primitive p) {
        synchronized (mClientTransactions) {
            AsyncTransaction tx = mClientTransactions.remove(p.getTransactionID());
            if(tx != null) {
                String newId = nextTransactionId();
                tx.setTransactionInfo(newId, mConnection);
                p.setTransactionId(newId);
                mClientTransactions.put(newId, tx);
            }
        }
    }

    /**
     * TODO: This should not be called from the DataChannel thread.
     *
     * @param transactionId
     * @param code
     * @param info
     */
    public void notifyErrorResponse(String transactionId,
            int code, String info) {
        AsyncTransaction tx;
        synchronized(mClientTransactions) {
            tx = mClientTransactions.get(transactionId);
        }
        if (tx != null) {
            tx.notifyError(new ImErrorInfo(code, info));
        } else {
            ImpsLog.log("Ignoring possible server transaction error " + code + info);
        }
    }

    /**
     * Notifies the TransactionManager that a new primitive from the server has
     * arrived.
     *
     * @param primitive the incoming primitive.
     */
    public void notifyIncomingPrimitive(Primitive primitive) {
        String transactionId = primitive.getTransactionID();
        if (primitive.getTransactionMode() == TransactionMode.Response) {

            AsyncTransaction tx;
            synchronized(mClientTransactions) {
                tx = mClientTransactions.get(transactionId);
            }
            // The transaction might has been terminated by the client,
            // just ignore the incoming primitive in that case.
            if (tx != null) {
                tx.notifyResponse(primitive);
            }
        } else {
            ServerTransaction serverTx = new ServerTransaction(transactionId,
                    mConnection, primitive);

            ServerTransactionListener listener;
            synchronized(mServerTransactionListeners) {
                listener = mServerTransactionListeners.get(primitive.getType());
            }
            if (listener != null) {
                listener.notifyServerTransaction(serverTx);
            } else {
                ImpsLog.log("Unhandled Server transaction: " + primitive.getType());
            }
        }
    }

    /**
     * Generates a new transaction ID.
     *
     * @return a new transaction ID.
     */
    private synchronized String nextTransactionId() {
        if(mTransactionId >= 999) {
            mTransactionId = 0;
        }
        return String.valueOf(++mTransactionId);
    }

}
