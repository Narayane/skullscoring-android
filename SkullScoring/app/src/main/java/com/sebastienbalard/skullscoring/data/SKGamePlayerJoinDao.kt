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
import com.sebastienbalard.skullscoring.models.SKGame
import com.sebastienbalard.skullscoring.models.SKGamePlayerJoin
import com.sebastienbalard.skullscoring.models.SKPlayer

@Dao
interface SKGamePlayerJoinDao : SKBaseDao<SKGamePlayerJoin> {

    @Query("SELECT * FROM sk_games INNER JOIN sk_game_player_joins ON sk_games.pk_game_id = sk_game_player_joins.fk_game_id WHERE sk_game_player_joins.fk_player_id = :playerId")
    suspend fun findGameByPlayer(playerId: Long): List<SKGame>

    @Query("SELECT * FROM sk_players INNER JOIN sk_game_player_joins ON sk_players.pk_player_id = sk_game_player_joins.fk_player_id WHERE sk_game_player_joins.fk_game_id = :gameId")
    suspend fun findPlayerByGame(gameId: Long): List<SKPlayer>
}