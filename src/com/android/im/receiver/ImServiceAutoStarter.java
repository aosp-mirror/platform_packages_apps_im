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

package com.android.im.receiver;

import com.android.im.provider.Imps;
import com.android.im.service.ImServiceConstants;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.database.Cursor;
import android.util.Log;

public class ImServiceAutoStarter extends BroadcastReceiver {
    static final String TAG = "ImServiceAutoStarter";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Received intent only when the system boot is completed
        Log.d(TAG, "onReceiveIntent");

        String selection = Imps.Account.KEEP_SIGNED_IN + "=1 AND "
                + Imps.Account.ACTIVE + "=1";
        Cursor cursor = context.getContentResolver().query(Imps.Account.CONTENT_URI,
                new String[]{Imps.Account._ID}, selection, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                Log.d(TAG, "start service");
                Intent serviceIntent = new Intent();
                serviceIntent.setComponent(ImServiceConstants.IM_SERVICE_COMPONENT);
                serviceIntent.putExtra(ImServiceConstants.EXTRA_CHECK_AUTO_LOGIN, true);
                context.startService(serviceIntent);
            }
            cursor.close();
        }
    }

}
