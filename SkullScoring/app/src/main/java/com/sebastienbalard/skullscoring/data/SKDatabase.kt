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

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sebastienbalard.skullscoring.models.*


@Database(
    entities = [SKGame::class, SKPlayer::class, SKGamePlayerJoin::class, SKTurn::class, SKTurnPlayerJoin::class, SKGroup::class, SKPlayerGroupJoin::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class SKDatabase : RoomDatabase() {
    abstract fun getGameDao(): SKGameDao
    abstract fun getPlayerDao(): SKPlayerDao
    abstract fun getTurnDao(): SKTurnDao
    abstract fun getGamePlayerJoinDao(): SKGamePlayerJoinDao
    abstract fun getTurnPlayerJoinDao(): SKTurnPlayerJoinDao
    abstract fun getGroupDao(): SKGroupDao
    abstract fun getPlayerGroupJoinDao(): SKPlayerGroupJoinDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE `sk_groups` (`pk_group_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)"
                )
                database.execSQL(
                    "CREATE INDEX `index_sk_groups_pk_group_id` ON `sk_groups` (`pk_group_id`)"
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX `index_sk_groups_name` ON `sk_groups` (`name`)"
                )
                database.execSQL(
                    "CREATE TABLE `sk_player_group_joins` (`fk_player_id` INTEGER NOT NULL, `fk_group_id` INTEGER NOT NULL, PRIMARY KEY(`fk_player_id`, `fk_group_id`), FOREIGN KEY(`fk_player_id`) REFERENCES `sk_players`(`pk_player_id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`fk_group_id`) REFERENCES `sk_groups`(`pk_group_id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                database.execSQL(
                    "CREATE INDEX `index_sk_player_group_joins_fk_player_id` ON `sk_player_group_joins` (`fk_player_id`)"
                )
                database.execSQL(
                    "CREATE INDEX `index_sk_player_group_joins_fk_group_id` ON `sk_player_group_joins` (`fk_group_id`)"
                )
            }
        }
    }
}