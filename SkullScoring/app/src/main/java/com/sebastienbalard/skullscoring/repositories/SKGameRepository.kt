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
import com.sebastienbalard.skullscoring.models.SKGame
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.models.SKTurn
import timber.log.Timber

open class SKGameRepository(
    private val playerRepository: SKPlayerRepository,
    private val turnRepository: SKTurnRepository,
    private val gameDao: SKGameDao
) {

    open suspend fun hasAtLeastOneGame(): Boolean {
        return gameDao.getAllCount() > 0
    }

    open suspend fun loadGame(gameId: Long): SKGame {
        val game = gameDao.findById(gameId)
        Timber.d("current turn: ${game.currentTurnNumber}")
        game.players =
            playerRepository.getPlayersWithScore(gameId, game.isEnded, game.currentTurnNumber)
        val results = turnRepository.getTurnByNumber(
            game.id, game.currentTurnNumber
        ).results
        val nullDeclarationCount = results.filter { it.declaration == null }.size
        game.areCurrentTurnDeclarationsSet = nullDeclarationCount == 0
        val nullResultsCount = results.filter { it.result == null }.size
        game.areCurrentTurnResultsSet = nullResultsCount == 0
        return game
    }

    open suspend fun loadGames(): List<SKGame> =
        gameDao.getAll().sortedByDescending { it.startDate }

    open suspend fun createGame(players: List<SKPlayer>): SKGame {
        val newGame = SKGame()
        gameDao.insert(newGame)
        val savedGame = gameDao.findByDate(newGame.startDate)
        playerRepository.createPlayersForGame(players, savedGame.id)
        turnRepository.createTurnsForGame(savedGame.id, players)
        return savedGame
    }

    open suspend fun deleteGame(vararg games: SKGame): Int {
        games.forEach {
            turnRepository.deleteTurnsForGame(it.id)
        }
        return gameDao.delete(*games)
    }

    open suspend fun loadCurrentTurn(gameId: Long): SKTurn = turnRepository.getCurrentTurn(gameId)

    open suspend fun loadTurn(gameId: Long, number: Int): SKTurn = turnRepository.getTurnByNumber(gameId, number)

    open suspend fun startNextTurn(gameId: Long) {
        val game = gameDao.findById(gameId)
        game.currentTurnNumber++
        gameDao.update(game)
    }

    open suspend fun endGame(gameId: Long) {
        val game = gameDao.findById(gameId)
        game.isEnded = true
        gameDao.update(game)
    }
}