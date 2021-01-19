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

package com.sebastienbalard.skullscoring.ui.home

import androidx.lifecycle.viewModelScope
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.models.SKGame
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.repositories.SKGameRepository
import com.sebastienbalard.skullscoring.repositories.SKPlayerRepository
import com.sebastienbalard.skullscoring.ui.*
import kotlinx.coroutines.launch

open class SKHomeViewModel(
    private val gameRepository: SKGameRepository,
    private val playerRepository: SKPlayerRepository
) : SBViewModel() {

    /*open fun loadGame(game: SKGame) = viewModelScope.launch {
        _events.postValue(EventGame(gameRepository.loadGame(game.id)))
    }*/

    open fun loadGames() = viewModelScope.launch {
        _events.postValue(EventGameList(gameRepository.loadGames()))
    }

    open fun deleteGame(vararg games: SKGame) = viewModelScope.launch {
        val count = gameRepository.deleteGame(*games)

        if (count != games.size) {
            val delta = games.size - count
            _events.postValue(EventErrorPluralWithArg(R.plurals.plural_error_game_not_deleted, delta))
        } else {
            _events.postValue(EventGameList(gameRepository.loadGames()))
        }
    }

    open fun createGameWithPlayers(gameId: Long) = viewModelScope.launch {
        val players = playerRepository.findPlayerByGame(gameId)
        val game = gameRepository.createGame(players)
        _events.value = EventGameCreated(game.id)
    }
}