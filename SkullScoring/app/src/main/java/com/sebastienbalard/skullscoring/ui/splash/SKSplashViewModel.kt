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

import androidx.lifecycle.viewModelScope
import com.sebastienbalard.skullscoring.repositories.SKGameRepository
import com.sebastienbalard.skullscoring.ui.SBEvent
import com.sebastienbalard.skullscoring.ui.SBState
import com.sebastienbalard.skullscoring.ui.SBViewModel
import kotlinx.coroutines.launch

object StateSplashConfig : SBState()

object EventSplashStartOnboarding : SBEvent()
object EventSplashGoToHome : SBEvent()

open class SKSplashViewModel(private val gameRepository: SKGameRepository) : SBViewModel() {

    open fun loadConfig() = viewModelScope.launch {
        _states.value = StateSplashConfig
        if (gameRepository.hasGame()) {
            _events.value = EventSplashGoToHome
        } else {
            _events.value = EventSplashStartOnboarding
        }
    }
}