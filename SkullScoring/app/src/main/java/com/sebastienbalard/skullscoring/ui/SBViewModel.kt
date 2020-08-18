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

package com.sebastienbalard.skullscoring.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sebastienbalard.skullscoring.models.SKGame
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.models.SKTurn

open class SBState
object StateLoading : SBState()
data class StateError(val error: Throwable) : SBState()
data class StateTurnDeclarations(val turn: SKTurn) : SBState()
data class StateTurnResults(val turn: SKTurn) : SBState()

object StateSplashConfig : SBState()

open class SBEvent
open class EventError(val messageResId: Int) : SBEvent()
open class EventErrorWithArg(val messageResId: Int, val arg: Any) : SBEvent()

object EventSplashStartOnboarding : SBEvent()
object EventSplashGoToHome : SBEvent()
object EventPlayerCreated : SBEvent()
data class EventPlayerList(val players: List<SKPlayer>) : SBEvent()
data class EventPlayer(val player: SKPlayer) : SBEvent()
data class EventGame(val game: SKGame) : SBEvent()
data class EventGameCreated(val gameId: Long) : SBEvent()
data class EventGameAtLeastOne(val hasAtLeastOneGame: Boolean) : SBEvent()
data class EventGameList(val games: List<SKGame>) : SBEvent()
object EventTurnDeclarationsUpdated : SBEvent()
object EventTurnResultsUpdated: SBEvent()

abstract class SBViewModel : ViewModel() {

    protected var _states = MutableLiveData<SBState>()
    open val states: LiveData<SBState>
        get() = _states

    protected var _events = MutableLiveData<SBEvent>()
    open val events: LiveData<SBEvent>
        get() = _events
}