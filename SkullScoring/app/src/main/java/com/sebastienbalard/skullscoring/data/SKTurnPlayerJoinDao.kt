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
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.models.SKTurn
import com.sebastienbalard.skullscoring.models.SKTurnPlayerJoin

@Dao
interface SKTurnPlayerJoinDao : SKBaseDao<SKTurnPlayerJoin> {

    @Query("SELECT * FROM sk_turns INNER JOIN sk_turn_player_joins ON sk_turns.pk_turn_id = sk_turn_player_joins.fk_turn_id WHERE sk_turn_player_joins.fk_player_id = :playerId")
    suspend fun findTurnByPlayer(playerId: Long): List<SKTurn>

    @Query("SELECT * FROM sk_players INNER JOIN sk_turn_player_joins ON sk_players.pk_player_id = sk_turn_player_joins.fk_player_id WHERE sk_turn_player_joins.fk_turn_id = :turnId")
    suspend fun findPlayerByTurn(turnId: Long): List<SKPlayer>
}