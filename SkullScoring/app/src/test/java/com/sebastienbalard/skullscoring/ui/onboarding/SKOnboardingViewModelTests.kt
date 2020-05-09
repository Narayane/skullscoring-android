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

package com.sebastienbalard.skullscoring.ui.onboarding

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.models.SKGame
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.repositories.SKGameRepository
import com.sebastienbalard.skullscoring.repositories.SKPlayerRepository
import com.sebastienbalard.skullscoring.ui.EventError
import com.sebastienbalard.skullscoring.ui.EventGameAtLeastOne
import com.sebastienbalard.skullscoring.ui.EventGameCreated
import com.sebastienbalard.skullscoring.ui.EventPlayerCreated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldNotBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito
import java.util.*

@ExperimentalCoroutinesApi
class SKOnboardingViewModelTests {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mockGameRepository = mock<SKGameRepository>()
    private val mockPlayerRepository = mock<SKPlayerRepository>()

    private lateinit var viewModel: SKOnboardingViewModel

    @Test
    fun testLoad() {

        runBlockingTest {
            whenever(mockGameRepository.hasAtLeastOneGame()).thenReturn(true)
        }

        viewModel.load()

        verifyBlocking(mockGameRepository) {
            hasAtLeastOneGame()
        }

        viewModel.events.value shouldNotBe null
        val event = viewModel.events.value as? EventGameAtLeastOne
        event shouldNotBe null
        event?.hasAtLeastOneGame shouldBeEqualTo true
    }

    @Test
    fun testCreatePlayer() {

        runBlockingTest {
            whenever(mockPlayerRepository.createPlayer("Sébastien")).thenReturn(SKPlayer("Sébastien"))
        }

        viewModel.createPlayer("Sébastien")

        verifyBlocking(mockPlayerRepository) {
            createPlayer("Sébastien")
        }

        viewModel.events.value shouldNotBe null
        viewModel.events.value shouldBeInstanceOf EventPlayerCreated::class.java
    }

    @Test
    fun testCreatePlayerWithEmptyName() {

        viewModel.createPlayer("")

        verifyZeroInteractions(mockPlayerRepository)

        viewModel.events.value shouldNotBe null
        val event = viewModel.events.value as? EventError
        event shouldNotBe null
        event?.messageResId shouldBeEqualTo R.string.error_player_empty_name
    }

    @Test
    fun testCreateGame() {

        runBlockingTest {
            whenever(mockPlayerRepository.createPlayer("Sébastien")).thenReturn(SKPlayer("Sébastien"))
            whenever(mockPlayerRepository.createPlayer("Hassan")).thenReturn(SKPlayer("Hassan"))
            whenever(mockGameRepository.createGame(anyList())).thenReturn(SKGame(Date()))
        }

        viewModel.createPlayer("Sébastien")
        viewModel.createPlayer("Hassan")
        viewModel.createGame()

        verifyBlocking(mockGameRepository) {
            createGame(anyList())
        }

        viewModel.events.value shouldNotBe null
        val event = viewModel.events.value as? EventGameCreated
        event shouldNotBe null
        event?.gameId shouldBeEqualTo 0
    }

    @Test
    fun testCreateGameWhenNoPlayer() {

        viewModel.createGame()

        verifyZeroInteractions(mockGameRepository)

        viewModel.events.value shouldNotBe null
        val event = viewModel.events.value as? EventError
        event shouldNotBe null
        event?.messageResId shouldBeEqualTo R.string.error_players_not_enough_selected
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        viewModel = SKOnboardingViewModel(mockGameRepository, mockPlayerRepository)
        viewModel shouldNotBe null
        viewModel.states shouldNotBe null
        viewModel.events shouldNotBe null
    }
}
