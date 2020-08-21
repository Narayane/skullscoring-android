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
import com.sebastienbalard.skullscoring.repositories.SKGameRepository
import com.sebastienbalard.skullscoring.ui.EventGame
import com.sebastienbalard.skullscoring.ui.SBViewModel
import kotlinx.coroutines.launch

open class SKGameViewModel(
    private val gameRepository: SKGameRepository
) : SBViewModel() {

    open fun loadGame(gameId: Long) = viewModelScope.launch {
        val game = gameRepository.loadGame(gameId)
        _events.value = EventGame(game)
    }

    fun startNextTurn(gameId: Long) = viewModelScope.launch {
        gameRepository.startNextTurn(gameId)
        loadGame(gameId)
    }

    fun endGame(gameId: Long) = viewModelScope.launch {
        gameRepository.endGame(gameId)
        loadGame(gameId)
    }
}