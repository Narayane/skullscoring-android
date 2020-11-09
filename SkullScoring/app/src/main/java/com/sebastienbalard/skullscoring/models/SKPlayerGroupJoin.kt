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
    tableName = "sk_player_group_joins",
    primaryKeys = ["fk_player_id", "fk_group_id"],
    foreignKeys = [ForeignKey(
        entity = SKPlayer::class,
        parentColumns = ["pk_player_id"],
        childColumns = ["fk_player_id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = SKGroup::class,
        parentColumns = ["pk_group_id"],
        childColumns = ["fk_group_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["fk_player_id"]), Index(value = ["fk_group_id"])]
)
data class SKPlayerGroupJoin(
    @ColumnInfo(name = "fk_player_id")
    val playerId: Long,
    @ColumnInfo(name = "fk_group_id")
    var gameId: Long
)