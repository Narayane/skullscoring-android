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

package com.sebastienbalard.skullscoring.models

import androidx.annotation.NonNull
import androidx.room.*
import java.util.*

@Entity(
    tableName = "sk_games",
    indices = [Index(value = ["pk_game_id"]), Index(value = ["start_date"], unique = true)]
)
data class SKGame(
    @ColumnInfo(name = "start_date") @NonNull var startDate: Date
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pk_game_id")
    @NonNull
    var id: Long = 0

    @ColumnInfo(name = "current_turn_number")
    @NonNull
    var currentTurnNumber: Int = 1

    @Ignore
    lateinit var players: List<SKPlayer>

    @Ignore
    lateinit var state: GameState

    constructor() : this(Date())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SKGame

        if (startDate != other.startDate) return false

        return true
    }

    override fun hashCode(): Int {
        return startDate.hashCode()
    }
}

sealed class GameState
object Ongoing : GameState()
object Finished : GameState()