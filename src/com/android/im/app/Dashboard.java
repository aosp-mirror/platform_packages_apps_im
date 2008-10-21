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

import com.android.im.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Im;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class Dashboard extends LinearLayout implements Gallery.OnItemClickListener {
    private Gallery mGallery;

    private Cursor mChats;

    private long mAccountId;
    private String mUserName;
    Activity mActivity;

    public Dashboard(Context screen, AttributeSet attrs) {
        super(screen, attrs);
    }

    public static final void openDashboard(Activity parent, long accountId, String username) {
        LayoutInflater inflate = LayoutInflater.from(parent);
        View v = inflate.inflate(R.layout.dashboard, null);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
            PixelFormat.TRANSLUCENT);

        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.token = parent.getWindow().peekDecorView().getWindowToken();

        WindowManagerImpl.getDefault().addView(v, lp);

        ((Dashboard) v).init(parent, accountId, username);
    }

    public final void init(Activity activity, long accountId, String username) {
        mActivity = activity;
        mAccountId = accountId;
        mUserName = username;
        mGallery = (Gallery) findViewById(R.id.chats_gallery);

        mGallery.setEmptyView(findViewById(R.id.empty));

        ContentResolver cr = mContext.getContentResolver();

        mChats = cr.query(Im.Contacts.CONTENT_URI_CHAT_CONTACTS, null, null, null, null);
        mGallery.setAdapter(new DashboardAdapter(mContext, mChats));
        mGallery.setSelection(getInitialPosition());
        mGallery.setOnItemClickListener(this);
        mGallery.requestFocus();
    }

    public final void init(Activity activity) {
        init(activity, -1L, null);
    }

    @Override
    public final boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            closeDashboard();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    void closeDashboard() {
        WindowManagerImpl.getDefault().removeView(this);
        if (mChats != null) {
            mChats.deactivate();
        }
    }

    private int getInitialPosition() {
        if ((mAccountId == -1) || (mUserName == null)) {
            return -1;
        }

        int usernameColumn = mChats.getColumnIndexOrThrow(Im.Contacts.USERNAME);
        int accountColumn = mChats.getColumnIndexOrThrow(Im.Contacts.ACCOUNT);

        mChats.moveToPosition(-1);
        while (mChats.moveToNext()) {
            if ((mAccountId == mChats.getLong(accountColumn))
                    && mUserName.equals(mChats.getString(usernameColumn))) {
                return mChats.getPosition();
            }
        }
        return -1;
    }

    private class DashboardAdapter extends ResourceCursorAdapter {
        public DashboardAdapter(Context context, Cursor c) {
            super(context, R.layout.dashboard_item, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor c) {
            long providerId = c.getLong(c.getColumnIndexOrThrow(Im.Contacts.PROVIDER));
            String nickname = c.getString(c.getColumnIndexOrThrow(Im.Contacts.NICKNAME));
            TextView t = (TextView) view.findViewById(R.id.name);

            t.setText(nickname);

            ImageView i = (ImageView) view.findViewById(R.id.presence);
            int presenceMode = c.getInt(c.getColumnIndexOrThrow(Im.Contacts.PRESENCE_STATUS));
            ImApp app = ImApp.getApplication(mActivity);
            BrandingResources brandingRes = app.getBrandingResource(providerId);
            Drawable presenceIcon = brandingRes.getDrawable(
                    PresenceUtils.getStatusIconId(presenceMode));
            i.setImageDrawable(presenceIcon);

            setAvatar(c, view);
        }

        private void setAvatar(Cursor c, View v) {
            ImageView i = (ImageView) v.findViewById(R.id.avatar);

            int avatarDataColumn = c.getColumnIndexOrThrow(Im.Contacts.AVATAR_DATA);
            Drawable avatar = DatabaseUtils.getAvatarFromCursor(c, avatarDataColumn);

            if (avatar == null) {
                setBitmapResource(i, R.drawable.avatar_unknown);
            } else {
                i.setImageDrawable(avatar);
            }
        }

        private final void setBitmapResource(ImageView i, int r) {
            Resources res = mContext.getResources();

            i.setImageDrawable(res.getDrawable(r));
        }
    }

    public final void onItemClick(AdapterView parent, View view, int position, long id) {
        Cursor c  = (Cursor) mGallery.getItemAtPosition(position);
        String contact = c.getString(c.getColumnIndexOrThrow(Im.Contacts.USERNAME));
        long account = c.getLong(c.getColumnIndexOrThrow(Im.Contacts.ACCOUNT));

        if ((account == mAccountId) && contact.equals(mUserName)) {
            closeDashboard();
            return;
        }

        Intent intent;
        Uri uri = ContentUris.withAppendedId(Im.Chats.CONTENT_URI, id);
        intent = new Intent(Intent.ACTION_VIEW, uri);

        closeDashboard();
        mActivity.finish();
        mActivity.startActivity(intent);
    }
}
