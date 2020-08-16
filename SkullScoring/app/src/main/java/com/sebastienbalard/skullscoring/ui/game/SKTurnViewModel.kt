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
import com.sebastienbalard.skullscoring.data.SKTurnPlayerJoinDao
import com.sebastienbalard.skullscoring.models.SKTurnPlayerJoin
import com.sebastienbalard.skullscoring.repositories.SKGameRepository
import com.sebastienbalard.skullscoring.ui.EventErrorWithArg
import com.sebastienbalard.skullscoring.ui.EventTurn
import com.sebastienbalard.skullscoring.ui.EventTurnDeclarationsUpdated
import com.sebastienbalard.skullscoring.ui.SBViewModel
import kotlinx.coroutines.launch

open class SKTurnViewModel(
    private val gameRepository: SKGameRepository, private val turnPlayerJoinDao: SKTurnPlayerJoinDao
) : SBViewModel() {

    open fun loadCurrentTurn(gameId: Long) = viewModelScope.launch {
        val turn = gameRepository.loadCurrentTurn(gameId)
        _events.value = EventTurn(turn)
    }

    fun saveDeclarations(declarations: List<SKTurnPlayerJoin>) = viewModelScope.launch {
        val updatedCount = turnPlayerJoinDao.update(*declarations.toTypedArray())
        if (updatedCount == declarations.count()) {
            _events.value = EventTurnDeclarationsUpdated
        } else {
            _events.value = EventErrorWithArg(
                R.string.error_turn_declarations_not_updated,
                declarations.count() - updatedCount
            )
        }
    }
}