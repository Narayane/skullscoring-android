/**
 * Copyright © 2020 Skull Scoring (Sébastien BALARD)
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

import com.google.firebase.crashlytics.FirebaseCrashlytics

open class SBCrashReport(
    private val preferenceRepository: SKPreferenceRepository,
    private val crashlytics: FirebaseCrashlytics
) {

    open fun setUserInformation(userId: String, userName: String, email: String) {
        if (BuildConfig.BUILD_TYPE == "release" && preferenceRepository.isCrashDataSendingAllowed) {
            crashlytics.setUserId(userId)
        }
    }

    open fun setCustomKey(key: String, value: String) {
        if (BuildConfig.BUILD_TYPE == "release" && preferenceRepository.isCrashDataSendingAllowed) {
            crashlytics.setCustomKey(key, value)
        }
    }

    open fun logDebug(callerName: String, message: String): String {
        if (BuildConfig.BUILD_TYPE == "release" && preferenceRepository.isCrashDataSendingAllowed) {
            crashlytics.log("[DEBUG] | $callerName: $message")
        }
        return message
    }

    open fun logInfo(callerName: String, message: String): String {
        if (BuildConfig.BUILD_TYPE == "release" && preferenceRepository.isCrashDataSendingAllowed) {
            crashlytics.log("[INFO] | $callerName: $message")
        }
        return message
    }

    open fun logWarning(callerName: String, message: String): String {
        if (BuildConfig.BUILD_TYPE == "release" && preferenceRepository.isCrashDataSendingAllowed) {
            crashlytics.log("[WARN] | $callerName: $message")
        }
        return message
    }

    open fun logError(callerName: String, message: String): String {
        if (BuildConfig.BUILD_TYPE == "release" && preferenceRepository.isCrashDataSendingAllowed) {
            crashlytics.log("[ERROR] | $callerName: $message")
        }
        return message
    }

    open fun catchException(callerName: String, message: String, exception: Exception): String {
        exception.cause?.let { throwable ->
            return catchException(callerName, message, throwable)
        }
        return message
    }

    open fun catchException(callerName: String, message: String, throwable: Throwable): String {
        logError(callerName, message)
        if (BuildConfig.BUILD_TYPE == "release" && preferenceRepository.isCrashDataSendingAllowed) {
            crashlytics.recordException(throwable)
        }
        return message
    }
}
