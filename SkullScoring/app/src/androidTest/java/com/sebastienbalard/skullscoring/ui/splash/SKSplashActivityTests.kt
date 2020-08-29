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

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyBlocking
import com.sebastienbalard.skullscoring.ui.onboarding.SKSplashActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class SKSplashActivityTests {

    @get:Rule
    var activityScenarioRule = activityScenarioRule<SKSplashActivity>()
    //val rule = lazyActivityScenarioRule<SKSplashActivity>()

    private val mockSplashViewModel = mock<SKSplashViewModel>()
    private lateinit var scenario: ActivityScenario<SKSplashActivity>
    private lateinit var activity: SKSplashActivity

    @Test
    fun testOnCreate() {

        val scenario = launch(SKSplashActivity::class.java)

        // WHEN
        //scenario.moveToState(Lifecycle.State.CREATED)

        scenario.onActivity {
            /*activity = it
            whenever(activity.viewModel.states).thenReturn(MutableLiveData())
            whenever(activity.viewModel.events).thenReturn(MutableLiveData())

            val state = MutableLiveData<SBState>()
            state.postValue(StateSplashConfig)
            whenever(activity.viewModel.states).thenReturn(state)

            val events = MutableLiveData<SBEvent>()
            events.postValue(EventSplashGoToHome)
            whenever(activity.viewModel.events).thenReturn(events)*/

            verifyBlocking(mockSplashViewModel) {
                loadConfig()
            }
        }
    }

    @Before
    fun setUp() {
        startKoin {
            modules(module {
                viewModel { mockSplashViewModel }
            })
        }
        //rule.launch()
        //scenario = activityScenarioRule.scenario
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
