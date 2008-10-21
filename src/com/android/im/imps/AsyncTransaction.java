/*
 * Copyright (C) 2007 Esmertec AG.
 * Copyright (C) 2007 The Android Open Source Project
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

import com.android.im.engine.ImErrorInfo;

abstract class AsyncTransaction extends ImpsTransaction {

    private final AsyncCompletion mCompletionCallback;
    private boolean mCompletionNotified;
    protected final ImpsTransactionManager mTransManager;

    AsyncTransaction(ImpsTransactionManager manager) {
        this(manager, null);
    }

    AsyncTransaction(ImpsTransactionManager manager, AsyncCompletion completion) {
        mTransManager = manager;
        mCompletionCallback = completion;
        manager.beginClientTransaction(this);
    }

    /**
     * Sends a request within this transaction.
     *
     * @param request the request to send.
     */
    public void sendRequest(Primitive request) {
        sendPrimitive(request);
    }

    /**
     * Notify that an error occurs in the transaction.
     *
     * @param error the error
     */
    final void notifyError(ImErrorInfo error) {
        notifyErrorResponse(new ImpsErrorInfo(error.getCode(), error.getDescription(), null));
    }

    /**
     * Notify that a response from the server has arrived.
     *
     * @param response the response.
     */
    final void notifyResponse(Primitive response) {
        response.setTransaction(this);
        ImpsErrorInfo error = ImpsUtils.checkResultError(response);
        if (error != null) {
            notifyErrorResponse(error);
        } else {
            notifySuccessResponse(response);
        }
    }

    protected void notifyErrorResponse(ImpsErrorInfo error) {
        onResponseError(error);
        mTransManager.endClientTransaction(this);
        notifyAsyncCompletionError(error);
    }

    protected void notifySuccessResponse(Primitive response) {
        onResponseOk(response);
        mTransManager.endClientTransaction(this);
        notifyAsyncCompletionSuccess();
    }

    public abstract void onResponseError(ImpsErrorInfo error);
    public abstract void onResponseOk(Primitive response);

    protected void notifyAsyncCompletionError(ImErrorInfo error) {
        if (!mCompletionNotified) {
            mCompletionNotified = true;
            if (mCompletionCallback != null)
                mCompletionCallback.onError(error);
        }
    }

    protected void notifyAsyncCompletionSuccess() {
        if (!mCompletionNotified) {
            mCompletionNotified = true;
            if (mCompletionCallback != null)
                mCompletionCallback.onComplete();
        }
    }
}
