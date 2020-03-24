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
import com.sebastienbalard.skullscoring.data.SKDatabase
import com.sebastienbalard.skullscoring.data.SKPlayerDao
import com.sebastienbalard.skullscoring.di.commonTestModule
import com.sebastienbalard.skullscoring.di.dataTestModule
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

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SKPlayerRepositoryTests : KoinTest {

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
    private val playerDao by inject<SKPlayerDao>()
    private val repository by inject<SKPlayerRepository>()
    private val gameRepository by inject<SKGameRepository>()

    @Test
    fun testFindPlayerByGame() = runBlocking {
        val savedPlayers =
            listOf(repository.createPlayer("Sébastien"), repository.createPlayer("Arnaud"))
        val savedGame = gameRepository.createGame(savedPlayers)

        val players = repository.findPlayerByGame(savedGame)
        players shouldBeEqualTo savedPlayers

        gameRepository.deleteGame(savedGame)
        repository.deletePlayer(*players.toTypedArray())
    }

    @Test
    fun testCreatePlayer() = runBlocking {
        val savedPlayer = repository.createPlayer("Sébastien")
        savedPlayer.id shouldBeGreaterThan 0
        repository.deletePlayer(savedPlayer)
    }

    @Before
    fun before() {
        Dispatchers.setMain(testDispatcher)
        runBlocking {
            playerDao.getAllCount() shouldBeEqualTo 0
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            playerDao.getAllCount() shouldBeEqualTo 0
        }
        testDatabase.close()
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}