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

import com.android.im.IChatSession;
import com.android.im.IChatSessionManager;
import com.android.im.IImConnection;
import com.android.im.engine.ImConnection;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Im;
import android.text.TextUtils;
import android.util.Log;

import java.util.Iterator;
import java.util.Set;

public class ImUrlActivity extends Activity {
    private static final String[] ACCOUNT_PROJECTION = {
        Im.Account._ID,
        Im.Account.PASSWORD,
    };
    private static final int ACCOUNT_ID_COLUMN = 0;
    private static final int ACCOUNT_PW_COLUMN = 1;

    private String mProviderCategory;
    private String mToAddress;

    private ImApp mApp;
    private IImConnection mConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (Intent.ACTION_SENDTO.equals(intent.getAction())) {
            resolveIntent(intent);

            if (!isProviderSupported()) {
                Log.w(ImApp.LOG_TAG, "<ImUrlActivity>Unsuppported provider:" + mProviderCategory);
                finish();
                return;
            }

            if (TextUtils.isEmpty(mToAddress)) {
                Log.w(ImApp.LOG_TAG, "<ImUrlActivity>Invalid to address:" + mToAddress);
                finish();
                return;
            }
            mApp = ImApp.getApplication(this);
            mApp.callWhenServiceConnected(new Handler(), new Runnable(){
                public void run() {
                    handleIntent();
                }});

        } else {
            finish();
        }
    }

    void handleIntent() {
        ContentResolver cr = getContentResolver();
        String providername = Im.Provider.getProviderNameForCategory(mProviderCategory);
        long providerId = Im.Provider.getProviderIdForName(cr, providername);
        mConn= mApp.getConnection(providerId);
        if (mConn == null) {
            Cursor c = DatabaseUtils.queryAccountsForProvider(cr, ACCOUNT_PROJECTION, providerId);
            if (c == null) {
                addAccount(providerId);
            } else {
                long accountId = c.getLong(ACCOUNT_ID_COLUMN);
                if (c.isNull(ACCOUNT_PW_COLUMN)) {
                    editAccount(accountId);
                } else {
                    signInAccount(accountId);
                }
            }
        } else {
            try {
                int state = mConn.getState();
                if (state < ImConnection.LOGGED_IN) {
                    signInAccount(mConn.getAccountId());
                } else if (state == ImConnection.LOGGED_IN
                        || state == ImConnection.SUSPENDED) {
                    openChat();
                }
            } catch (RemoteException e) {
                // Ouch!  Service died!  We'll just disappear.
                Log.w("ImUrlActivity", "Connection disappeared!");
            }
        }
        finish();
    }

    private void addAccount(long providerId) {
        Intent  intent = new Intent(this, AccountActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        intent.setData(ContentUris.withAppendedId(Im.Provider.CONTENT_URI, providerId));
        intent.putExtra(ImApp.EXTRA_INTENT_SEND_TO_USER, mToAddress);
        startActivity(intent);
    }

    private void editAccount(long accountId) {
        Uri accountUri = ContentUris.withAppendedId(Im.Account.CONTENT_URI, accountId);
        Intent intent = new Intent(this, AccountActivity.class);
        intent.setAction(Intent.ACTION_EDIT);
        intent.setData(accountUri);
        intent.putExtra(ImApp.EXTRA_INTENT_SEND_TO_USER, mToAddress);
        startActivity(intent);
    }

    private void signInAccount(long accountId) {
        Uri accountUri = ContentUris.withAppendedId(Im.Account.CONTENT_URI, accountId);
        Intent intent = new Intent(this, SigningInActivity.class);
        intent.setData(accountUri);
        intent.putExtra(ImApp.EXTRA_INTENT_SEND_TO_USER, mToAddress);
        startActivity(intent);
    }

    private void openChat() {
        try {
            IChatSessionManager manager = mConn.getChatSessionManager();
            IChatSession session = manager.getChatSession(mToAddress);
            if(session == null) {
                session = manager.createChatSession(mToAddress);
            }

            Uri data = ContentUris.withAppendedId(Im.Chats.CONTENT_URI,
                    session.getId());
            Intent i = new Intent(Intent.ACTION_VIEW, data);
            startActivity(i);
        } catch (RemoteException e) {
            // Ouch!  Service died!  We'll just disappear.
            Log.w("ImUrlActivity", "Connection disappeared!");
        }
    }

    private void resolveIntent(Intent intent) {
        Set<String> categories = intent.getCategories();
        if (categories != null) {
            Iterator<String> iter = categories.iterator();
            if (iter.hasNext()) {
                mProviderCategory = iter.next();
            }
        }
        Uri data = intent.getData();
        mToAddress = data.getSchemeSpecificPart();

        if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
            log ("mProviderCategory=" + mProviderCategory + ", mToAddress=" + mToAddress);
        }
    }

    private boolean isProviderSupported() {
        return Im.ProviderCategories.AIM.equals(mProviderCategory)
                || Im.ProviderCategories.MSN.equals(mProviderCategory)
                || Im.ProviderCategories.YAHOO.equals(mProviderCategory);
    }

    private static void log(String msg) {
        Log.d(ImApp.LOG_TAG, "<ImUrlActivity> " + msg);
    }
}
