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
 * A primitive is the basic packet sent between the IMPS server and the IMPS
 * client. Note that this class is not thread-safe.
 */
public final class Primitive {
    private TransactionMode mTransactionMode = TransactionMode.Request;
    private String mTransactionId;

    private String mSessionId;

    private String mPoll;
    private String mCir;

    private PrimitiveElement mContentElement;

    /**
     * Constructs a new Primitive with default value.
     */
    public Primitive() {
    }

    /**
     * Constructs a new Primitive with a type.
     *
     * @param type the type of the primitive.
     */
    public Primitive(String type) {
        mContentElement = new PrimitiveElement(type);
    }

    /**
     * Gets the session type of this primitive.
     *
     * @return the session type .
     */
    public SessionType getSessionType() {
        return mSessionId == null ? SessionType.Outband : SessionType.Inband;
    }

    /**
     * Gets the session ID of this primitive.
     *
     * @return the session ID.
     */
    public String getSessionId() {
        return mSessionId;
    }

    /**
     * Sets the session ID of this primitive.
     *
     * @param sessionId the session ID.
     */
    public void setSession(String sessionId) {
        this.mSessionId = sessionId;
    }

    /**
     * Gets the transaction mode of this primitive.
     *
     * @return the transaction mode.
     */
    public TransactionMode getTransactionMode() {
        return mTransactionMode;
    }

    /**
     * Sets the transaction mode of this primitive.
     *
     * @param mode the transaction mode.
     */
    public void setTransactionMode(TransactionMode mode) {
        this.mTransactionMode = mode;
    }

    /**
     * Gets the transaction ID of this primitive.
     *
     * @return the transaction ID.
     */
    public String getTransactionID() {
        return mTransactionId;
    }

    /**
     * Sets the transaction ID of this primitive.
     * @param transId the transaction ID.
     */
    public void setTransactionId(String transId) {
        this.mTransactionId = transId;
    }

    public void setTransaction(ImpsTransaction transaction) {
        this.mTransactionId = transaction.getId();
    }

    public String getCir() {
        return mCir;
    }

    public void setCir(String cir) {
        this.mCir = cir;
    }

    public String getPoll() {
        return mPoll;
    }

    public void setPoll(String poll) {
        this.mPoll = poll;
    }

    public String getType() {
        return (mContentElement == null) ? null : mContentElement.getTagName();
    }

    public PrimitiveElement getContentElement() {
        return mContentElement;
    }

    public void setContentElement(String type) {
        mContentElement = new PrimitiveElement(type);
    }

    public PrimitiveElement addElement(String tag) {
        return mContentElement.addChild(tag);
    }

    public void addElement(String tag, String value) {
        mContentElement.addChild(tag, value);
    }

    public void addElement(String tag, boolean value) {
        mContentElement.addChild(tag, value);
    }

    public void addElement(PrimitiveElement elem) {
        mContentElement.addChild(elem);
    }

    public PrimitiveElement getElement(String tag) {
        return mContentElement.getChild(tag);
    }

    public String getElementContents(String tag) {
        PrimitiveElement elem = getElement(tag);
        return elem == null ? null : elem.getContents();
    }

    PrimitiveElement createMessage(String versionUri, String transactUri) {
        PrimitiveElement root = new PrimitiveElement(ImpsTags.WV_CSP_Message);

        root.setAttribute(ImpsTags.XMLNS, versionUri);
        PrimitiveElement sessionElem = root.addChild(ImpsTags.Session);

        PrimitiveElement sessionDescElem = sessionElem.addChild(
                ImpsTags.SessionDescriptor);
        sessionDescElem.addChild(ImpsTags.SessionType,
                getSessionType().toString());
        if (getSessionId() != null) {
            sessionDescElem.addChild(ImpsTags.SessionID, getSessionId());
        }

        PrimitiveElement transElem = sessionElem.addChild(ImpsTags.Transaction);

        PrimitiveElement transDescElem = transElem.addChild(
                ImpsTags.TransactionDescriptor);
        transDescElem.addChild(ImpsTags.TransactionMode,
                getTransactionMode().toString());
        if (getTransactionID() != null) {
            transDescElem.addChild(ImpsTags.TransactionID, getTransactionID());
        }

        PrimitiveElement transContentElem = transElem.addChild(
                ImpsTags.TransactionContent);
        transContentElem.setAttribute(ImpsTags.XMLNS, transactUri);
        transContentElem.addChild(getContentElement());

        return root;
    }

    /**
     * Represents the transaction mode of a primitive.
     */
    public static enum TransactionMode {
        /**
         * Indicates the primitive is a request in a transaction.
         */
        Request,

        /**
         * Indicates the primitive is a response in a transaction.
         */
        Response
    }

    /**
     * Represents the session type of a primitive.
     */
    public static enum SessionType {
        /**
         * Indicates a primitive is sent within a session.
         */
        Inband,

        /**
         * Indicates a primitive is sent without a session.
         */
        Outband
    }
}
