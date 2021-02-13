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
import com.sebastienbalard.skullscoring.data.SKPlayerGroupJoinDao
import com.sebastienbalard.skullscoring.models.SKGamePlayerJoin
import com.sebastienbalard.skullscoring.models.SKPlayer
import timber.log.Timber
import java.text.Collator
import java.util.*
import kotlin.Comparator
import kotlin.math.abs

open class SKPlayerRepository(
    private val turnRepository: SKTurnRepository,
    private val playerDao: SKPlayerDao,
    private val gamePlayerJoinDao: SKGamePlayerJoinDao,
    private val playerGroupJoinDao: SKPlayerGroupJoinDao
) {

    open suspend fun createPlayer(name: String): SKPlayer {
        playerDao.insert(SKPlayer(name))
        return playerDao.findByName(name)!!
    }

    open suspend fun deletePlayer(vararg player: SKPlayer): Int {
        return playerDao.delete(*player)
    }

    open suspend fun findPlayerByName(name: String): SKPlayer? {
        return playerDao.findByName(name)
    }

    open suspend fun findPlayerByGame(gameId: Long): List<SKPlayer> {
        return gamePlayerJoinDao.findPlayerByGame(gameId)
    }

    open suspend fun findAll(): List<SKPlayer> {
        val players = playerDao.getAll().sortedWith(compareBy({ s1, s2 ->
            Collator.getInstance(Locale.getDefault()).compare(s1, s2)
        }) { it.name })
        players.forEach { player ->
            player.groups = playerGroupJoinDao.findGroupByPlayer(player.id)
            player.isDeletable = gamePlayerJoinDao.findGameByPlayer(player.id).isEmpty()
        }
        return players
    }

    open suspend fun addPlayersToGame(players: List<SKPlayer>, gameId: Long) {
        gamePlayerJoinDao.insert(*players.map {
            SKGamePlayerJoin(
                gameId, it.id, players.indexOf(it)
            )
        }.toTypedArray())
    }

    open suspend fun getPlayersWithScore(
        gameId: Long, isEnded: Boolean, currentTurnNumber: Int
    ): List<SKPlayer> {
        val players = gamePlayerJoinDao.findPlayerByGame(gameId)
        players.forEach { player ->
            Timber.d(" ")
            Timber.d("current player: ${player.name}")
            val turns = turnRepository.getTurnResultsByPlayer(gameId, player.id)
            player.currentTurnDeclaration =
                if (isEnded) null else turns.first { it.number == currentTurnNumber }.results.first { it.playerId == player.id }.declaration
            player.score = turns.map { turn ->
                Timber.d("turn: ${turn.number}")
                var turnScore = 0
                turn.results.first { it.playerId == player.id }.let {
                    Timber.d("declaration: ${it.declaration}, result: ${it.result}, hasSkullKing: ${it.hasSkullKing}(${it.pirateCount}), hasMarmaid: ${it.hasMermaid}")
                    it.declaration?.let { declaration ->
                        it.result?.let { result ->
                            if (declaration == result) {
                                turnScore += if (result == 0) turn.number * 10 else result * 20
                                val hasSkullKing = it.hasSkullKing ?: false
                                val pirateCount = it.pirateCount ?: 0
                                turnScore += if (hasSkullKing) pirateCount * 30 else 0
                                val hasMermaid = it.hasMermaid ?: false
                                turnScore += if (hasMermaid) 50 else 0
                            } else {
                                turnScore += if (declaration == 0) turn.number * -10 else abs(result - declaration) * -10
                            }
                        }
                    }
                }
                turnScore
            }.reduce { acc, next -> acc + next }
            player.position = gamePlayerJoinDao.getPlayerPosition(gameId, player.id)
        }
        return if (players.filter { it.score == 0 }.size == players.size) {
            players.sortedWith(compareBy<SKPlayer> { it.position })
        } else {
            players.sortedWith(compareByDescending<SKPlayer> { it.score }.thenBy { it.name })
        }
    }

    open suspend fun getPlayer(playerId: Long): SKPlayer {
        val player = playerDao.findById(playerId)
        player.groups = playerGroupJoinDao.findGroupByPlayer(playerId)
        return player
    }

    open suspend fun updatePlayer(player: SKPlayer) {
        playerDao.update(player)
    }

    open suspend fun getPlayers(vararg playerId: Long): List<SKPlayer> {
        return playerId.map { id ->
            playerDao.findById(id).apply {
                groups = playerGroupJoinDao.findGroupByPlayer(id)
            }
        }
    }
}