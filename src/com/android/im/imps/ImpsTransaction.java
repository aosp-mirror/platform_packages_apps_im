/*
 * Copyright (C) 2007 Esmertec AG.
 * Copyright (C) 2007 The Android Open Source Project
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
 * Represents an IMPS transaction which is a basic communication mechanism
 * between an IMPS client and an IMPS SAP. A transaction usually consists of a
 * request and a response primitive. The transactions MAY originate from either
 * IMPS client or IMPS SAP.
 */
class ImpsTransaction {
    private String mId;
    private ImpsConnection mConnection;

    /**
     * Creates a new transaction.
     *
     * @param id the id of the transaction.
     */
    protected ImpsTransaction() {
    }

    void setTransactionInfo(String id, ImpsConnection conn) {
        mId = id;
        mConnection = conn;
    }

    /**
     * Gets the id of this transaction.
     *
     * @return the id of this transaction.
     */
    public String getId() {
        return mId;
    }

    protected void sendPrimitive(Primitive primitive) {
        ImpsSession session = mConnection.getSession();
        if (session != null) {
            primitive.setSession(session.getID());
        }
        primitive.setTransaction(this);

        mConnection.sendPrimitive(primitive);
    }

}
