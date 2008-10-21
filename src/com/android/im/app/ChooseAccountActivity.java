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

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Im;
import android.util.Log;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.CursorAdapter;

import com.android.im.IImConnection;
import com.android.im.R;
import com.android.im.engine.ImConnection;
import com.android.im.plugin.BrandingResourceIDs;
import com.android.im.service.ImServiceConstants;

public class ChooseAccountActivity extends ListActivity implements
        View.OnCreateContextMenuListener {

    private static final int ID_SIGN_IN = Menu.FIRST + 1;
    private static final int ID_SIGN_OUT = Menu.FIRST + 2;
    private static final int ID_EDIT_ACCOUNT = Menu.FIRST + 3;
    private static final int ID_REMOVE_ACCOUNT = Menu.FIRST + 4;
    private static final int ID_SIGN_OUT_ALL = Menu.FIRST + 5;
    private static final int ID_ADD_ACCOUNT = Menu.FIRST + 6;
    private static final int ID_VIEW_CONTACT_LIST = Menu.FIRST + 7;
    private static final int ID_SETTINGS = Menu.FIRST + 8;

    ImApp mApp;

    MyHandler mHandler;
    private ProviderAdapter mAdapter;
    private Cursor mProviderCursor;

    private static final String[] PROVIDER_PROJECTION = {
        Im.Provider._ID,
        Im.Provider.NAME,
        Im.Provider.FULLNAME,
        Im.Provider.ACTIVE_ACCOUNT_ID,
        Im.Provider.ACTIVE_ACCOUNT_USERNAME,
        Im.Provider.ACTIVE_ACCOUNT_PW,
    };

    static final int PROVIDER_ID_COLUMN = 0;
    static final int PROVIDER_NAME_COLUMN = 1;
    static final int PROVIDER_FULLNAME_COLUMN = 2;
    static final int ACTIVE_ACCOUNT_ID_COLUMN = 3;
    static final int ACTIVE_ACCOUNT_USERNAME_COLUMN = 4;
    static final int ACTIVE_ACCOUNT_PW_COLUMN = 5;


    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setTitle(R.string.choose_account_title);

        mApp = ImApp.getApplication(this);

        mHandler = new MyHandler();
        mApp.registerForBroadcastEvent(ImApp.EVENT_SERVICE_CONNECTED, mHandler);
        mApp.registerForConnEvents(mHandler);

        mApp.startImServiceIfNeed();

        // (for open source) for now exclude GTalk on the landing page until we can load
        // it in an abstract way
        String selection = "providers.name != ?";
        String[] selectionArgs = new String[] { Im.ProviderNames.GTALK };
        mProviderCursor = managedQuery(Im.Provider.CONTENT_URI_WITH_ACCOUNT,
                PROVIDER_PROJECTION,
                selection,
                selectionArgs,
                Im.Provider.DEFAULT_SORT_ORDER);
        mAdapter = new ProviderAdapter(this, mProviderCursor);
        mApp.callWhenServiceConnected(mHandler, new Runnable() {
            public void run() {
                setListAdapter(mAdapter);
            }
        });
        registerForContextMenu(getListView());
    }

    private boolean allAccountsSignedOut() {
        if (!mProviderCursor.moveToFirst()) return true;

        do {
            long accountId = mProviderCursor.getLong(ACTIVE_ACCOUNT_ID_COLUMN);
            if (isSignedIn(accountId)) return false;
        } while (mProviderCursor.moveToNext()) ;

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(ID_SIGN_OUT_ALL).setVisible(!allAccountsSignedOut());
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, ID_SIGN_OUT_ALL, 0, R.string.menu_sign_out_all)
                .setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ID_SIGN_OUT_ALL:
                // Sign out MSN/AIM/YAHOO account
                if (mApp.serviceConnected()) {
                    for (IImConnection conn : mApp.getActiveConnections()) {
                        try {
                            conn.logout();
                        } catch (RemoteException e) {
                        }
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(ImApp.LOG_TAG, "bad menuInfo", e);
            return;
        }

        Cursor providerCursor = (Cursor) getListAdapter().getItem(info.position);
        menu.setHeaderTitle(providerCursor.getString(PROVIDER_FULLNAME_COLUMN));

        if (providerCursor.isNull(ACTIVE_ACCOUNT_ID_COLUMN)) {
            menu.add(0, ID_ADD_ACCOUNT, 0, R.string.menu_add_account);
            return;
        }
        long providerId = providerCursor.getLong(PROVIDER_ID_COLUMN);
        long accountId = providerCursor.getLong(ACTIVE_ACCOUNT_ID_COLUMN);
        boolean isLoggingIn = isSigningIn(accountId);
        boolean isLoggedIn = isSignedIn(accountId);

        if (!isLoggedIn) {
            menu.add(0, ID_SIGN_IN, 0, R.string.sign_in)
                .setIcon(R.drawable.ic_menu_login);
        } else {
            BrandingResources brandingRes = mApp.getBrandingResource(providerId);
            menu.add(0, ID_VIEW_CONTACT_LIST, 0,
                    brandingRes.getString(BrandingResourceIDs.STRING_MENU_CONTACT_LIST));
            menu.add(0, ID_SIGN_OUT, 0, R.string.menu_sign_out)
                .setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        }

        if (!isLoggingIn && !isLoggedIn) {
            menu.add(0, ID_EDIT_ACCOUNT, 0, R.string.menu_edit_account)
                .setIcon(android.R.drawable.ic_menu_edit);
            menu.add(0, ID_REMOVE_ACCOUNT, 0, R.string.menu_remove_account)
                .setIcon(android.R.drawable.ic_menu_delete);
        }

        // always add a settings menu item
        menu.add(0, ID_SETTINGS, 0, R.string.menu_settings);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(ImApp.LOG_TAG, "bad menuInfo", e);
            return false;
        }
        int position = info.position;
        long providerId = info.id;

        switch (item.getItemId()) {
        case ID_EDIT_ACCOUNT:
        {
            Cursor c = (Cursor)mAdapter.getItem(position);
            if (c != null) {
                Intent i = new Intent(ChooseAccountActivity.this, AccountActivity.class);
                i.setAction(Intent.ACTION_EDIT);
                i.setData(ContentUris.withAppendedId(Im.Account.CONTENT_URI,
                        c.getLong(ACTIVE_ACCOUNT_ID_COLUMN)));
                c.close();
                startActivity(i);
            }
            return true;
        }

        case ID_REMOVE_ACCOUNT:
        {
            Cursor c = (Cursor)mAdapter.getItem(position);
            if (c != null) {
                long accountId = c.getLong(ACTIVE_ACCOUNT_ID_COLUMN);
                Uri accountUri = ContentUris.withAppendedId(Im.Account.CONTENT_URI, accountId);
                getContentResolver().delete(accountUri, null, null);
                // Requery the cursor to force refreshing screen
                c.requery();
            }
            return true;
        }

        case ID_VIEW_CONTACT_LIST:
        case ID_ADD_ACCOUNT:
        case ID_SIGN_IN:
            Intent i = mAdapter.intentForPosition(position);
            if (i != null) {
                startActivity(i);
            }
            return true;

        case ID_SIGN_OUT:
            // TODO: progress bar
            IImConnection conn = mApp.getConnection(providerId);
            if (conn != null) {
                try {
                    conn.logout();
                } catch (RemoteException e) {
                }
            }
            return true;

        case ID_SETTINGS:
            Intent settingsIntent = mAdapter.settingsIntentForPosition(position);
            if (settingsIntent != null) {
                startActivity(settingsIntent);
            }
            return true;

        }

        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        startActivityAtPosition(position);
    }

    void startActivityAtPosition(int position) {
        Intent i = mAdapter.intentForPosition(position);
        if (i != null) {
            startActivity(i);
        }
    }

    static void log(String msg) {
        Log.d(ImApp.LOG_TAG, "[ChooseAccount]" + msg);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        mApp.startImServiceIfNeed();
        mApp.registerForConnEvents(mHandler);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mApp.unregisterForConnEvents(mHandler);
        mApp.unregisterForBroadcastEvent(ImApp.EVENT_SERVICE_CONNECTED, mHandler);
        mApp.stopImServiceIfInactive();
    }

    boolean isSigningIn(long accountId) {
        IImConnection conn = mApp.getConnectionByAccount(accountId);
        try {
            return (conn == null) ? false : (conn.getState() == ImConnection.LOGGING_IN);
        } catch (RemoteException e) {
            return false;
        }
    }

    boolean isSignedIn(long accountId) {
        try {
            IImConnection conn = mApp.getConnectionByAccount(accountId);
            if (conn == null) {
                return false;
            }
            int state = conn.getState();
            return state == ImConnection.LOGGED_IN || state == ImConnection.SUSPENDED;
        } catch (RemoteException e) {
            return false;
        }
    }



    private final class MyHandler extends SimpleAlertHandler {
        public MyHandler() {
            super(ChooseAccountActivity.this);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case ImApp.EVENT_CONNECTION_DISCONNECTED:
                    promptDisconnectedEvent(msg);
                // fall through
                case ImApp.EVENT_SERVICE_CONNECTED:
                case ImApp.EVENT_CONNECTION_CREATED:
                case ImApp.EVENT_CONNECTION_LOGGING_IN:
                case ImApp.EVENT_CONNECTION_LOGGED_IN:
                    getListView().invalidateViews();
                    return;
            }
            super.handleMessage(msg);
        }
    }

    private class ProviderListItemFactory implements LayoutInflater.Factory {
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            if (name != null && name.equals(ProviderListItem.class.getName())) {
                return new ProviderListItem(context, ChooseAccountActivity.this);
            }
            return null;
        }
    }

    private final class ProviderAdapter extends CursorAdapter {
        private LayoutInflater mInflater;

        public ProviderAdapter(Context context, Cursor c) {
            super(context, c);
            mInflater = LayoutInflater.from(context).cloneInContext(context);
            mInflater.setFactory(new ProviderListItemFactory());
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // create a custom view, so we can manage it ourselves. Mainly, we want to
            // initialize the widget views (by calling getViewById()) in newView() instead of in
            // bindView(), which can be called more often.
            ProviderListItem view = (ProviderListItem) mInflater.inflate(
                    R.layout.account_view, parent, false);
            view.init(cursor);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((ProviderListItem) view).bindView(cursor);
        }

        public Intent intentForPosition(int position) {
            Intent intent = null;

            if (mCursor == null) {
                return null;
            }

            mCursor.moveToPosition(position);
            long providerId = mCursor.getLong(PROVIDER_ID_COLUMN);

            if (mCursor.isNull(ACTIVE_ACCOUNT_ID_COLUMN)) {
                // add account
                intent = new Intent(ChooseAccountActivity.this, AccountActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
                intent.setData(ContentUris.withAppendedId(Im.Provider.CONTENT_URI, providerId));
            } else {
                long accountId = mCursor.getLong(ACTIVE_ACCOUNT_ID_COLUMN);

                IImConnection conn = mApp.getConnection(providerId);
                int state = getConnState(conn);
                if (state < ImConnection.LOGGED_IN ) {
                    Uri accountUri = ContentUris.withAppendedId(Im.Account.CONTENT_URI, accountId);
                    if (mCursor.isNull(ACTIVE_ACCOUNT_PW_COLUMN)) {
                        // no password, edit the account
                        intent = new Intent(ChooseAccountActivity.this, AccountActivity.class);
                        intent.setAction(Intent.ACTION_EDIT);
                        intent.setData(accountUri);
                    } else {
                        // intent for sign in
                        intent = new Intent(ChooseAccountActivity.this, SigningInActivity.class);
                        intent.setData(accountUri);
                    }
                } else if (state == ImConnection.LOGGED_IN || state == ImConnection.SUSPENDED) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClass(ChooseAccountActivity.this, ContactListActivity.class);
                    intent.putExtra(ImServiceConstants.EXTRA_INTENT_ACCOUNT_ID, accountId);
                }
            }
            return intent;
        }

        public Intent settingsIntentForPosition(int position) {
            Intent intent = null;

            if (mCursor == null) {
                return null;
            }

            mCursor.moveToPosition(position);
            Long providerId = mCursor.getLong(PROVIDER_ID_COLUMN);
            intent = new Intent(ChooseAccountActivity.this, SettingActivity.class);
            intent.putExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, providerId);

            return intent;
        }

        private int getConnState(IImConnection conn) {
            try {
                return conn == null ? ImConnection.DISCONNECTED : conn.getState();
            } catch (RemoteException e) {
                return ImConnection.DISCONNECTED;
            }
        }
    }
}
