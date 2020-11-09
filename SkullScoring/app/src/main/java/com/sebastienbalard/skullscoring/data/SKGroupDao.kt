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
import com.sebastienbalard.skullscoring.models.SKGroup
import java.util.*

@Dao
interface SKGroupDao : SKBaseDao<SKGroup> {

    @Query("SELECT * FROM sk_groups")
    suspend fun getAll(): List<SKGroup>

    @Query("SELECT COUNT(*) FROM sk_groups")
    suspend fun getAllCount(): Int

    @Query("SELECT * FROM sk_groups WHERE pk_group_id = :id")
    suspend fun findById(id: Long): SKGroup

    @Query("SELECT * FROM sk_groups WHERE name = :name")
    suspend fun findByName(name: String): SKGroup
}