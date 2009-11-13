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

import com.android.im.plugin.ImPluginConstants;
import com.android.im.plugin.PresenceMapping;

import java.util.Map;

public class DefaultPresenceMapping implements PresenceMapping {

    public Map<String, Object> getExtra(int status) {
        return null;
    }

    public boolean getOnlineStatus(int status) {
        return status != ImPluginConstants.PRESENCE_OFFLINE;
    }

    public int getPresenceStatus(boolean onlineStatus, String userAvailability,
            Map<String, Object> allValues) {
        if (!onlineStatus) {
            return ImPluginConstants.PRESENCE_OFFLINE;
        }
        if (ImPluginConstants.PA_NOT_AVAILABLE.equals(userAvailability)) {
            return ImPluginConstants.PRESENCE_AWAY;
        } else if (ImPluginConstants.PA_DISCREET.equals(userAvailability)) {
            return ImPluginConstants.PRESENCE_DO_NOT_DISTURB;
        } else {
            return ImPluginConstants.PRESENCE_AVAILABLE;
        }
    }

    public int[] getSupportedPresenceStatus() {
        return new int[] {
                ImPluginConstants.PRESENCE_AVAILABLE,
                ImPluginConstants.PRESENCE_DO_NOT_DISTURB,
                ImPluginConstants.PRESENCE_AWAY
        };
    }

    public String getUserAvaibility(int status) {
        switch (status) {
            case ImPluginConstants.PRESENCE_AVAILABLE:
                return ImPluginConstants.PA_AVAILABLE;

            case ImPluginConstants.PRESENCE_AWAY:
                return ImPluginConstants.PA_NOT_AVAILABLE;

            case ImPluginConstants.PRESENCE_DO_NOT_DISTURB:
                return ImPluginConstants.PA_DISCREET;

            default:
                return null;
        }
    }

    public boolean requireAllPresenceValues() {
        return false;
    }

}
