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

import com.android.im.engine.ImException;

/**
 * The protocol binding in data channel might be WSP, HTTP and HTTPS which are
 * asymmetric. In this case when the server needs to start transaction, it has
 * to send a communication initiation request message through the CIR channel to
 * the client in order to request an immediate PollingRequest message from the
 * client to the server on the data channel.
 */
abstract class CirChannel {
    protected ImpsConnection mConnection;

    protected CirChannel(ImpsConnection connection) {
        mConnection = connection;
    }

    /**
     * Establishes the connection to the server if the protocol is connection
     * oriented (e.g. as TCP)and starts to listen to CIR requests from the
     * server.
     * @throws Exception
     */
    public abstract void connect() throws ImException;

    /**
     * Re-establish the connection and drop the old one.
     */
    public void reconnect(){
    }

    /**
     * Tells if the CIR has been shutdown or not.
     */
    public abstract boolean isShutdown();

    /**
     * Shutdown the CIR channel, stops to listen to CIR requests from the server.
     *
     */
    public abstract void shutdown();
}
