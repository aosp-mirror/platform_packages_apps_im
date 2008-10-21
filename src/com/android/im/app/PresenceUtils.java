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
package com.android.im.app;

import android.provider.Im;
import android.util.Log;

import com.android.im.engine.Presence;
import com.android.im.plugin.BrandingResourceIDs;

public final class PresenceUtils {
    private PresenceUtils() {}

    public static int convertStatus(int status) {
        switch (status) {
        case Presence.AVAILABLE:
            return Im.Presence.AVAILABLE;

        case Presence.AWAY:
            return Im.Presence.AWAY;

        case Presence.DO_NOT_DISTURB:
            return Im.Presence.DO_NOT_DISTURB;

        case Presence.IDLE:
            return Im.Presence.IDLE;

        case Presence.OFFLINE:
            return Im.Presence.OFFLINE;

        default:
            Log.w(ImApp.LOG_TAG, "[ContactView] Unknown presence status " + status);
            return Im.Presence.AVAILABLE;
        }
    }

    public static int getStatusStringRes(int status) {
        switch (status) {
        case Im.Presence.AVAILABLE:
            return BrandingResourceIDs.STRING_PRESENCE_AVAILABLE;

        case Im.Presence.AWAY:
            return BrandingResourceIDs.STRING_PRESENCE_AWAY;

        case Im.Presence.DO_NOT_DISTURB:
            return BrandingResourceIDs.STRING_PRESENCE_BUSY;

        case Im.Presence.IDLE:
            return BrandingResourceIDs.STRING_PRESENCE_IDLE;

        case Im.Presence.INVISIBLE:
            return BrandingResourceIDs.STRING_PRESENCE_INVISIBLE;

        case Im.Presence.OFFLINE:
            return BrandingResourceIDs.STRING_PRESENCE_OFFLINE;

        default:
            return BrandingResourceIDs.STRING_PRESENCE_AVAILABLE;
        }
    }

    public static int getStatusIconId(int status) {
        switch (status) {
        case Im.Presence.AVAILABLE:
            return BrandingResourceIDs.DRAWABLE_PRESENCE_ONLINE;

        case Im.Presence.IDLE:
            return BrandingResourceIDs.DRAWABLE_PRESENCE_AWAY;

        case Im.Presence.AWAY:
            return BrandingResourceIDs.DRAWABLE_PRESENCE_AWAY;

        case Im.Presence.DO_NOT_DISTURB:
            return BrandingResourceIDs.DRAWABLE_PRESENCE_BUSY;

        case Im.Presence.INVISIBLE:
            return BrandingResourceIDs.DRAWABLE_PRESENCE_INVISIBLE;

        default:
            return BrandingResourceIDs.DRAWABLE_PRESENCE_OFFLINE;
        }
    }

}
