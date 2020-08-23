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

package com.sebastienbalard.skullscoring.data

import androidx.room.Dao
import androidx.room.Query
import com.sebastienbalard.skullscoring.models.SKTurn

@Dao
interface SKTurnDao : SKBaseDao<SKTurn> {

    @Query("SELECT COUNT(*) FROM sk_turns")
    suspend fun getAllCount(): Int

    @Query("SELECT COUNT(*) FROM sk_turns WHERE fk_game_id = :gameId")
    suspend fun getCountByGame(gameId: Long): Int

    @Query("SELECT * FROM sk_turns WHERE fk_game_id = :gameId")
    suspend fun findByGame(gameId: Long): List<SKTurn>

    @Query("SELECT * FROM sk_turns WHERE fk_game_id = :gameId AND number = :number")
    suspend fun findByNumber(gameId: Long, number: Int): SKTurn

    @Query("SELECT * FROM sk_turns WHERE pk_turn_id = :turnId")
    suspend fun findById(turnId: Long): SKTurn
}