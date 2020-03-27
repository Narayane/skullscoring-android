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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "sk_turn_player_joins",
    primaryKeys = ["fk_turn_id", "fk_player_id"],
    foreignKeys = [ForeignKey(
        entity = SKTurn::class,
        parentColumns = ["pk_turn_id"],
        childColumns = ["fk_turn_id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = SKPlayer::class,
        parentColumns = ["pk_player_id"],
        childColumns = ["fk_player_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["fk_turn_id"]), Index(value = ["fk_player_id"])]
)
data class SKTurnPlayerJoin(
    @ColumnInfo(name = "fk_turn_id") var turnId: Long,
    @ColumnInfo(name = "fk_player_id") val playerId: Long
) {
    @ColumnInfo(name = "declaration")
    var declaration: Int? = null

    @ColumnInfo(name = "result")
    var result: Int? = null
}