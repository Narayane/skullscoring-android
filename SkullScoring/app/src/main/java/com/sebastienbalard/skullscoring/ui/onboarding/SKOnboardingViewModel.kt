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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.repositories.SKGameRepository
import com.sebastienbalard.skullscoring.repositories.SKPlayerRepository
import com.sebastienbalard.skullscoring.ui.SBEvent
import com.sebastienbalard.skullscoring.ui.SBViewModel
import kotlinx.coroutines.launch

object EventPlayerCreated : SBEvent()
data class EventGameCreated(val gameId: Long) : SBEvent()
data class EventGameAtLeastOne(val hasAtLeastOneGame: Boolean) : SBEvent()

open class SKOnboardingViewModel(
    private val gameRepository: SKGameRepository, private val playerRepository: SKPlayerRepository
) : SBViewModel() {

    private var _players = MutableLiveData<List<SKPlayer>>(listOf())
    val players: LiveData<List<SKPlayer>>
        get() = _players

    open fun createGame() = viewModelScope.launch {
        val game = gameRepository.createGame(players.value!!)
        _events.value =
            EventGameCreated(game.id)
    }

    open fun createPlayer(name: String) = viewModelScope.launch {
        _players.postValue(mutableListOf<SKPlayer>().apply {
            addAll(_players.value!!)
            add(playerRepository.createPlayer(name))
            sortBy { it.name }
        })
        _events.value =
            EventPlayerCreated
    }

    open fun load() = viewModelScope.launch {
        val hasAtLeastOneGame = gameRepository.hasAtLeastOneGame()
        _events.value =
            EventGameAtLeastOne(
                hasAtLeastOneGame
            )
    }
}