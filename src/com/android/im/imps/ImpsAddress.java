/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

import android.os.Parcel;

import com.android.im.engine.Address;
import com.android.im.engine.ImEntity;

/**
 * The abstract representation of an IMPS address.
 */
public abstract class ImpsAddress extends Address {
    private static final char[] SPECIALS = {'/', '@', '+'};

    protected String mUser;
    protected String mResource;
    protected String mDomain;

    private String mFullAddress;

    protected ImpsAddress() {
    }

    protected ImpsAddress(String user, String resource, String domain) {
        // build the full address and unify the fields to lower case since imps
        // address is case insensitive.
        StringBuilder buf = new StringBuilder(ImpsConstants.ADDRESS_PREFIX);
        if(user != null) {
            buf.append(user.toLowerCase());
        }
        if(resource != null) {
            buf.append('/').append(resource.toLowerCase());
        }
        if(domain != null) {
            buf.append('@').append(domain.toLowerCase());
        }
        mFullAddress = buf.toString();
        mUser = user;
        mResource = resource;
        mDomain = domain;
        verifyAddress();
    }

    protected ImpsAddress(String full){
        this(full, true);
    }

    protected ImpsAddress(String full, boolean verify) {
        if (full == null || full.length() == 0) {
            throw new IllegalArgumentException();
        }

        //TODO: escape reserved characters.
        if(!full.startsWith(ImpsConstants.ADDRESS_PREFIX)) {
            full = ImpsConstants.ADDRESS_PREFIX + full;
        }

        parse(full);
        mFullAddress = full;

        if (verify) {
            verifyAddress();
        }
    }

    private void parse(String full) {
        mUser = parseUser(full);
        mResource = parseResource(full);
        mDomain = parseDomain(full);
    }

    private void verifyAddress() throws IllegalArgumentException {
        ImpsLog.log("verifyAddress:" + mUser + ", " + mResource + ",  " + mDomain);
        if(mUser == null && mResource == null) {
            throw new IllegalArgumentException();
        }

        if(mUser != null) {
            if(mUser.length() == 0) {
                throw new IllegalArgumentException("Invalid user");
            }
            if(mUser.charAt(0) == '+') {//mobile number
                for(int i = 1; i < mUser.length(); i++) {
                    if(!Character.isDigit(mUser.charAt(i))) {
                        throw new IllegalArgumentException("Invalid user");
                    }
                }
            } else if(!isAlphaSequence(mUser)) {
                throw new IllegalArgumentException("Invalid user");
            }
        }

        if(mResource != null && !isAlphaSequence(mResource)) {
            throw new IllegalArgumentException("Invalid resource");
        }
        if(mDomain != null && !isAlphaSequence(mDomain)) {
            throw new IllegalArgumentException("Invalid domain");
        }
    }

    /**
     * Gets the User-part of the address which refers to the identification of
     * the IMPS user.
     *
     * @return the User-part of the address.
     */
    public String getUser() {
        return mUser;
    }

    /**
     * Gets the Resource-part of the address which identifies the referred
     * public or private resource.
     *
     * @return the Resource-part of the address.
     */
    public String getResource() {
        return mResource;
    }

    /**
     * Gets the Domain-part of the address which identifies the IMPS server
     * domain.
     *
     * @return the Domain-part of the address.
     */
    public String getDomain() {
        return mDomain;
    }

    /**
     * Gets the full string representation of the address.
     */
    @Override
    public String getFullName() {
        return mFullAddress;
    }

    @Override
    public void writeToParcel(Parcel dest) {
        dest.writeString(mFullAddress);
    }

    @Override
    public void readFromParcel(Parcel source) {
        mFullAddress = source.readString();
        parse(mFullAddress);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ImpsAddress
                && mFullAddress.equalsIgnoreCase(((ImpsAddress)other).mFullAddress);
    }

    @Override
    public int hashCode() {
        return mFullAddress.toLowerCase().hashCode();
    }

    /**
     * Formats the address to a PrimitiveElement which can be used as the
     * content of Sender or Recipient.
     *
     * @return a PrimitiveElement.
     */
    public abstract PrimitiveElement toPrimitiveElement();

    /**
     * Gets the entity this address object refers to.
     *
     * @param connection
     * @return the entity this address refers to or <code>null</code> if the
     *         entity not found.
     */
    abstract ImEntity getEntity(ImpsConnection connection);

    /**
     * Constructs an ImpsAddress from the Sender or Recipient element in a
     * primitive.
     *
     * @param elem the Sender of Recipient element.
     * @return the ImpsAddress object or <code>null</code> if it's not a valid
     *         element.
     */
    public static ImpsAddress fromPrimitiveElement(PrimitiveElement elem) {
        String type = elem.getTagName();
        if(ImpsTags.User.equals(type)) {
            return new ImpsUserAddress(elem.getChildContents(ImpsTags.UserID), false);
        } else if(ImpsTags.Group.equals(type)) {
            PrimitiveElement child = elem.getFirstChild();
            if(child == null) {
                throw new IllegalArgumentException();
            }
            if(ImpsTags.GroupID.equals(child.getTagName())){
                return new ImpsGroupAddress(child.getContents());
            } else {
                String screeName = child.getChildContents(ImpsTags.SName);
                String groupId = child.getChildContents(ImpsTags.GroupID);
                return new ImpsGroupAddress(groupId, screeName);
            }
        } else if(ImpsTags.ContactList.equals(type)) {
            return new ImpsContactListAddress(elem.getContents(), false);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private String parseUser(String full) {
        int index = full.indexOf('/');
        if(index == 3) {
            return null;
        }
        if (index == -1) {
            index = full.lastIndexOf('@');
        }
        if (index == -1) {
            index = full.length();
        }
        return full.substring(3, index);
    }

    private String parseResource(String full) {
        int beginIndex = full.indexOf('/');
        if (beginIndex == -1) {
            return null;
        }
        int endIndex = full.lastIndexOf('@');
        if (endIndex == -1 || endIndex < beginIndex) {
            endIndex = full.length();
        }
        return full.substring(beginIndex + 1, endIndex);
    }

    private String parseDomain(String full) {
        int beginIndex = full.lastIndexOf('@');
        return beginIndex == -1 ? null : full.substring(beginIndex + 1);
    }

    private boolean isAlphaSequence(String str) {
        for(int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if(ch < 32 || ch > 126 || isSpecial(ch)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSpecial(char ch) {
        for (char element : SPECIALS) {
            if(ch == element) {
                return true;
            }
        }
        return false;
    }
}
