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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
open class SKBaseDaoTests {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    protected lateinit var testDatabase: SKDatabase

    @Before
    open fun setUp() {
        Dispatchers.setMain(testDispatcher)
        testDatabase = inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), SKDatabase::class.java
        ).setTransactionExecutor(Executors.newSingleThreadExecutor()).build()
    }

    @After
    open fun tearDown() {
        testDatabase.close()
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}