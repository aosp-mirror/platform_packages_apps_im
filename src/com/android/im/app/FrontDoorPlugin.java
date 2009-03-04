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

import com.android.im.plugin.ImPluginConstants;

import android.app.Service;
import android.content.Intent;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.im.IImPlugin;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Bundle;
import android.provider.Im;
import android.util.Log;
import android.text.TextUtils;
import android.database.Cursor;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import dalvik.system.PathClassLoader;


public class FrontDoorPlugin extends Service {
    private final static String TAG = ImApp.LOG_TAG;
    private final static boolean LOCAL_DEBUG = false;

    // database access constants for branding resource map cache table
    private final static String[] BRANDING_RESOURCE_MAP_CACHE_PROJECTION = {
        Im.BrandingResourceMapCache.PROVIDER_ID,
        Im.BrandingResourceMapCache.APP_RES_ID,
        Im.BrandingResourceMapCache.PLUGIN_RES_ID
    };
    private final static int BRANDING_RESOURCE_MAP_CACHE_PROVIDER_ID_COLUMN = 0;
    private final static int BRANDING_RESOURCE_MAP_CACHE_APP_RES_ID_COLUMN = 1;
    private final static int BRANDING_RESOURCE_MAP_CACHE_PLUGIN_RES_ID_COLUMN = 2;

    private ArrayList<String> mProviderNames;
    private HashMap<String, String> mPackageNames;
    private HashMap<String, String> mClassNames;
    private HashMap<String, String> mSrcPaths;
    private HashMap<String, Map<Integer, Integer>> mBrandingResources;

    @Override
    public IBinder onBind(Intent intent) {
        // temporary provider ID<->Name mappings
        HashMap<String, Long> providerNameToId = new HashMap<String, Long>();
        HashMap<Long, String> providerIdToName = new HashMap<Long, String>();

        loadThirdPartyPlugins(providerNameToId, providerIdToName);
        loadBrandingResources(providerNameToId, providerIdToName);

        return mBinder;
    }

    private void loadThirdPartyPlugins(HashMap<String, Long> providerNameToId,
            HashMap<Long, String> providerIdToName) {
        mProviderNames = new ArrayList<String>();
        mPackageNames = new HashMap<String, String>();
        mClassNames = new HashMap<String, String>();
        mSrcPaths = new HashMap<String, String>();

        PackageManager pm = getPackageManager();
        List<ResolveInfo> plugins = pm.queryIntentServices(
                new Intent(ImPluginConstants.PLUGIN_ACTION_NAME), PackageManager.GET_META_DATA);
        for (ResolveInfo info : plugins) {
            if (LOCAL_DEBUG) log("loadThirdPartyPlugins: found plugin " + info);

            ServiceInfo serviceInfo = info.serviceInfo;
            if (serviceInfo == null) {
                Log.e(TAG, "loadThirdPartyPlugins: ignore bad plugin: " + info);
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

            mProviderNames.add(providerName);
            mPackageNames.put(providerName, serviceInfo.packageName);
            mClassNames.put(providerName, serviceInfo.name);
            mSrcPaths.put(providerName, serviceInfo.applicationInfo.sourceDir);

            long providerId = updateProviderDb(providerName, providerFullName, signUpUrl);
            providerNameToId.put(providerName, providerId);
            providerIdToName.put(providerId, providerName);
        }
    }

    private long updateProviderDb(
            String providerName, String providerFullName, String signUpUrl) {
        long providerId;
        ContentResolver cr = getContentResolver();
        String where = Im.Provider.NAME + "=?";
        String[] selectionArgs = new String[]{providerName};
        Cursor c = cr.query(Im.Provider.CONTENT_URI, null, where, selectionArgs, null);

        try {
            if (c.moveToFirst()) {
                providerId = c.getLong(c.getColumnIndexOrThrow(Im.Provider._ID));
                String origFullName = c.getString(
                        c.getColumnIndexOrThrow(Im.Provider.FULLNAME));
                String origCategory = c.getString(
                        c.getColumnIndexOrThrow(Im.Provider.CATEGORY));
                String origSignupUrl = c.getString(
                        c.getColumnIndexOrThrow(Im.Provider.SIGNUP_URL));
                ContentValues values = new ContentValues();
                if (origFullName == null || !origFullName.equals(providerFullName)) {
                    values.put(Im.Provider.FULLNAME, providerFullName);
                }
                if (origCategory == null) {
                    values.put(Im.Provider.CATEGORY, ImApp.IMPS_CATEGORY);
                }
                if (origSignupUrl == null || !origSignupUrl.equals(signUpUrl)) {
                    values.put(Im.Provider.SIGNUP_URL, signUpUrl);
                }
                if (values.size() > 0) {
                    Uri uri = ContentUris.withAppendedId(Im.Provider.CONTENT_URI, providerId);
                    cr.update(uri, values, null, null);
                }
            } else {
                ContentValues values = new ContentValues(3);
                values.put(Im.Provider.NAME, providerName);
                values.put(Im.Provider.FULLNAME, providerFullName);
                values.put(Im.Provider.CATEGORY, ImApp.IMPS_CATEGORY);
                values.put(Im.Provider.SIGNUP_URL, signUpUrl);

                Uri result = cr.insert(Im.Provider.CONTENT_URI, values);
                providerId = ContentUris.parseId(result);
            }
        } finally {
            c.close();
        }

        return providerId;
    }

    private void loadBrandingResources(HashMap<String, Long> providerNameToId,
            HashMap<Long, String> providerIdToName) {
        mBrandingResources = new HashMap<String, Map<Integer, Integer>>();

        // first try load from cache
        loadBrandingResourcesFromCache(providerIdToName);

        // check and load any un-cached resources
        ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();
        for (String provider : mProviderNames) {
            long providerId = providerNameToId.get(provider);
            if (!mBrandingResources.containsKey(provider)) {
                Map<Integer, Integer> resMap = loadBrandingResource(mClassNames.get(provider),
                        mSrcPaths.get(provider));
                if (resMap != null) {
                    mBrandingResources.put(provider, resMap);
                    for (int appResId : resMap.keySet()) {
                        int pluginResId = resMap.get(appResId);

                        ContentValues values = new ContentValues();
                        values.put(Im.BrandingResourceMapCache.PROVIDER_ID, providerId);
                        values.put(Im.BrandingResourceMapCache.APP_RES_ID, appResId);
                        values.put(Im.BrandingResourceMapCache.PLUGIN_RES_ID, pluginResId);

                        valuesList.add(values);
                    }
                    Log.d(TAG, "Plugin " + provider + " not in cache, loaded and saved");
                }
            }
        }

        // save the changes to cache
        int size = valuesList.size();
        if (size > 0) {
            getContentResolver().bulkInsert(
                    Im.BrandingResourceMapCache.CONTENT_URI,
                    valuesList.toArray(new ContentValues[size]));
        }
    }

    /**
     * Try loading the branding resources from the database.
     * @param providerIdToName a map between provider ID and name.
     */
    private void loadBrandingResourcesFromCache(HashMap<Long, String> providerIdToName) {
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(
                Im.BrandingResourceMapCache.CONTENT_URI, /* URI */
                BRANDING_RESOURCE_MAP_CACHE_PROJECTION,  /* projection */
                null,                                    /* where */
                null,                                    /* where args */
                null                                     /* sort */);

        if (c != null) {
            try {
                while (c.moveToNext()) {
                    long providerId = c.getLong(BRANDING_RESOURCE_MAP_CACHE_PROVIDER_ID_COLUMN);
                    String provider = providerIdToName.get(providerId);
                    if (TextUtils.isEmpty(provider)) {
                        Log.e(TAG, "Empty provider name in branding resource map cache table.");
                        continue;
                    }
                    int appResId = c.getInt(BRANDING_RESOURCE_MAP_CACHE_APP_RES_ID_COLUMN);
                    int pluginResId = c.getInt(BRANDING_RESOURCE_MAP_CACHE_PLUGIN_RES_ID_COLUMN);

                    Map<Integer, Integer> resMap = mBrandingResources.get(provider);
                    if (resMap == null) {
                        resMap = new HashMap<Integer, Integer>();
                        mBrandingResources.put(provider, resMap);
                    }

                    resMap.put(appResId, pluginResId);
                }
            } finally {
                c.close();
            }
        } else {
            Log.e(TAG, "Query of branding resource map cache table returns empty cursor"); 
        }
    }

    /**
     * Load branding resources from one plugin.
     */
    private Map<Integer, Integer> loadBrandingResource(String className, String srcPath) {
        Map retVal = null;

        if (LOCAL_DEBUG) log("loadBrandingResource: className=" + className +
                ", srcPath=" + srcPath);

        PathClassLoader classLoader = new PathClassLoader(srcPath,
                getCustomClassLoader());

        try {
            Class cls = classLoader.loadClass(className);
            Method m = cls.getMethod("getResourceMap");

            // TODO: this would still cause a VM verifier exception to be thrown if.
            // the landing page Android.mk and AndroidManifest.xml don't include use-library for
            // "com.android.im.plugin". This is even with getCustomClassLoader() as the parent
            // class loader.
            retVal = (Map)m.invoke(cls.newInstance(), new Object[]{});

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Failed load the plugin resource map", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Failed load the plugin resource map", e);
        } catch (InstantiationException e) {
            Log.e(TAG, "Failed load the plugin resource map", e);
        } catch (SecurityException e) {
            Log.e(TAG, "Failed load the plugin resource map", e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Failed load the plugin resource map", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed load the plugin resource map", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Failed load the plugin resource map", e);
        }

        return retVal;
    }

    private ClassLoader getCustomClassLoader() {
        /*
        // TODO: should not hard code the path!
        ClassLoader retVal = new PathClassLoader("/System/framework/com.android.im.plugin.jar",
                getClassLoader());
        if (LOCAL_DEBUG) log("getCustomClassLoader: " + retVal);
        return retVal;
        */
        return getClassLoader();
    }

    private void log(String msg) {
        Log.d(TAG, "[ImFrontDoor] " + msg);
    }


    /**
     * The implementation of IImFrontDoorPlugin defined through AIDL.
     */
    private final IImPlugin.Stub mBinder = new IImPlugin.Stub() {

        /**
         * Notify the plugin the front door activity is created. This gives the plugin a chance to
         * start its own servics, etc.
         */
        public void onStart() {
        }

        /**
         * Notify the plugin the front door activity is stopping.
         */
        public void onStop() {
        }

        /**
         * Sign in to the service for the account passed in.
         */
        public void signIn(long account) {
            if (LOCAL_DEBUG) log("signIn for account " + account);

            Intent intent = new Intent();
            intent.setData(ContentUris.withAppendedId(Im.Account.CONTENT_URI, account));
            intent.setClassName("com.android.im", "com.android.im.app.SigningInActivity");

            startActivity(intent);
        }

        /**
         * Sign out of the service for the account passed in.
         */
        public void signOut(long account) {
            if (LOCAL_DEBUG) log("signOut for account " + account);
            Intent intent = new Intent();
            intent.setData(ContentUris.withAppendedId(Im.Account.CONTENT_URI, account));
            intent.setClassName("com.android.im", "com.android.im.app.SignoutActivity");

            startActivity(intent);
        }

        public String getResourcePackageNameForProvider(String providerName) {
            return mPackageNames.get(providerName);
        }

        public Map getResourceMapForProvider(String providerName) throws RemoteException {
            return mBrandingResources.get(providerName);
        }

        public List getSupportedProviders() {
            return mProviderNames;
        }
    };

}
