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
 * The IMPS transport binding is divided into two channels: a mandatory data
 * channel and a conditional CIR channel. All the exchange of CSP primitives is
 * done in the data channel.
 */
abstract class DataChannel {
    protected ImpsConnection mConnection;
    protected PrimitiveParser mParser;
    protected PrimitiveSerializer mSerializer;
    protected long mMinPollMillis;

    protected DataChannel(ImpsConnection connection) throws ImException {
        mConnection = connection;
    }

    /**
     * Establishes a data channel with the IMPS server.
     *
     * @throws ImException if an error occur during establishing the data
     *             channel.
     */
    public abstract void connect() throws ImException;

    /**
     * Suspend the data channel. No data will be sent through the data channel
     * after suspended. It can be recovered from {@link #resume()}.
     */
    public abstract void suspend();

    /**
     * Resume the suspended data channel.
     *
     * @return <code>true</code> if the channel is resumed successfully;
     *         <code>false</code> if the channel is timeout and a new one must
     *         be established.
     */
    public abstract boolean resume();

    /**
     * Shutdown the data channel.
     */
    public abstract void shutdown();

    /**
     * Sends a CSP primitive to the IMPS server through this data channel.
     *
     * @param p the primitive to send.
     */
    public abstract void sendPrimitive(Primitive p);

    /**
     * Receives a primitive from this data channel, waiting until a primitive
     * from the server arrived or being interrupted.
     *
     * @return the received primitive
     * @throws InterruptedException
     */
    public abstract Primitive receivePrimitive() throws InterruptedException;

    /**
     * Gets the time when the last primitive was sent to the server through the
     * data channel.
     *
     * @return the time last primitive was sent.
     */
    public abstract long getLastActiveTime();

    /**
     * Tells if there is any primitive waiting to send.
     *
     * @return <code>true</code> if there is one or more primitives waiting to send.
     */
    public abstract boolean isSendingQueueEmpty();

    /**
     * Starts the keep alive task. KeepAliveRequest will be sent to the server
     * if no other transaction has occurred during the KeepAliveTime interval.
     */
    public abstract void startKeepAlive(long interval);

    /**
     * Set the ServerMinPoll value (in seconds) after capability negotiation.
     * The DataChannel <b>MUST NOT</b> send more than 1 PollingRequest within
     * this interval.
     */
    public void setServerMinPoll(long interval)
    {
        mMinPollMillis = interval * 1000;
    }
}
