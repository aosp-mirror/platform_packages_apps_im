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
import com.android.im.plugin.IPasswordDigest;

import android.os.RemoteException;

import dalvik.system.PathClassLoader;

public class CustomPasswordDigest implements PasswordDigest {

    private IPasswordDigest mPasswordDigest;
    public CustomPasswordDigest(String pluginPath, String implClass) throws ImException {
        PathClassLoader classLoader = new PathClassLoader(pluginPath,
                getClass().getClassLoader());
        try {
            Class cls = classLoader.loadClass(implClass);
            mPasswordDigest = (IPasswordDigest)cls.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ImException(e);
        } catch (IllegalAccessException e) {
            throw new ImException(e);
        } catch (InstantiationException e) {
            throw new ImException(e);
        }
    }
    public String digest(String schema, String nonce, String password) throws ImException {
        try {
            return mPasswordDigest.digest(schema, nonce, password);
        } catch (RemoteException e) {
            throw new ImException(e);
        }
    }

    public String[] getSupportedDigestSchema() {
        try {
            return mPasswordDigest.getSupportedDigestSchema();
        } catch (RemoteException e) {
            return new String[0];
        }
    }

}
