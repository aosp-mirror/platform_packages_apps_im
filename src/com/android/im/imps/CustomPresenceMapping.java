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

import java.util.Map;

import android.os.RemoteException;

import com.android.im.engine.ImException;
import com.android.im.plugin.ImPluginConstants;
import com.android.im.plugin.PresenceMapping;

import dalvik.system.PathClassLoader;

public class CustomPresenceMapping implements PresenceMapping {
    private PresenceMapping mPresenceMapping;

    public CustomPresenceMapping(String pluginPath, String implClass) throws ImException {
        PathClassLoader classLoader = new PathClassLoader(pluginPath,
                getClass().getClassLoader());
        try {
            Class<?> cls = classLoader.loadClass(implClass);
            mPresenceMapping = (PresenceMapping)cls.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ImException(e);
        } catch (IllegalAccessException e) {
            throw new ImException(e);
        } catch (InstantiationException e) {
            throw new ImException(e);
        }
    }

    public Map<String, Object> getExtra(int status) {
        return mPresenceMapping.getExtra(status);
    }

    public boolean getOnlineStatus(int status) {
        return mPresenceMapping.getOnlineStatus(status);
    }

    public int getPresenceStatus(boolean onlineStatus, String userAvailability,
            Map<String, Object> allValues) {
        return mPresenceMapping.getPresenceStatus(onlineStatus, userAvailability, allValues);
    }

    public int[] getSupportedPresenceStatus() {
        return mPresenceMapping.getSupportedPresenceStatus();
    }

    public String getUserAvaibility(int status) {
        return mPresenceMapping.getUserAvaibility(status);
    }

    public boolean requireAllPresenceValues() {
        return mPresenceMapping.requireAllPresenceValues();
    }

}
