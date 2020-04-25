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

package com.sebastienbalard.skullscoring.ui.game

import androidx.lifecycle.viewModelScope
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.repositories.SKGameRepository
import com.sebastienbalard.skullscoring.repositories.SKPlayerRepository
import com.sebastienbalard.skullscoring.ui.EventError
import com.sebastienbalard.skullscoring.ui.SBEvent
import com.sebastienbalard.skullscoring.ui.SBViewModel
import com.sebastienbalard.skullscoring.ui.onboarding.EventGameCreated
import kotlinx.coroutines.launch

data class EventPlayerList(val players: List<SKPlayer>) : SBEvent()

open class SKPlayerSearchViewModel(
    private val playerRepository: SKPlayerRepository, private val gameRepository: SKGameRepository
) : SBViewModel() {

    open fun loadPlayers() = viewModelScope.launch {
        val players = playerRepository.findAll()
        _events.value = EventPlayerList(players)
    }

    open fun createGame(players: List<SKPlayer>) = viewModelScope.launch {
        if (players.count() < 2) {
            _events.value = EventError(R.string.error_not_enough_selected_players)
        } else {
            val game = gameRepository.createGame(players)
            _events.value = EventGameCreated(game.id)
        }
    }
}