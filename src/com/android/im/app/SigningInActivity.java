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
import com.android.im.IConnectionListener;
import com.android.im.IImConnection;
import com.android.im.R;
import com.android.im.app.adapter.ConnectionListenerAdapter;
import com.android.im.engine.ImConnection;
import com.android.im.engine.ImErrorInfo;
import com.android.im.plugin.BrandingResourceIDs;
import com.android.im.service.ImServiceConstants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Handler;
import android.provider.Im;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;

public class SigningInActivity extends Activity {
    private IImConnection mConn;
    private IConnectionListener mListener;
    private SimpleAlertHandler mHandler;
    private ImApp mApp;
    private String mToAddress;

    protected static final int ID_CANCEL_SIGNIN = Menu.FIRST + 1;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        //setTheme(android.R.style.Theme_Dialog);
        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.signing_in_activity);
        Intent intent = getIntent();
        mToAddress = intent.getStringExtra(ImApp.EXTRA_INTENT_SEND_TO_USER);

        Uri data = intent.getData();
        if (data == null) {
            if(Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                log("Need account data to sign in");
            }
            finish();
            return;
        }
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(data, null, null, null, null);
        if (c == null) {
            if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                log("Query fail:" + data);
            }
            finish();
            return;
        }
        if (!c.moveToFirst()) {
            if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                log("No data for " + data);
            }
            c.close();
            finish();
            return;
        }

        long providerId = c.getLong(c.getColumnIndexOrThrow(Im.Account.PROVIDER));
        final long accountId = c.getLong(c.getColumnIndexOrThrow(Im.Account._ID));
        final String username = c.getString(c.getColumnIndexOrThrow(Im.Account.USERNAME));
        String pwExtra = intent.getStringExtra(ImApp.EXTRA_INTENT_PASSWORD);
        final String pw = pwExtra != null ? pwExtra
                : c.getString(c.getColumnIndexOrThrow(Im.Account.PASSWORD));

        c.close();
        mApp = ImApp.getApplication(this);
        final ProviderDef provider = mApp.getProvider(providerId);

        BrandingResources brandingRes = mApp.getBrandingResource(providerId);
        getWindow().setFeatureDrawable(Window.FEATURE_LEFT_ICON,
                brandingRes.getDrawable(BrandingResourceIDs.DRAWABLE_LOGO));

        setTitle(getResources().getString(R.string.signing_in_to,
                provider.mFullName));

        ImageView splash = (ImageView)findViewById(R.id.splashscr);
        splash.setImageDrawable(brandingRes.getDrawable(
                BrandingResourceIDs.DRAWABLE_SPLASH_SCREEN));

        mHandler = new SimpleAlertHandler(this);
        mListener = new MyConnectionListener(mHandler, provider.mName);

        mApp.callWhenServiceConnected(mHandler, new Runnable() {
            public void run() {
                signInAccount(provider, accountId, username, pw);
            }
        });

        // assume we can sign in successfully.
        setResult(RESULT_OK);
    }

    void signInAccount(ProviderDef provider, long accountId,
            String username, String pw) {
        try {
            IImConnection conn = mApp.getConnection(provider.mId);
            if (conn != null) {
                mConn = conn;
                // register listener before get state so that we won't miss
                // any state change event.
                conn.registerConnectionListener(mListener);
                int state = conn.getState();
                if (state != ImConnection.LOGGING_IN) {
                    // already signed in or failed
                    conn.unregisterConnectionListener(mListener);
                    handleConnectionEvent(provider.mName, state, null);
                }
            } else {
                mConn = mApp.createConnection(provider.mId);
                mConn.registerConnectionListener(mListener);
                mConn.login(accountId, username, pw, true);
            }
        } catch (RemoteException e) {
            mHandler.showServiceErrorAlert();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (mApp != null) {
            mApp.removePendingCall(mHandler);
        }
        if (mConn != null) {
            try {
                if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                    log("unregisterConnectonListener");
                }
                mConn.unregisterConnectionListener(mListener);
            } catch (RemoteException e) {
                Log.w(ImApp.LOG_TAG, "<SigningInActivity> Connection disappeared!");
            }
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, ID_CANCEL_SIGNIN, 0, R.string.menu_cancel_signin);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == ID_CANCEL_SIGNIN) {
            if (mConn != null) {
                try {
                    if (mConn.getState() == ImConnection.LOGGING_IN) {
                        if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                            log("Cancelling sign in");
                        }
                        mConn.logout();
                        finish();
                    }
                } catch (RemoteException e) {
                    Log.w(ImApp.LOG_TAG, "<SigningInActivity> Connection disappeared!");
                }
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    void handleConnectionEvent(String serviceName, int state, ImErrorInfo error) {
        if (isFinishing()) {
            return;
        }

        if (state == ImConnection.LOGGED_IN) {
            // sign in successfully, finish and switch to contact list
            finish();
            try {
                Intent intent;
                if (mToAddress != null) {
                    IChatSessionManager manager = mConn.getChatSessionManager();
                    IChatSession session = manager.getChatSession(mToAddress);
                    if(session == null) {
                        session = manager.createChatSession(mToAddress);
                    }
                    Uri data = ContentUris.withAppendedId(Im.Chats.CONTENT_URI,
                            session.getId());
                    intent = new Intent(Intent.ACTION_VIEW, data);
                } else {
                    intent = new Intent(this, ContactListActivity.class);
                    intent.putExtra(ImServiceConstants.EXTRA_INTENT_ACCOUNT_ID,
                            mConn.getAccountId());
                }
                startActivity(intent);
            } catch (RemoteException e) {
                // Ouch!  Service died!  We'll just disappear.
                Log.w(ImApp.LOG_TAG, "<SigningInActivity> Connection disappeared while signing in!");
            }
        } else if (state == ImConnection.DISCONNECTED) {
            // sign in failed
            Resources r = getResources();
            new AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(r.getString(R.string.login_service_failed, serviceName,
                            error == null? "": ErrorResUtils.getErrorRes(r, error.getCode())))
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                setResult(RESULT_CANCELED);
                                finish();
                            }
                        })
                .setCancelable(false)
                .show();
        }
    }

    private static final void log(String msg) {
        Log.d(ImApp.LOG_TAG, "<SigningInActivity>" + msg);
    }

    private final class MyConnectionListener extends ConnectionListenerAdapter {
        private final String mServiceName;

        MyConnectionListener(Handler handler, String serviceName) {
            super(handler);
            mServiceName = serviceName;
        }

        @Override
        public void onConnectionStateChange(IImConnection connection,
                int state, ImErrorInfo error) {
            handleConnectionEvent(mServiceName, state, error);
        }
    }
}
