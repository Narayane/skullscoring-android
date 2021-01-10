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

package com.sebastienbalard.skullscoring.ui.players

import androidx.lifecycle.viewModelScope
import com.sebastienbalard.skullscoring.data.SKPlayerGroupJoinDao
import com.sebastienbalard.skullscoring.models.SKPlayerGroupJoin
import com.sebastienbalard.skullscoring.repositories.SKGroupRepository
import com.sebastienbalard.skullscoring.repositories.SKPlayerRepository
import com.sebastienbalard.skullscoring.ui.SBViewModel
import kotlinx.coroutines.launch

open class SKPlayerViewModel(
    private val groupRepository: SKGroupRepository,
    private val playerRepository: SKPlayerRepository,
    private val playerGroupJoinDao: SKPlayerGroupJoinDao
) : SBViewModel() {

    fun createGroup(name: String, playerId: Long) = viewModelScope.launch {
        val group = groupRepository.createGroup(name)
        playerGroupJoinDao.insert(SKPlayerGroupJoin(playerId, group.id))
    }

    fun createPlayer(name: String, groupNames: List<String>) = viewModelScope.launch {
        val player = playerRepository.createPlayer(name)
        if (groupNames.isNotEmpty()) {
            groupNames.forEach { groupName ->
                createGroup(groupName, player.id)
            }
        }
    }
}