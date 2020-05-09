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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sebastienbalard.skullscoring.data.*
import com.sebastienbalard.skullscoring.di.commonTestModule
import com.sebastienbalard.skullscoring.di.dataTestModule
import com.sebastienbalard.skullscoring.models.SKPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeGreaterThan
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject

fun void() {}

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SKGameRepositoryTests : KoinTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.DEBUG)
        modules(commonTestModule + dataTestModule)
        androidContext(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    private val testDispatcher = TestCoroutineDispatcher()

    private val testDatabase by inject<SKDatabase>()
    private val repository by inject<SKGameRepository>()
    private val gameDao by inject<SKGameDao>()
    private val playerDao by inject<SKPlayerDao>()
    private val turnDao by inject<SKTurnDao>()
    private val turnPlayerJoinDao by inject<SKTurnPlayerJoinDao>()

    @Test
    fun testCreateGame() = runBlocking {

        val players = playerDao.getAll()

        val savedGame = repository.createGame(players)
        val gameId = savedGame.id
        gameId shouldBeGreaterThan 0

        val turns = turnDao.findByGame(gameId)
        turns.size shouldBeEqualTo 10
        turns.map { turn ->
            val results = turnPlayerJoinDao.findResultByTurn(turn.id)
            results.size shouldBeEqualTo players.size
        }

        repository.deleteGame(savedGame)

        turnDao.getCountByGame(gameId) shouldBeEqualTo 0

        void()
    }

    @Before
    fun before() {
        Dispatchers.setMain(testDispatcher)
        runBlocking {
            gameDao.getAllCount() shouldBeEqualTo 0
            playerDao.getAllCount() shouldBeEqualTo 0
            playerDao.insert(SKPlayer("Sébastien"), SKPlayer("Arnaud"))
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            gameDao.getAllCount() shouldBeEqualTo 0
            playerDao.delete(playerDao.findByName("Sébastien")!!)
            playerDao.delete(playerDao.findByName("Arnaud")!!)
            playerDao.getAllCount() shouldBeEqualTo 0
        }
        testDatabase.close()
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}