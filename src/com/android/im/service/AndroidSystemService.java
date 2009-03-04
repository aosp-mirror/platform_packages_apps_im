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
package com.android.im.service;

import android.content.Context;

import com.android.im.engine.HeartbeatService;
import com.android.im.engine.SmsService;
import com.android.im.engine.SystemService;

public class AndroidSystemService extends SystemService {
    private static AndroidSystemService sInstance;

    private AndroidSystemService() {
    }

    public static AndroidSystemService getInstance() {
        if (sInstance == null) {
            sInstance = new AndroidSystemService();
        }
        return sInstance;
    }

    private Context mContext;
    private AndroidHeartBeatService mHeartbeatServcie;
    private AndroidSmsService mSmsService;

    public void initialize(Context context) {
        mContext = context;
    }

    public void shutdown() {
        if (mHeartbeatServcie != null) {
            mHeartbeatServcie.stopAll();
        }
        if (mSmsService != null) {
            mSmsService.stop();
        }
    }

    @Override
    public HeartbeatService getHeartbeatService() {
        if(mContext == null) {
            throw new IllegalStateException("Hasn't been initialized yet");
        }
        if (mHeartbeatServcie == null) {
            mHeartbeatServcie = new AndroidHeartBeatService(mContext);
        }
        return mHeartbeatServcie;
    }

    @Override
    public SmsService getSmsService() {
        if(mContext == null) {
            throw new IllegalStateException("Hasn't been initialized yet");
        }
        if (mSmsService == null) {
            mSmsService = new AndroidSmsService(mContext);
        }
        return mSmsService;
    }

}
