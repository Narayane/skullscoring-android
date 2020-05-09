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

package com.sebastienbalard.skullscoring.ui.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyBlocking
import com.nhaarman.mockitokotlin2.whenever
import com.sebastienbalard.skullscoring.repositories.SKGameRepository
import com.sebastienbalard.skullscoring.ui.EventSplashGoToHome
import com.sebastienbalard.skullscoring.ui.EventSplashStartOnboarding
import com.sebastienbalard.skullscoring.ui.StateSplashConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldNotBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SKSplashViewModelTests {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mockGameRepository = mock<SKGameRepository>()

    private lateinit var viewModel: SKSplashViewModel

    @Test
    fun testLoadConfig() {

        runBlockingTest {
            whenever(mockGameRepository.hasAtLeastOneGame()).thenReturn(true)
        }

        viewModel.loadConfig()

        verifyBlocking(mockGameRepository) {
            hasAtLeastOneGame()
        }

        viewModel.states.value shouldNotBe null
        viewModel.states.value shouldBeInstanceOf StateSplashConfig::class.java

        viewModel.events.value shouldNotBe null
        viewModel.events.value shouldBeInstanceOf EventSplashGoToHome::class.java
    }

    @Test
    fun testLoadConfigStartOnboarding() {

        runBlockingTest {
            whenever(mockGameRepository.hasAtLeastOneGame()).thenReturn(false)
        }

        viewModel.loadConfig()

        verifyBlocking(mockGameRepository) {
            hasAtLeastOneGame()
        }

        viewModel.states.value shouldNotBe null
        viewModel.states.value shouldBeInstanceOf StateSplashConfig::class.java

        viewModel.events.value shouldNotBe null
        viewModel.events.value shouldBeInstanceOf EventSplashStartOnboarding::class.java
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        viewModel = SKSplashViewModel(mockGameRepository)
        viewModel shouldNotBe null
        viewModel.states shouldNotBe null
        viewModel.events shouldNotBe null
    }
}
