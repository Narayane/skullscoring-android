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
import com.sebastienbalard.skullscoring.data.SKPlayerDao
import com.sebastienbalard.skullscoring.data.SKTurnDao
import com.sebastienbalard.skullscoring.data.SKTurnPlayerJoinDao
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.models.SKTurn
import com.sebastienbalard.skullscoring.models.SKTurnPlayerJoin

open class SKTurnRepository(
    private val gameDao: SKGameDao,
    private val turnDao: SKTurnDao,
    private val turnPlayerJoinDao: SKTurnPlayerJoinDao,
    private val playerDao: SKPlayerDao
) {

    open suspend fun getTurnByNumber(gameId: Long, number: Int): SKTurn {
        val turn = turnDao.findByNumber(gameId, number)
        val results = turnPlayerJoinDao.findResultByTurn(turn.id)
        turn.results = results
        return turn
    }

    open suspend fun getCurrentTurn(gameId: Long): SKTurn {
        val game = gameDao.findById(gameId)
        val turn = turnDao.findByNumber(gameId, game.currentTurnNumber)
        turn.results = turnPlayerJoinDao.findResultByTurn(turn.id)
        turn.results.forEach {
            it.player = playerDao.findById(it.playerId)
        }
        turn.results.sortedBy { it.player.name }
        return turn
    }

    open suspend fun getTurnResultsByPlayer(gameId: Long, playerId: Long): List<SKTurn> {
        val turns = turnPlayerJoinDao.findTurnByPlayer(playerId, gameId)
        turns.map {
            it.results = turnPlayerJoinDao.findResultByTurn(it.id)
        }
        return turns
    }

    open suspend fun createTurnsForGame(gameId: Long, players: List<SKPlayer>) {
        turnDao.insert(*Array(10) { index -> SKTurn(index + 1, gameId) })
        val savedTurns = turnDao.findByGame(gameId)
        val results = savedTurns.map { turn ->
            players.map { player ->
                SKTurnPlayerJoin(
                    turn.id, player.id
                )
            }
        }.flatten()
        turnPlayerJoinDao.insert(*results.toTypedArray())
    }

    open suspend fun deleteTurnsForGame(gameId: Long) {
        turnDao.delete(*turnDao.findByGame(gameId).toTypedArray())
    }
}