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

package com.sebastienbalard.skullscoring.repositories

import com.sebastienbalard.skullscoring.data.SKGamePlayerJoinDao
import com.sebastienbalard.skullscoring.data.SKPlayerDao
import com.sebastienbalard.skullscoring.models.SKGame
import com.sebastienbalard.skullscoring.models.SKPlayer

class SKPlayerRepository(
    private val playerDao: SKPlayerDao, private val gamePlayerJoinDao: SKGamePlayerJoinDao
) {

    suspend fun createPlayer(name: String): SKPlayer {
        playerDao.insert(SKPlayer(name))
        return playerDao.findByName(name)!!
    }

    suspend fun deletePlayer(vararg player: SKPlayer) {
        playerDao.delete(*player)
    }

    suspend fun findPlayerByName(name: String): SKPlayer? {
        return playerDao.findByName(name)
    }

    suspend fun findPlayerByGame(game: SKGame): List<SKPlayer> {
        return gamePlayerJoinDao.findPlayerByGame(game.id)
    }

    suspend fun findAll(): List<SKPlayer> {
        return playerDao.getAll().sortedBy { it.name }
    }
}