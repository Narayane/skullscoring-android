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

import com.sebastienbalard.skullscoring.data.*
import com.sebastienbalard.skullscoring.models.*
import timber.log.Timber
import kotlin.math.abs

open class SKGameRepository(
    private val gameDao: SKGameDao,
    private val gamePlayerJoinDao: SKGamePlayerJoinDao,
    private val turnDao: SKTurnDao,
    private val turnPlayerJoinDao: SKTurnPlayerJoinDao,
    private val playerDao: SKPlayerDao
) {

    open suspend fun hasAtLeastOneGame(): Boolean {
        return gameDao.getAllCount() > 0
    }

    open suspend fun loadGame(gameId: Long): SKGame {
        val players = gamePlayerJoinDao.findPlayerByGame(gameId)
        players.forEach { player ->
            Timber.d("current player: ${player.name}")
            val turns = turnPlayerJoinDao.findTurnByPlayer(player.id)
            turns.map {
                it.results = turnPlayerJoinDao.findResultByTurn(it.id)
            }
            player.score = turns.map { turn ->
                Timber.d("current turn: ${turn.number}")
                var turnScore = 0
                turn.results.first { it.playerId == player.id }.let {
                    Timber.d("declaration: ${it.declaration}, result: ${it.result}, hasSkullKing: ${it.hasSkullKing}(${it.pirateCount}), hasMarmaid: ${it.hasMarmaid}")
                    it.declaration?.let { declaration ->
                        it.result?.let { result ->
                            turnScore += if (declaration == result) {
                                if (result == 0) turn.number * 10 else result * 20
                            } else {
                                abs(result - declaration) * -10
                            }
                            val hasSkullKing = it.hasSkullKing ?: false
                            val pirateCount = it.pirateCount ?: 0
                            turnScore += if (hasSkullKing) pirateCount * 30 else 0
                            val hasMarmaid = it.hasMarmaid ?: false
                            turnScore += if (hasMarmaid) 50 else 0
                        }
                    }
                }
                turnScore
            }.reduce { acc, next -> acc + next }
        }
        val game = gameDao.findById(gameId)
        game.players = players.sortedWith(compareByDescending<SKPlayer> { it.score }.thenBy { it.name })
        return game
    }

    open suspend fun loadGames(): List<SKGame> {
        return gameDao.getAll().apply {
            map { it.state = Ongoing }
            sortedByDescending { it.startDate }
        }
    }

    open suspend fun createGame(players: List<SKPlayer>): SKGame {
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
                    turn.id, player.id
                )
            }
        }.flatten()
        turnPlayerJoinDao.insert(*results.toTypedArray())
        return savedGame
    }

    open suspend fun deleteGame(game: SKGame) {
        turnDao.delete(*turnDao.findByGame(game.id).toTypedArray())
        gameDao.delete(game)
    }

    open suspend fun loadCurrentTurn(gameId: Long): SKTurn {
        val game = gameDao.findById(gameId)
        val turn = turnDao.findByNumber(gameId, game.currentTurnNumber)
        turn.results = turnPlayerJoinDao.findResultByTurn(turn.id)
        turn.results.forEach {
            it.player = playerDao.findById(it.playerId)
        }
        turn.results.sortedBy { it.player.name }
        return turn
    }
}