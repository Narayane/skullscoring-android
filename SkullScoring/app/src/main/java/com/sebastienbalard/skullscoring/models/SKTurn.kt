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
import com.sebastienbalard.skullscoring.models.SKGame

@Entity(
    tableName = "sk_turns",
    foreignKeys = [ForeignKey(
        entity = SKGame::class,
        parentColumns = ["pk_game_id"],
        childColumns = ["fk_game_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["fk_game_id"])]
)
data class SKTurn(
    @ColumnInfo(name = "number")
    @NonNull
    var number: Int,
    @ColumnInfo(name = "fk_game_id")
    @NonNull
    val gameId: Long
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pk_turn_id")
    @NonNull
    var id: Long = 0

    @Ignore
    lateinit var results: List<SKTurnPlayerJoin>
}