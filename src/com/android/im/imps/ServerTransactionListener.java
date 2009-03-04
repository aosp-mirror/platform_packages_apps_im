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
 * An IMPS transaction is a basic communication mechanism between an IMPS client
 * and an IMPS server. It may originate from either IMPS client or IMPS server.
 */
interface ServerTransactionListener {
    /**
     * Called when a new transaction originated from the IMPS server received.
     *
     * @param transation the new transaction.
     */
    public void notifyServerTransaction(ServerTransaction transaction);
}
