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

package com.android.im.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkConnectivityListener;
import android.net.Uri;
import android.net.NetworkConnectivityListener.State;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Im;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.im.IConnectionCreationListener;
import com.android.im.IImConnection;
import com.android.im.IRemoteImService;
import com.android.im.engine.ConnectionFactory;
import com.android.im.engine.ImConnection;
import com.android.im.engine.ImException;
import com.android.im.imps.ImpsConnectionConfig;
import com.android.im.imps.TcpCirAlarmService;
import com.android.im.imps.ImpsConnectionConfig.CirMethod;
import com.android.im.plugin.IImPlugin;
import com.android.im.plugin.ImConfigNames;
import com.android.im.plugin.ImPluginConstants;
import com.android.im.plugin.ImPluginInfo;
import com.android.im.plugin.ImpsConfigNames;
import dalvik.system.PathClassLoader;

public class RemoteImService extends Service {

    private static final String[] ACCOUNT_PROJECTION = {
        Im.Account._ID,
        Im.Account.PROVIDER,
        Im.Account.USERNAME,
        Im.Account.PASSWORD,
    };
    private static final int ACCOUNT_ID_COLUMN = 0;
    private static final int ACCOUNT_PROVIDER_COLUMN = 1;
    private static final int ACCOUNT_USERNAME_COLUMN = 2;
    private static final int ACCOUNT_PASSOWRD_COLUMN = 3;

    static final String TAG = "ImService";

    private static final int EVENT_SHOW_TOAST = 100;
    private static final int EVENT_NETWORK_STATE_CHANGED = 200;

    private StatusBarNotifier mStatusBarNotifier;
    private Handler mServiceHandler;
    NetworkConnectivityListener mNetworkConnectivityListener;
    private int mNetworkType;
    private boolean mNeedCheckAutoLogin;

    Vector<ImConnectionAdapter> mConnections;

    private PendingIntent mTcpCirAlarmSender;

    private HashMap<Long, ImPluginInfo> mPlugins;

    public RemoteImService() {
        mConnections = new Vector<ImConnectionAdapter>();
        mPlugins = new HashMap<Long, ImPluginInfo>();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "ImService started");
        mStatusBarNotifier = new StatusBarNotifier(this);
        mServiceHandler = new ServiceHandler();
        mNetworkConnectivityListener = new NetworkConnectivityListener();
        mNetworkConnectivityListener.registerHandler(mServiceHandler, EVENT_NETWORK_STATE_CHANGED);
        mNetworkConnectivityListener.startListening(this);

        findAvaiablePlugins();
    }

    private void findAvaiablePlugins() {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> plugins = pm.queryIntentServices(
                new Intent(ImPluginConstants.PLUGIN_ACTION_NAME), PackageManager.GET_META_DATA);
        for (ResolveInfo info : plugins) {
            Log.d(TAG, "Found plugin " + info);

            ServiceInfo serviceInfo = info.serviceInfo;
            if (serviceInfo == null) {
                Log.e(TAG, "Ignore bad IM plugin: " + info);
                continue;
            }
            String providerName = null;
            String providerFullName = null;
            String signUpUrl = null;
            Bundle metaData = serviceInfo.metaData;
            if (metaData != null) {
                providerName = metaData.getString(ImPluginConstants.METADATA_PROVIDER_NAME);
                providerFullName = metaData.getString(ImPluginConstants.METADATA_PROVIDER_FULL_NAME);
                signUpUrl = metaData.getString(ImPluginConstants.METADATA_SIGN_UP_URL);
            }
            if (TextUtils.isEmpty(providerName) || TextUtils.isEmpty(providerFullName)) {
                Log.e(TAG, "Ignore bad IM plugin: " + info + ". Lack of required meta data");
                continue;
            }

            ImPluginInfo pluginInfo = new ImPluginInfo(providerName, serviceInfo.packageName,
                    serviceInfo.name, serviceInfo.applicationInfo.sourceDir);

            Map<String, String> config = loadProviderConfigFromPlugin(pluginInfo);
            if (config == null) {
                Log.e(TAG, "Ignore bad IM plugin");
                break;
            }

            config.put(ImConfigNames.PLUGIN_PATH, pluginInfo.mSrcPath);
            config.put(ImConfigNames.PLUGIN_CLASS, pluginInfo.mClassName);
            long providerId = updateProviderDb(providerName, providerFullName, signUpUrl,
                    config);
            mPlugins.put(providerId, pluginInfo);
        }
    }

    private Map<String, String> loadProviderConfigFromPlugin(ImPluginInfo pluginInfo) {
        // XXX Load the plug-in implementation directly from the apk rather than
        // binding to the service and call through IPC Binder API. This is much
        // more effective since we don't need to start the service in other
        // process. We can not run the plug-in service in the same process as a
        // local service because that the interface is defined in a shared
        // library in order to compile the plug-in separately. In this case, the
        // interface will be loaded by two class loader separately and a
        // ClassCastException will be thrown if we cast the binder to the
        // interface.
        PathClassLoader loader = new PathClassLoader(pluginInfo.mSrcPath, getClassLoader());
        try {
            Class cls = loader.loadClass(pluginInfo.mClassName);
            Method m = cls.getMethod("onBind", Intent.class);
            IImPlugin plugin = (IImPlugin)m.invoke(cls.newInstance(), new Object[]{null});
            return plugin.getProviderConfig();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Could not find plugin class", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Could not create plugin instance", e);
        } catch (InstantiationException e) {
            Log.e(TAG, "Could not create plugin instance", e);
        } catch (SecurityException e) {
            Log.e(TAG, "Could not load config from the plugin", e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Could not load config from the plugin", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Could not load config from the plugin", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Could not load config from the plugin", e);
        } catch (RemoteException e) {
            Log.e(TAG, "Could not load config from the plugin", e);
        }
        return null;
    }

    private long updateProviderDb(String providerName, String providerFullName,
            String signUpUrl, Map<String, String> config) {
        long providerId;
        ContentResolver cr = getContentResolver();
        String where = Im.Provider.NAME + "=?";
        String[] selectionArgs = new String[]{providerName};
        Cursor c = cr.query(Im.Provider.CONTENT_URI, null, where, selectionArgs, null);
        if (c == null) {
            return -1;
        }
        if (c.moveToFirst()) {
            providerId = c.getLong(c.getColumnIndexOrThrow(Im.Provider._ID));
        } else {
            ContentValues values = new ContentValues(3);
            values.put(Im.Provider.NAME, providerName);
            values.put(Im.Provider.FULLNAME, providerFullName);
            values.put(Im.Provider.SIGNUP_URL, signUpUrl);

            Uri result = cr.insert(Im.Provider.CONTENT_URI, values);
            providerId = ContentUris.parseId(result);
            ContentValues[] settingValues = new ContentValues[config.size()];

            int index = 0;
            for (Map.Entry<String, String> entry : config.entrySet()) {
                ContentValues settingValue = new ContentValues();
                settingValue.put(Im.ProviderSettings.PROVIDER, providerId);
                settingValue.put(Im.ProviderSettings.NAME, entry.getKey());
                settingValue.put(Im.ProviderSettings.VALUE, entry.getValue());
                settingValues[index++] = settingValue;
            }
            cr.bulkInsert(Im.ProviderSettings.CONTENT_URI, settingValues);
        }
        c.close();

        return providerId;
    }

    public void startTcpCirAlarm() {
        if (mTcpCirAlarmSender != null) {
            return;
        }

        mTcpCirAlarmSender = PendingIntent.getService(this, 0,
                new Intent(this, TcpCirAlarmService.class), 0);
        long firstTime = SystemClock.elapsedRealtime();
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
                TcpCirAlarmService.INTERVAL, mTcpCirAlarmSender);
    }

    public void stopTcpCirAlarm() {
        if (mTcpCirAlarmSender != null) {
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.cancel(mTcpCirAlarmSender);
            mTcpCirAlarmSender = null;
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mNeedCheckAutoLogin = intent.getBooleanExtra(ImServiceConstants.EXTRA_CHECK_AUTO_LOGIN, false);

        Log.d(TAG, "ImService.onStart, checkAutoLogin=" + mNeedCheckAutoLogin);

        // Check and login accounts if network is ready, otherwise it's checked
        // when the network becomes available.
        if (mNeedCheckAutoLogin &&
                mNetworkConnectivityListener.getState() == State.CONNECTED) {
            mNeedCheckAutoLogin = false;
            autoLogin();
        }
    }

    private void autoLogin() {
        Log.d(TAG, "Scaning accounts and login automatically");

        ContentResolver resolver = getContentResolver();

        String where = Im.Account.KEEP_SIGNED_IN + "=1 AND " + Im.Account.ACTIVE + "=1";
        Cursor cursor = resolver.query(Im.Account.CONTENT_URI,
                ACCOUNT_PROJECTION, where, null, null);
        if (cursor == null) {
            Log.w(TAG, "Can't query account!");
            return;
        }
        while (cursor.moveToNext()) {
            long accountId = cursor.getLong(ACCOUNT_ID_COLUMN);
            long providerId = cursor.getLong(ACCOUNT_PROVIDER_COLUMN);
            String username = cursor.getString(ACCOUNT_USERNAME_COLUMN);
            String password = cursor.getString(ACCOUNT_PASSOWRD_COLUMN);

            IImConnection conn = createConnection(providerId);

            try {
                conn.login(accountId, username, password, true);
            } catch (RemoteException e) {
                Log.w(TAG, "Logging error while automatically login!");
            }
        }
        cursor.close();
    }

    private Map<String, String> loadProviderSettings(long providerId) {
        ContentResolver cr = getContentResolver();
        Map<String, String> settings = Im.ProviderSettings.queryProviderSettings(cr, providerId);

        NetworkInfo networkInfo = mNetworkConnectivityListener.getNetworkInfo();
        // Insert a fake msisdn on emulator. We don't need this on device
        // because the mobile network will take care of it.
        if ("1".equals(SystemProperties.get("ro.kernel.qemu"))) {
            settings.put(ImpsConfigNames.MSISDN, "1231231234");
        } else if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            // Wi-Fi network won't insert a MSISDN, we should get from the SIM
            // card. Assume we can always get the correct MSISDN from SIM, otherwise,
            // the sign in would fail and an error message should be shown to warn
            // the user to contact their operator.
            String msisdn = TelephonyManager.getDefault().getLine1Number();
            if (!TextUtils.isEmpty(msisdn)) {
                settings.put(ImpsConfigNames.MSISDN, msisdn);
            } else {
                // TODO: This should be removed. We can't fetch phone number from
                // the test T-Mobile SIMs. Use a fake phone number so that we can
                // work with our test SIMs right now. This can't happen with T-Mobile
                // production SIMs
                Log.w(TAG, "Can not get phone number from SIM, use a fake one");
                settings.put(ImpsConfigNames.MSISDN, "1231231234");
            }
        }
        return settings;
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "ImService stopped.");
        for (ImConnectionAdapter conn : mConnections) {
            conn.logout();
        }
        mNetworkConnectivityListener.unregisterHandler(mServiceHandler);
        mNetworkConnectivityListener.stopListening();
        mNetworkConnectivityListener = null;
        stopTcpCirAlarm();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void showToast(CharSequence text, int duration) {
        Message msg = Message.obtain(mServiceHandler, EVENT_SHOW_TOAST, duration, 0, text);
        msg.sendToTarget();
    }

    public StatusBarNotifier getStatusBarNotifier() {
        return mStatusBarNotifier;
    }

    public void scheduleReconnect(long delay) {
        if (!isNetworkAvailable()) {
            // Don't schedule reconnect if no network available. We will try to
            // reconnect when network state become CONNECTED.
            return;
        }
        mServiceHandler.postDelayed(new Runnable() {
            public void run() {
                reestablishConnections();
            }
        }, delay);
    }

    IImConnection createConnection(long providerId) {
        Map<String, String> settings = loadProviderSettings(providerId);
        String protocol = settings.get(ImConfigNames.PROTOCOL_NAME);
        if(!"IMPS".equals(protocol)) {
            Log.e(TAG, "Unsupported protocol: " + protocol);
            return null;
        }
        ImpsConnectionConfig config = new ImpsConnectionConfig(settings);
        ConnectionFactory factory = ConnectionFactory.getInstance();
        try {
            ImConnection conn = factory.createConnection(config);
            ImConnectionAdapter result = new ImConnectionAdapter(providerId,
                    conn, this);
            mConnections.add(result);
            if (config.getCirChannelBinding() == CirMethod.STCP) {
                startTcpCirAlarm();
            }
            mListenerMgr.notifyConnectionCreated(result);
            return result;
        } catch (ImException e) {
            Log.e(TAG, "Error creating connection", e);
            return null;
        }
    }

    void removeConnection(IImConnection connection) {
        mConnections.remove(connection);
    }

    private boolean isNetworkAvailable() {
        return mNetworkConnectivityListener.getState() == State.CONNECTED;
    }

    void networkStateChanged() {
        if (mNetworkConnectivityListener == null) {
            return;
        }
        NetworkInfo networkInfo = mNetworkConnectivityListener.getNetworkInfo();
        NetworkInfo.State state = networkInfo.getState();

        Log.d(TAG, "networkStateChanged:" + state);

        int oldType = mNetworkType;
        mNetworkType = networkInfo.getType();

	// Notify the connection that network type has changed. Note that this
	// only work for connected connections, we need to reestablish if it's
	// suspended.
        if (mNetworkType != oldType
                && isNetworkAvailable()) {
            for (ImConnectionAdapter conn : mConnections) {
                conn.networkTypeChanged();
            }
        }

        switch (state) {
            case CONNECTED:
                if (mNeedCheckAutoLogin) {
                    mNeedCheckAutoLogin = false;
                    autoLogin();
                    break;
                }
                reestablishConnections();
                break;

            case DISCONNECTED:
                if (!isNetworkAvailable()) {
                    suspendConnections();
                }
                break;
        }
    }

    // package private for inner class access
    void reestablishConnections() {
        if (!isNetworkAvailable()) {
            return;
        }

        for (ImConnectionAdapter conn : mConnections) {
            int connState = conn.getState();
            if (connState == ImConnection.SUSPENDED) {
                conn.reestablishSession();
            }
        }
    }

    private void suspendConnections() {
        for (ImConnectionAdapter conn : mConnections) {
            if (conn.getState() != ImConnection.LOGGED_IN) {
                continue;
            }
            conn.suspend();
        }
    }

    private final IRemoteImService.Stub mBinder = new IRemoteImService.Stub() {

        public List getAllPlugins() {
            return new ArrayList(mPlugins.values());
        }

        public void addConnectionCreatedListener(IConnectionCreationListener listener) {
            mListenerMgr.addRemoteListener(listener);

        }

        public void removeConnectionCreatedListener(IConnectionCreationListener listener) {
            mListenerMgr.removeRemoteListener(listener);
        }

        public IImConnection createConnection(long providerId) {
            return RemoteImService.this.createConnection(providerId);
        }

        public List getActiveConnections() {
            ArrayList<IBinder> result = new ArrayList<IBinder>(mConnections.size());
            for(IImConnection conn : mConnections) {
                result.add(conn.asBinder());
            }
            return result;
        }

        public void dismissNotifications(long providerId) {
            mStatusBarNotifier.dismissNotifications(providerId);
        }

    };

    final ConnectionListenerManager mListenerMgr = new ConnectionListenerManager();

    private final static class ConnectionListenerManager
            extends RemoteListenerManager<IConnectionCreationListener> {
        public ConnectionListenerManager(){
        }

        public void notifyConnectionCreated(final ImConnectionAdapter conn) {
            notifyRemoteListeners(new ListenerInvocation<IConnectionCreationListener>() {
                public void invoke(IConnectionCreationListener remoteListener)
                        throws RemoteException {
                    remoteListener.onConnectionCreated(conn);
                }
            });
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_SHOW_TOAST:
                    Toast.makeText(RemoteImService.this,
                            (CharSequence) msg.obj, msg.arg1).show();
                    break;

                case EVENT_NETWORK_STATE_CHANGED:
                    networkStateChanged();
                    break;

                default:
            }
        }
    }
}
