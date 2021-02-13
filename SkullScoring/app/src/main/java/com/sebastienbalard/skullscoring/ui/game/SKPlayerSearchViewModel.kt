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
import com.sebastienbalard.skullscoring.models.SKGame
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.repositories.SKGameRepository
import com.sebastienbalard.skullscoring.repositories.SKPlayerRepository
import com.sebastienbalard.skullscoring.ui.*
import kotlinx.coroutines.launch

open class SKPlayerSearchViewModel(
    private val playerRepository: SKPlayerRepository, private val gameRepository: SKGameRepository
) : SBViewModel() {

    open fun loadPlayers() = viewModelScope.launch {
        val players = playerRepository.findAll()
        _events.value = EventPlayerList(players)
    }

    open fun createPlayer(name: String) = viewModelScope.launch {
        if (name.isEmpty()) {
            _events.value = EventError(R.string.error_player_empty_name)
        } else {
            playerRepository.findPlayerByName(name)?.let {
                _events.value = EventErrorWithArg(R.string.error_player_already_exists, it.name)
            } ?: playerRepository.createPlayer(name).apply {
                _events.value = EventPlayer(this)
            }
        }
    }

    open fun createGame(players: List<SKPlayer>) = viewModelScope.launch {
        if (players.count() < 2) {
            _events.value = EventError(R.string.error_players_not_enough_selected)
        } else {
            val game = gameRepository.createGame(players)
            _events.value = EventGameCreated(game.id)
        }
    }

    open fun loadPlayers(playerIds: List<Long>) = viewModelScope.launch {
        val players = playerRepository.getPlayers(*playerIds.toLongArray())
        _events.value = EventPlayerList(players)
    }

    open fun deletePlayer(vararg player: SKPlayer) = viewModelScope.launch {
        val count = playerRepository.deletePlayer(*player)

        if (count != player.size) {
            val delta = player.size - count
            _events.postValue(EventErrorPluralWithArg(R.plurals.plural_error_player_not_deleted, delta))
        } else {
            _events.postValue(EventPlayerList(playerRepository.findAll()))
        }
    }
}