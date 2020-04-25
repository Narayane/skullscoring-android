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
import com.sebastienbalard.skullscoring.data.SKTurnDao
import com.sebastienbalard.skullscoring.data.SKTurnPlayerJoinDao
import com.sebastienbalard.skullscoring.models.*

class SKGameRepository(
    private val gameDao: SKGameDao,
    private val gamePlayerJoinDao: SKGamePlayerJoinDao,
    private val turnDao: SKTurnDao,
    private val turnPlayerJoinDao: SKTurnPlayerJoinDao
) {

    suspend fun hasAtLeastOneGame(): Boolean {
        return gameDao.getAllCount() > 0
    }

    suspend fun loadGame(gameId: Long): SKGame {
        val game = gameDao.findById(gameId)
        game.players = gamePlayerJoinDao.findPlayerByGame(gameId).sortedBy { it.name }
        return game
    }

    suspend fun loadGames(): List<SKGame> {
        return gameDao.getAll().sortedByDescending { it.startDate }
    }

    suspend fun createGame(players: List<SKPlayer>): SKGame {
        val newGame = SKGame()
        gameDao.insert(newGame)
        val savedGame = gameDao.findByDate(newGame.startDate)
        gamePlayerJoinDao.insert(*players.map { SKGamePlayerJoin(savedGame.id, it.id) }
            .toTypedArray())
        turnDao.insert(*Array(10) { index -> SKTurn(index + 1, savedGame.id) })
        val savedTurns = turnDao.findByGame(savedGame.id)
        val results = savedTurns.map { turn ->
            players.map { player ->
                SKTurnPlayerJoin(
                    turn.id,
                    player.id
                )
            }
        }.flatten()
        turnPlayerJoinDao.insert(*results.toTypedArray())
        return savedGame
    }

    suspend fun deleteGame(game: SKGame) {
        turnDao.delete(*turnDao.findByGame(game.id).toTypedArray())
        gameDao.delete(game)
    }
}