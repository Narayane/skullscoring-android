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

package com.sebastienbalard.skullscoring.ui.onboarding

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.SBAnalytics
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.repositories.SKGameRepository
import com.sebastienbalard.skullscoring.repositories.SKPlayerRepository
import com.sebastienbalard.skullscoring.repositories.SKPreferenceRepository
import com.sebastienbalard.skullscoring.ui.*
import kotlinx.coroutines.launch

open class SKOnboardingViewModel(
    private val gameRepository: SKGameRepository,
    private val playerRepository: SKPlayerRepository,
    private val preferenceRepository: SKPreferenceRepository,
    private val analytics: SBAnalytics
) : SBViewModel() {

    private var _players = MutableLiveData<List<SKPlayer>>(listOf())
    open val players: LiveData<List<SKPlayer>>
        get() = _players

    open fun load() = viewModelScope.launch {
        _states.value = StateSplashConfig
        if (gameRepository.hasAtLeastOneGame()) {
            _events.value = EventSplashGoToHome
        } else {
            _events.value = EventSplashStartOnboarding
        }
    }

    open fun requestDataSendingPermissions() {
        _events.value =
            EventSplashRequestDataPermissions(preferenceRepository.requestDataSendingPermissions)
    }

    open fun createGame() = viewModelScope.launch {
        if (players.value!!.count() < 2) {
            _events.value = EventError(R.string.error_players_not_enough_selected)
        } else {
            val game = gameRepository.createGame(players.value!!)
            _events.value = EventGameCreated(game.id)
        }
    }

    open fun createPlayer(name: String) = viewModelScope.launch {
        if (name.isEmpty()) {
            _events.value = EventError(R.string.error_player_empty_name)
        } else {
            _players.postValue(mutableListOf<SKPlayer>().apply {
                addAll(_players.value!!)
                add(playerRepository.createPlayer(name))
                sortBy { it.name }
            })
            _events.value = EventPlayerCreated
        }
    }

    open fun loadOnboarding() = viewModelScope.launch {
        val hasAtLeastOneGame = gameRepository.hasAtLeastOneGame()
        _events.value = EventGameAtLeastOne(
            hasAtLeastOneGame
        )
    }

    open fun loadDataSendingPermissions() {
        _events.value = EventDataSendingPermissionsLoaded(
            preferenceRepository.isCrashDataSendingAllowed,
            preferenceRepository.isUseDataSendingAllowed
        )
    }

    open fun saveDataSendingPermissions(
        allowCrashDataSending: Boolean, allowUseDataSending: Boolean
    ) {
        preferenceRepository.isCrashDataSendingAllowed = allowCrashDataSending
        preferenceRepository.isUseDataSendingAllowed = allowUseDataSending
        preferenceRepository.requestDataSendingPermissions = false
        var bundle = Bundle()
        bundle.putInt("allowed", if (allowCrashDataSending) 1 else 0)
        bundle.putInt("is_onboarding", 1)
        analytics.sendEvent("crash_data_sending", bundle)
        bundle = Bundle()

        bundle.putInt("allowed", if (allowUseDataSending) 1 else 0)
        bundle.putInt("is_onboarding", 1)
        analytics.sendEvent("use_data_sending", bundle)

        load()
    }
}