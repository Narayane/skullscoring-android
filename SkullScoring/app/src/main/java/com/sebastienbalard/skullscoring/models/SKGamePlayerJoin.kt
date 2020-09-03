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
    tableName = "sk_game_player_joins",
    primaryKeys = ["fk_game_id", "fk_player_id"],
    foreignKeys = [ForeignKey(
        entity = SKGame::class,
        parentColumns = ["pk_game_id"],
        childColumns = ["fk_game_id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = SKPlayer::class,
        parentColumns = ["pk_player_id"],
        childColumns = ["fk_player_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["fk_game_id"]), Index(value = ["fk_player_id"])]
)
data class SKGamePlayerJoin(
    @ColumnInfo(name = "fk_game_id")
    var gameId: Long,
    @ColumnInfo(name = "fk_player_id")
    val playerId: Long,
    @ColumnInfo(name = "position")
    val position: Int
)