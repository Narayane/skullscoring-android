/**
 * Copyright © 2023 Skull Scoring (Sébastien BALARD)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sebastienbalard.skullscoring

import android.content.SharedPreferences

class SKPreferenceRepository(private val preferences: SharedPreferences) {

    open var requestDataSendingPermissions: Boolean
        get() = preferences.getBoolean(PREFERENCE_REQUEST_DATA_SENDING_PERMISSIONS, true)
        set(value) {
            preferences.edit().putBoolean(PREFERENCE_REQUEST_DATA_SENDING_PERMISSIONS, value).commit()
        }

    open var isCrashDataSendingAllowed: Boolean
        get() = preferences.getBoolean(PREFERENCE_CRASH_DATA_SENDING_PERMISSION, true)
        set(value) {
            preferences.edit().putBoolean(PREFERENCE_CRASH_DATA_SENDING_PERMISSION, value).commit()
        }

    open var isUseDataSendingAllowed: Boolean
        get() = preferences.getBoolean(PREFERENCE_USE_DATA_SENDING_PERMISSION, true)
        set(value) {
            preferences.edit().putBoolean(PREFERENCE_USE_DATA_SENDING_PERMISSION, value).commit()
        }

    companion object {
        const val PREFERENCE_REQUEST_DATA_SENDING_PERMISSIONS: String = "PREFERENCE_REQUEST_DATA_SENDING_PERMISSIONS"
        const val PREFERENCE_CRASH_DATA_SENDING_PERMISSION: String = "PREFERENCE_CRASH_DATA_SENDING_PERMISSION"
        const val PREFERENCE_USE_DATA_SENDING_PERMISSION: String = "PREFERENCE_USE_DATA_SENDING_PERMISSION"
    }
}