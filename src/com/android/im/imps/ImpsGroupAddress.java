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

import android.os.Parcel;

import com.android.im.engine.ChatGroup;
import com.android.im.engine.ImEntity;

public class ImpsGroupAddress extends ImpsAddress{
    private String mScreenName;

    /**
     * Default constructor. Required by AddressParcelHelper.
     */
    public ImpsGroupAddress() {
    }

    public ImpsGroupAddress(String groupId) {
        this(groupId, null);
    }

    public ImpsGroupAddress(ImpsAddress userAddress, String groupName) {
        super(userAddress.getUser(), groupName, userAddress.getDomain());
        if(mResource == null) {
            throw new IllegalArgumentException();
        }
    }

    public ImpsGroupAddress(String groupId, String screenName) {
        super(groupId);
        if(mResource == null) {
            throw new IllegalArgumentException();
        }
        mScreenName = screenName;
    }

    @Override
    public String getScreenName() {
        return mScreenName == null ? getResource() : mScreenName;
    }

    @Override
    public void writeToParcel(Parcel dest) {
        super.writeToParcel(dest);
        dest.writeString(mScreenName);
    }

    @Override
    public void readFromParcel(Parcel source) {
        super.readFromParcel(source);
        mScreenName = source.readString();
    }

    @Override
    public PrimitiveElement toPrimitiveElement() {
        PrimitiveElement group = new PrimitiveElement(ImpsTags.Group);
        group.addChild(ImpsTags.GroupID, getFullName());
        return group;
    }

    @Override
    ImEntity getEntity(ImpsConnection connection) {
        ImpsChatGroupManager manager =
            (ImpsChatGroupManager) connection.getChatGroupManager();
        ChatGroup group = manager.getChatGroup(this);
        if(group == null) {
            group = manager.loadGroupMembersAsync(this);
        }
        return group;
    }
}
