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
 * A default implementation of AsyncTransaction for some simple transactions.
 * It might be convenient for a subclass to extend this class instead of
 * AsyncTransaction so it doesn't have to override both onResponseError and
 * onResponseOk but it could as well be missing either of them when doing so.
 * Therefore we make this class final to prevent that from happening.
 */
final class SimpleAsyncTransaction extends AsyncTransaction {

    public SimpleAsyncTransaction(ImpsTransactionManager manager,
            AsyncCompletion completion) {
        super(manager, completion);
        if (completion == null) {
            // Since SimpleAsyncTransaction does nothing in onResponseOk
            // and onResponseError, we must have a completion object to
            // be able to notify someone.
            throw new NullPointerException();
        }
    }

    @Override
    public void onResponseError(ImpsErrorInfo error) {
        // do nothing
    }

    @Override
    public void onResponseOk(Primitive response) {
        // do nothing
    }
}
