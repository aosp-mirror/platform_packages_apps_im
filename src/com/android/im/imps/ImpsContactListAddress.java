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

import com.android.im.engine.ContactList;
import com.android.im.engine.ContactListManager;
import com.android.im.engine.ImEntity;

public class ImpsContactListAddress extends ImpsAddress{

    /**
     * Default Constructor. Required by AddressParcelHelper.
     */
    public ImpsContactListAddress() {
    }

    public ImpsContactListAddress(ImpsAddress userAddress, String name) {
        super(userAddress.getUser(), name, userAddress.getDomain());
        if(mResource == null) {
            throw new IllegalArgumentException("resource can not be null");
        }
    }

    public ImpsContactListAddress(String full, boolean verify) {
        super(full, verify);
        if(mResource == null) {
            throw new IllegalArgumentException("resource can not be null");
        }
    }

    public ImpsContactListAddress(String full) {
        this(full, false);
    }

    @Override
    public PrimitiveElement toPrimitiveElement() {
        PrimitiveElement contactList = new PrimitiveElement(ImpsTags.ContactList);
        contactList.setContents(getFullName());
        return contactList;
    }

    @Override
    public String getScreenName() {
        return getResource();
    }

    @Override
    public ImEntity getEntity(ImpsConnection connection) {
        ContactListManager manager = connection.getContactListManager();
        for(ContactList list : manager.getContactLists()) {
            if(list.getAddress().equals(this)) {
                return list;
            }
        }
        return null;
    }

}
