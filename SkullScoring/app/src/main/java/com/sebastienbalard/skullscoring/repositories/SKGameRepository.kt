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

import com.sebastienbalard.skullscoring.data.SKGameDao
import com.sebastienbalard.skullscoring.data.SKGamePlayerJoinDao
import com.sebastienbalard.skullscoring.models.SKGame
import com.sebastienbalard.skullscoring.models.SKGamePlayerJoin
import com.sebastienbalard.skullscoring.models.SKPlayer

class SKGameRepository(
    private val gameDao: SKGameDao, private val gamePlayerJoinDao: SKGamePlayerJoinDao
) {

    suspend fun createGame(players: List<SKPlayer>): SKGame {
        val newGame = SKGame()
        gameDao.insert(newGame)
        val savedGame = gameDao.findByDate(newGame.startDate)
        gamePlayerJoinDao.insert(*players.map { SKGamePlayerJoin(savedGame.id, it.id) }
            .toTypedArray())
        return savedGame
    }

    suspend fun deleteGame(game: SKGame) {
        gameDao.delete(game)
    }
}