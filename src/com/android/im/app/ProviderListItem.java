/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.im.app;

import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.Im;
import android.view.View;
import android.os.RemoteException;
import com.android.im.engine.Presence;
import com.android.im.plugin.BrandingResourceIDs;
import com.android.im.IImConnection;
import com.android.im.R;

public class ProviderListItem extends LinearLayout {
    private ImApp mApp;
    private ChooseAccountActivity mActivity;
    private ImageView mProviderIcon;
    private ImageView mStatusIcon;
    private TextView mLine1;
    private TextView mLine2;
    private TextView mChatView;
    private int mProviderIdColumn;
    private int mProviderNameColumn;
    private int mProviderFullnameColumn;
    private int mActiveAccountIdColumn;
    private int mActiveAccountUserNameColumn;

    public ProviderListItem(Context context, ChooseAccountActivity activity) {
        super(context);
        mActivity = activity;
        mApp = ImApp.getApplication(activity);
    }

    public void init(Cursor c) {
        mProviderIcon = (ImageView) findViewById(R.id.providerIcon);
        mStatusIcon = (ImageView) findViewById(R.id.statusIcon);
        mLine1 = (TextView) findViewById(R.id.line1);
        mLine2 = (TextView) findViewById(R.id.line2);
        mChatView = (TextView) findViewById(R.id.conversations);

        mProviderIdColumn = c.getColumnIndexOrThrow(Im.Provider._ID);
        mProviderNameColumn = c.getColumnIndexOrThrow(Im.Provider.NAME);
        mProviderFullnameColumn = c.getColumnIndexOrThrow(Im.Provider.FULLNAME);
        mActiveAccountIdColumn = c.getColumnIndexOrThrow(
                Im.Provider.ACTIVE_ACCOUNT_ID);
        mActiveAccountUserNameColumn = c.getColumnIndexOrThrow(
                Im.Provider.ACTIVE_ACCOUNT_USERNAME);
    }

    public void bindView(Cursor cursor) {
        Resources r = getResources();
        ImageView providerIcon = mProviderIcon;
        ImageView statusIcon = mStatusIcon;
        TextView line1 = mLine1;
        TextView line2 = mLine2;
        TextView chatView = mChatView;

        int providerId = cursor.getInt(mProviderIdColumn);
        String providerDisplayName = cursor.getString(mProviderFullnameColumn);

        BrandingResources brandingRes = mApp.getBrandingResource(providerId);
        providerIcon.setImageDrawable(
                brandingRes.getDrawable(BrandingResourceIDs.DRAWABLE_LOGO));

        if (!cursor.isNull(mActiveAccountIdColumn)) {
            line1.setVisibility(View.VISIBLE);
            line1.setText(r.getString(R.string.account_title, providerDisplayName));
            line2.setText(cursor.getString(mActiveAccountUserNameColumn));

            long accountId = cursor.getLong(mActiveAccountIdColumn);

            if (mActivity.isSigningIn(accountId)) {
                statusIcon.setVisibility(View.GONE);
                chatView.setVisibility(View.VISIBLE);
                chatView.setText(R.string.signing_in_wait);
            } else if (mActivity.isSignedIn(accountId)) {
                int presenceIconId = getPresenceIconId(accountId);
                statusIcon.setImageDrawable(
                        brandingRes.getDrawable(presenceIconId));
                statusIcon.setVisibility(View.VISIBLE);
                ContentResolver cr = mActivity.getContentResolver();
                int count = getConversationCount(cr, accountId);
                if (count > 0) {
                    chatView.setVisibility(View.VISIBLE);
                    if (count == 1) {
                        chatView.setText(R.string.one_conversation);
                    } else {
                        chatView.setText(r.getString(R.string.conversations, count));
                    }
                } else {
                    chatView.setVisibility(View.GONE);
                }
            } else {
                statusIcon.setVisibility(View.GONE);
                chatView.setVisibility(View.GONE);
            }
        } else {
            // No active account, show add account
            line1.setVisibility(View.GONE);
            statusIcon.setVisibility(View.GONE);
            chatView.setVisibility(View.GONE);

            line2.setText(providerDisplayName);
        }
    }

    private int getConversationCount(ContentResolver cr, long accountId) {
        try {
            IImConnection conn = mApp.getConnectionByAccount(accountId);
            return (conn == null) ? 0 : conn.getChatSessionCount();
        } catch (RemoteException e) {
            return 0;
        }
    }

    private int getPresenceIconId(long accountId) {
        try {
            IImConnection conn = mApp.getConnectionByAccount(accountId);
            if (conn != null) {
                Presence p = conn.getUserPresence();
                if (p != null) {
                    int status = PresenceUtils.convertStatus(p.getStatus());
                    return PresenceUtils.getStatusIconId(status);
                }
            }
            return BrandingResourceIDs.DRAWABLE_PRESENCE_OFFLINE;
        } catch (RemoteException e) {
            return BrandingResourceIDs.DRAWABLE_PRESENCE_INVISIBLE;
        }
    }
}
