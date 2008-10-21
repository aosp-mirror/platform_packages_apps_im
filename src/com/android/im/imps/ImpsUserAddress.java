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

import com.android.im.engine.Contact;
import com.android.im.engine.ContactList;
import com.android.im.engine.ContactListManager;
import com.android.im.engine.ImEntity;

public class ImpsUserAddress extends ImpsAddress{

    /**
     * Default Constructor. Required by AddressParcelHelper.
     */
    public ImpsUserAddress() {
    }

    public ImpsUserAddress(String full, boolean verify) {
        super(full, verify);
        if(verify && (mUser == null || mResource != null)) {
            throw new IllegalArgumentException();
        }
    }

    public ImpsUserAddress(String full) {
        this(full, false);
    }

    public ImpsUserAddress(String user, String domain) {
        super(user, null, domain);
    }

    @Override
    public PrimitiveElement toPrimitiveElement() {
        PrimitiveElement user = new PrimitiveElement(ImpsTags.User);
        user.addChild(ImpsTags.UserID, getFullName());
        return user;
    }

    @Override
    public String getScreenName() {
        return mUser;
    }

    @Override
    public ImEntity getEntity(ImpsConnection connection) {
        ContactListManager manager = connection.getContactListManager();
        for(ContactList list : manager.getContactLists()) {
            Contact contact = list.getContact(this);
            if(contact != null) {
                return contact;
            }
        }
        //TODO: add to a stranger list?
        return new Contact(this, this.getUser());
    }
}
