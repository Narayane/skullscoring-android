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
import com.sebastienbalard.skullscoring.models.SKGroup
import com.sebastienbalard.skullscoring.models.SKPlayerGroupJoin
import com.sebastienbalard.skullscoring.repositories.SKGroupRepository
import com.sebastienbalard.skullscoring.repositories.SKPlayerRepository
import com.sebastienbalard.skullscoring.ui.EventGroupList
import com.sebastienbalard.skullscoring.ui.EventPlayer
import com.sebastienbalard.skullscoring.ui.SBViewModel
import kotlinx.coroutines.launch

open class SKPlayerViewModel(
    private val groupRepository: SKGroupRepository,
    private val playerRepository: SKPlayerRepository,
    private val playerGroupJoinDao: SKPlayerGroupJoinDao
) : SBViewModel() {

    fun getPlayer(playerId: Long) = viewModelScope.launch {
        val player = playerRepository.getPlayer(playerId)
        _events.value = EventPlayer(player)
    }

    fun createGroup(name: String, playerId: Long) = viewModelScope.launch {
        val group = groupRepository.createGroup(name)
        playerGroupJoinDao.insert(SKPlayerGroupJoin(playerId, group.id))
    }

    fun createPlayer(name: String, groups: List<SKGroup>) = viewModelScope.launch {
        val player = playerRepository.createPlayer(name)
        if (groups.isNotEmpty()) {
            groups.forEach {
                createGroup(it.name, player.id)
            }
        }
    }

    fun updatePlayer(playerId: Long, name: String, groups: List<SKGroup>) = viewModelScope.launch {
        val player = playerRepository.getPlayer(playerId)
        if (name != player.name) {
            player.name = name
            playerRepository.updatePlayer(player)
        }
        groups.filter { it.id == 0L }.map { createGroup(it.name, player.id) } // create the new ones

        val playerGroups = player.groups.filter { it.id != 0L }
        val updatedGroups = groups.filter { it.id != 0L }
        playerGroups.forEach { current ->
            if (!updatedGroups.contains(current)) {
                playerGroupJoinDao.delete(SKPlayerGroupJoin(playerId, current.id)) // delete the removed ones
            }
        }
        updatedGroups.forEach { updated ->
            if (!playerGroups.contains(updated)) {
                playerGroupJoinDao.insert(SKPlayerGroupJoin(playerId, updated.id)) // insert the added ones
            }
        }
        groupRepository.deleteOrphanGroups()
    }

    fun getGroups() = viewModelScope.launch {
        val groups = groupRepository.loadAll()
        _events.value = EventGroupList(groups)
    }
}