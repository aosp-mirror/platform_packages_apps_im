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
package com.android.im.imps;

import com.android.im.engine.ImException;
import com.android.im.plugin.IPresenceMapping;
import com.android.im.plugin.ImPluginConstants;

import dalvik.system.PathClassLoader;

import android.os.RemoteException;

import java.util.Map;

public class CustomPresenceMapping implements PresenceMapping {
    private IPresenceMapping mPresenceMapping;

    public CustomPresenceMapping(String pluginPath, String implClass) throws ImException {
        PathClassLoader classLoader = new PathClassLoader(pluginPath,
                getClass().getClassLoader());
        try {
            Class cls = classLoader.loadClass(implClass);
            mPresenceMapping = (IPresenceMapping)cls.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ImException(e);
        } catch (IllegalAccessException e) {
            throw new ImException(e);
        } catch (InstantiationException e) {
            throw new ImException(e);
        }
    }

    public Map<String, Object> getExtra(int status) {
        try {
            return mPresenceMapping.getExtra(status);
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean getOnlineStatus(int status) {
        try {
            return mPresenceMapping.getOnlineStatus(status);
        } catch (RemoteException e) {
            return false;
        }
    }

    public int getPresenceStatus(boolean onlineStatus, String userAvailability,
            Map<String, Object> allValues) {
        try {
            return mPresenceMapping.getPresenceStatus(onlineStatus, userAvailability, allValues);
        } catch (RemoteException e) {
            return ImPluginConstants.PRESENCE_OFFLINE;
        }
    }

    public int[] getSupportedPresenceStatus() {
        try {
            return mPresenceMapping.getSupportedPresenceStatus();
        } catch (RemoteException e) {
            return new int[0];
        }
    }

    public String getUserAvaibility(int status) {
        try {
            return mPresenceMapping.getUserAvaibility(status);
        } catch (RemoteException e) {
            return ImPluginConstants.PA_NOT_AVAILABLE;
        }
    }

    public boolean requireAllPresenceValues() {
        try {
            return mPresenceMapping.requireAllPresenceValues();
        } catch (RemoteException e) {
            return false;
        }
    }

}
