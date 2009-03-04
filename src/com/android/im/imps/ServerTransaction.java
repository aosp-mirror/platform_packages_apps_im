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

import com.android.im.imps.Primitive.TransactionMode;

final class ServerTransaction extends ImpsTransaction {
    private Primitive mRequest;

    ServerTransaction(String id, ImpsConnection connection, Primitive request) {
        setTransactionInfo(id, connection);
        mRequest = request;
        request.setTransaction(this);
    }

    /**
     * Gets the request of this server transaction.
     *
     * @return the request.
     */
    public Primitive getRequest() {
        return mRequest;
    }

    /**
     * Sends a response of this transaction back to the server.
     *
     * @param response the response to send.
     */
    public void sendResponse(Primitive response) {
        response.setTransactionMode(TransactionMode.Response);
        sendPrimitive(response);
    }

    /**
     * Sends a Status response of this transaction back to the server.
     *
     * @param response the response to send.
     */
    public void sendStatusResponse(String code) {
        Primitive status = new Primitive(ImpsTags.Status);
        status.setTransactionMode(TransactionMode.Response);
        status.addElement(ImpsTags.Result).addChild(ImpsTags.Code, code);

        sendPrimitive(status);
    }

}
