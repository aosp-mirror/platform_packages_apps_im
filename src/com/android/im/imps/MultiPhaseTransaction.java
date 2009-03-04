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

abstract class MultiPhaseTransaction extends AsyncTransaction {

    public enum TransactionStatus {
        TRANSACTION_COMPLETED,
        TRANSACTION_CONTINUE,
    }

    /* Multi-phase asynchronous transaction carries no AsyncCompletion.
     */
    public MultiPhaseTransaction(ImpsTransactionManager manager) {
        super(manager);
    }

    @Override
    protected void notifySuccessResponse(Primitive response) {
        TransactionStatus status = processResponse(response);
        if (status != TransactionStatus.TRANSACTION_CONTINUE) {
            mTransManager.endClientTransaction(this);
        }
    }

    @Override
    protected void notifyErrorResponse(ImpsErrorInfo error) {
        TransactionStatus status = processResponseError(error);
        if (status != TransactionStatus.TRANSACTION_CONTINUE) {
            mTransManager.endClientTransaction(this);
        }
    }

    public abstract TransactionStatus processResponseError(ImpsErrorInfo error);
    public abstract TransactionStatus processResponse(Primitive response);

    @Override
    final public void onResponseError(ImpsErrorInfo error) { }

    @Override
    final public void onResponseOk(Primitive response) { }

}
