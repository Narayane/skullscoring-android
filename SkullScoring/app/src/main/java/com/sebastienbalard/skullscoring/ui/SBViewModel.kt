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

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class SBState
object StateLoading : SBState()
data class StateError(val error: Throwable) : SBState()

open class SBEvent
object EventSuccess : SBEvent()
data class EventMessage(val message: String) : SBEvent()
data class EventFailure(val error: Throwable) : SBEvent()

abstract class SBViewModel : ViewModel() {

    protected var _states = MutableLiveData<SBState>()
    open val states: LiveData<SBState>
        get() = _states

    protected var _events = MutableLiveData<SBEvent>()
    open val events: LiveData<SBEvent>
        get() = _events
}