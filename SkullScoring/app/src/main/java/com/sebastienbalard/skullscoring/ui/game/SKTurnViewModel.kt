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
import com.sebastienbalard.skullscoring.data.SKTurnDao
import com.sebastienbalard.skullscoring.data.SKTurnPlayerJoinDao
import com.sebastienbalard.skullscoring.models.SKTurnPlayerJoin
import com.sebastienbalard.skullscoring.repositories.SKGameRepository
import com.sebastienbalard.skullscoring.ui.*
import kotlinx.coroutines.launch
import timber.log.Timber

open class SKTurnViewModel(
    private val gameRepository: SKGameRepository, private val turnDao: SKTurnDao, private val turnPlayerJoinDao: SKTurnPlayerJoinDao
) : SBViewModel() {

    open fun loadTurnDeclarations(gameId: Long) = viewModelScope.launch {
        val turn = gameRepository.loadCurrentTurn(gameId)
        _states.value = StateTurnDeclarations(turn)
    }

    open fun loadTurnResults(gameId: Long) = viewModelScope.launch {
        val turn = gameRepository.loadCurrentTurn(gameId)
        _states.value = StateTurnResults(turn)
    }

    open fun saveTurnDeclarations(declarations: List<SKTurnPlayerJoin>) = viewModelScope.launch {
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

    open fun saveTurnResults(results: List<SKTurnPlayerJoin>, gameId: Long) = viewModelScope.launch {
        val currentTurnNumber = turnDao.findById(results[0].turnId).number
        results.map { if (it.result == null) it.result = it.declaration }
        val turnResultSum = results.filter { it.result != null }.map { it.result!! }.reduce { acc, value -> acc + value }
        Timber.d("turn results sum : $turnResultSum")
        if (turnResultSum == currentTurnNumber) {
            val updatedCount = turnPlayerJoinDao.update(*results.toTypedArray())
            if (updatedCount == results.count()) {
                _events.value = EventTurnResultsUpdated
            } else {
                _events.value = EventErrorWithArg(
                    R.string.error_turn_results_not_updated,
                    results.count() - updatedCount
                )
            }
        } else {
            _events.value = EventErrorWithArg(
                R.string.error_turn_results_sum_is_invalid,
                currentTurnNumber
            )
        }
    }
}