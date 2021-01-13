/**
 * Copyright © 2021 Skull Scoring (Sébastien BALARD)
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

package com.sebastienbalard.skullscoring.repositories

import android.database.sqlite.SQLiteConstraintException
import com.sebastienbalard.skullscoring.data.SKGamePlayerJoinDao
import com.sebastienbalard.skullscoring.data.SKGroupDao
import com.sebastienbalard.skullscoring.data.SKPlayerGroupJoinDao
import com.sebastienbalard.skullscoring.models.SKGroup
import timber.log.Timber

open class SKGroupRepository(
    private val groupDao: SKGroupDao, private val playerGroupJoinDao: SKPlayerGroupJoinDao
) {

    open suspend fun createGroup(name: String): SKGroup {
        try {
            groupDao.insert(SKGroup(name))
        } catch (e: SQLiteConstraintException) {
        } finally {
            return groupDao.findByName(name)
        }
    }

    open suspend fun deleteOrphanGroups() {
        groupDao.getAll().forEach { group ->
            if (playerGroupJoinDao.findPlayerByGroup(group.id).isEmpty()) {
                Timber.d("delete orphan group: ${group.name}")
                groupDao.delete(group)
            }
        }
    }
}