/**
 * Copyright © 2021 Skull Scoring (Sébastien BALARD)
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

import android.util.Log
import org.jetbrains.annotations.NotNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class SKDebugTree : Timber.DebugTree() {

    override fun createStackElementTag(element: StackTraceElement): String? {
        return String.format(
            "### - %s",
            super.createStackElementTag(element)
        )
    }
}

class ReleaseTree : @NotNull Timber.Tree(), KoinComponent {

    private val crashReport: SBCrashReport by inject()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.DEBUG || priority == Log.VERBOSE) {
            return
        }
        when (priority) {
            Log.INFO -> crashReport.logInfo(tag ?: "no tag", message)
            Log.WARN -> crashReport.logWarning(tag ?: "no tag", message)
            Log.ERROR -> {
                t?.let {
                    crashReport.catchException(tag ?: "no tag", message, it)
                } ?: crashReport.logError(tag ?: "no tag", message)
            }
        }
    }
}