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

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.reflect.KProperty

class SKDataSendingViewModel(
    savedStateHandle: SavedStateHandle,
    private val preferenceRepository: SKPreferenceRepository,
    private val analytics: SBAnalytics
) : SBViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(SKDataSendingUiState())
    val uiState: StateFlow<SKDataSendingUiState> = _uiState.asStateFlow()

    var isCrashDataSendingAllowed by SaveableComposeState(
        savedStateHandle,
        "isCrashDataSendingAllowed",
        true
    )

    //private set
    var isUseDataSendingAllowed by SaveableComposeState(
        savedStateHandle,
        "isUseDataSendingAllowed",
        false
    )
    //private set

    /*var isCrashDataSendingAllowed by mutableStateOf(true)
        //private set
    var isUseDataSendingAllowed by mutableStateOf(false)
        //private set*/

    open fun saveDataSendingPermissions() {

        analytics.sendEvent("crash_data_sending", Bundle().apply {
            putInt("allowed", if (isCrashDataSendingAllowed) 1 else 0)
            putInt("is_onboarding", 1)
        })

        analytics.sendEvent("use_data_sending", Bundle().apply {
            putInt("allowed", if (isUseDataSendingAllowed) 1 else 0)
            putInt("is_onboarding", 1)
        })

        preferenceRepository.isCrashDataSendingAllowed = isCrashDataSendingAllowed
        preferenceRepository.isUseDataSendingAllowed = isUseDataSendingAllowed
        preferenceRepository.requestDataSendingPermissions = false
    }
}

data class SKDataSendingUiState(
    val isCrashDataSendingAllowed: Boolean = true,
    val isUseDataSendingAllowed: Boolean = false
)

class SaveableComposeState<T>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String,
    defaultValue: T
) {
    private var _state by mutableStateOf(savedStateHandle.get<T>(key) ?: defaultValue)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = _state

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        _state = value
        savedStateHandle[key] = value
    }
}