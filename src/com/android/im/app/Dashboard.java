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
import com.android.im.plugin.BrandingResourceIDs;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.provider.Im;
import android.text.TextUtils;
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
    private static final String[] PROVIDER_CATEGORY_PROJECTION = new String[] {
            Im.Provider.CATEGORY
    };
    private static final int PROVIDER_CATEGORY_COLUMN = 0;

    private Gallery mGallery;
    private Cursor mChats;
    private long mAccountId;
    private String mUserName;
    Activity mActivity;

    private int mProviderIdColumn;
    private int mAccountIdColumn;
    private int mUsernameColumn;
    private int mNicknameColumn;
    private int mPresenceStatusColumn;
    private int mLastUnreadMessageColumn;
    private int mShortcutColumn;

    public Dashboard(Context screen, AttributeSet attrs) {
        super(screen, attrs);
    }

    public static final void openDashboard(Activity parent, long accountId, String username) {
        LayoutInflater inflate = LayoutInflater.from(parent);
        View v = inflate.inflate(R.layout.dashboard, null);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT);
        lp.dimAmount = .5F;

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

        Cursor c = cr.query(Im.Contacts.CONTENT_URI_CHAT_CONTACTS,
                null, null, null, null);

        mProviderIdColumn = c.getColumnIndexOrThrow(Im.Contacts.PROVIDER);
        mAccountIdColumn = c.getColumnIndexOrThrow(Im.Contacts.ACCOUNT);
        mUsernameColumn = c.getColumnIndexOrThrow(Im.Contacts.USERNAME);
        mNicknameColumn = c.getColumnIndexOrThrow(Im.Contacts.NICKNAME);
        mPresenceStatusColumn = c.getColumnIndexOrThrow(Im.Contacts.PRESENCE_STATUS);
        mLastUnreadMessageColumn = c.getColumnIndexOrThrow(Im.Chats.LAST_UNREAD_MESSAGE);
        mShortcutColumn = c.getColumnIndexOrThrow(Im.Chats.SHORTCUT);
        mChats = c;

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
        int code = event.getKeyCode();
        if (code == KeyEvent.KEYCODE_BACK) {
            closeDashboard();
            return true;
        }

        if (code >= KeyEvent.KEYCODE_0 && code <= KeyEvent.KEYCODE_9) {
            if (quickSwitch(mActivity, mChats, code - KeyEvent.KEYCODE_0)) {
                mActivity.finish();
                closeDashboard();
                return true;
            }
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

        mChats.moveToPosition(-1);
        while (mChats.moveToNext()) {
            if ((mAccountId == mChats.getLong(mAccountIdColumn))
                    && mUserName.equals(mChats.getString(mUsernameColumn))) {
                return mChats.getPosition();
            }
        }
        return -1;
    }

    private class DashboardAdapter extends ResourceCursorAdapter {
        private String mMenuPlus;

        public DashboardAdapter(Context context, Cursor c) {
            super(context, R.layout.dashboard_item, c);

            mMenuPlus = context.getString(R.string.menu_plus);
        }

        @Override
        public void bindView(View view, Context context, Cursor c) {

            long providerId = c.getLong(mProviderIdColumn);
            String nickname = c.getString(mNicknameColumn);
            TextView t = (TextView) view.findViewById(R.id.name);

            t.setText(nickname);

            ImageView i = (ImageView) view.findViewById(R.id.presence);
            int presenceMode = c.getInt(mPresenceStatusColumn);
            String lastUnreadMsg = c.getString(mLastUnreadMessageColumn);

            ImApp app = ImApp.getApplication(mActivity);
            BrandingResources brandingRes = app.getBrandingResource(providerId);
            if (!TextUtils.isEmpty(lastUnreadMsg)) {
                i.setImageDrawable(brandingRes.getDrawable(
                        BrandingResourceIDs.DRAWABLE_UNREAD_CHAT));
            } else {
                i.setImageDrawable(brandingRes.getDrawable(
                        PresenceUtils.getStatusIconId(presenceMode)));
            }

            String shortcut = c.getString(mShortcutColumn);
            TextView shortcutView = (TextView) view.findViewById(R.id.shortcut);
            if (TextUtils.isEmpty(shortcut)) {
                shortcutView.setVisibility(View.GONE);
            } else {
                shortcutView.setVisibility(View.VISIBLE);
                shortcutView.setText(mMenuPlus + shortcut);
            }

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
        String contact = c.getString(mUsernameColumn);
        long account = c.getLong(mAccountIdColumn);
        long provider = c.getLong(mProviderIdColumn);

        closeDashboard();

        if ((account == mAccountId) && contact.equals(mUserName)) {
            return;
        }

        mActivity.startActivity(
                makeChatIntent(mActivity.getContentResolver(), provider, account, contact, id));
        mActivity.finish();
    }

    private static String findCategory(ContentResolver resolver, long providerId) {
        // find the provider category for this chat
        Cursor providerCursor = resolver.query(
                Im.Provider.CONTENT_URI,
                PROVIDER_CATEGORY_PROJECTION,
                "_id = " + providerId,
                null /* selection args */,
                null /* sort order */
        );
        String category = null;

        try {
            if (providerCursor.moveToFirst()) {
                category = providerCursor.getString(PROVIDER_CATEGORY_COLUMN);
            }
        } finally {
            providerCursor.close();
        }

        return category;
    }

    public static boolean quickSwitch(Activity parent, Cursor cursor, int slot) {
        if (cursor == null) {
            return false;
        }

        if (slot < 0 || slot > 9) {
            return false;
        }

        int shortcutColumn = cursor.getColumnIndexOrThrow(Im.Chats.SHORTCUT);
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            int shortcut = cursor.getInt(shortcutColumn);
            if (shortcut == slot) {
                long provider = cursor.getLong(
                        cursor.getColumnIndexOrThrow(Im.Contacts.PROVIDER));
                long account = cursor.getLong(
                        cursor.getColumnIndexOrThrow(Im.Contacts.ACCOUNT));
                String username = cursor.getString(
                        cursor.getColumnIndexOrThrow(Im.Contacts.USERNAME));
                long chatId = cursor.getLong(
                        cursor.getColumnIndexOrThrow("_id"));

                ContentResolver cr = parent.getContentResolver();
                parent.startActivity(
                        makeChatIntent(cr, provider, account, username, chatId));

                return true;
            }
        }
        return false;
    }

    public static Intent makeChatIntent(ContentResolver resolver, long provider, long account,
            String contact, long chatId) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                ContentUris.withAppendedId(Im.Chats.CONTENT_URI, chatId));
        intent.addCategory(findCategory(resolver, provider));
        intent.putExtra("from", contact);
        intent.putExtra("providerId", provider);
        intent.putExtra("accountId", account);

        return intent;
    }
}
