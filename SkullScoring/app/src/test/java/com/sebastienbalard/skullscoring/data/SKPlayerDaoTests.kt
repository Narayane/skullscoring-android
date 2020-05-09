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
import com.sebastienbalard.skullscoring.models.SKPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SKPlayerDaoTests : SKBaseDaoTests() {

    private lateinit var playerDao: SKPlayerDao

    @Test
    fun testInsert() = runBlocking {
        val player = SKPlayer("Sébastien")
        playerDao.insert(player)
        playerDao.getAllCount() shouldBeEqualTo 1
        playerDao.delete(playerDao.findByName("Sébastien")!!)
    }

    @Before
    override fun setUp() {
        super.setUp()
        playerDao = testDatabase.getPlayerDao()
        runBlocking {
            playerDao.getAllCount() shouldBeEqualTo 0
        }
    }

    @After
    override fun tearDown() {
        runBlocking {
            playerDao.getAllCount() shouldBeEqualTo 0
        }
        super.tearDown()
    }
}