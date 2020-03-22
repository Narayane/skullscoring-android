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

package com.sebastienbalard.skullscoring.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebastienbalard.skullscoring.models.SKGame
import com.sebastienbalard.skullscoring.models.SKTurn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SKTurnDaoTests : SKBaseDaoTests() {

    private lateinit var gameDao: SKGameDao
    private lateinit var turnDao: SKTurnDao

    private lateinit var game: SKGame

    @Test
    fun testInsert() {
        val turn = SKTurn(1, game.id)
        runBlocking {
            turnDao.insert(turn)
            turnDao.getCountByGame(1) shouldBeEqualTo 1
        }
    }

    @Before
    override fun setUp() {
        super.setUp()
        gameDao = testDatabase.getGameDao()
        turnDao = testDatabase.getTurnDao()
        runBlocking {
            gameDao.getAllCount() shouldBeEqualTo 0
            val localGame = SKGame(mutableListOf())
            gameDao.insert(localGame)
            game = gameDao.findByDate(localGame.startDate)
            turnDao.getAllCount() shouldBeEqualTo 0
        }
    }

    @After
    override fun tearDown() {
        runBlocking {
            gameDao.delete(game)
            turnDao.getAllCount() shouldBeEqualTo 0
            gameDao.getAllCount() shouldBeEqualTo 0
        }
        super.tearDown()
    }
}