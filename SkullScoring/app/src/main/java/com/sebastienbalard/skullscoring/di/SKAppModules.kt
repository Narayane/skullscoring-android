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

package com.sebastienbalard.skullscoring.di

import androidx.preference.PreferenceManager
import androidx.room.Room
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.sebastienbalard.skullscoring.BuildConfig
import com.sebastienbalard.skullscoring.SBAnalytics
import com.sebastienbalard.skullscoring.SBCrashReport
import com.sebastienbalard.skullscoring.SKApplication
import com.sebastienbalard.skullscoring.data.SKDatabase
import com.sebastienbalard.skullscoring.data.SKDatabase.Companion.MIGRATION_1_2
import com.sebastienbalard.skullscoring.repositories.*
import com.sebastienbalard.skullscoring.ui.game.SKGameViewModel
import com.sebastienbalard.skullscoring.ui.game.SKPlayerSearchViewModel
import com.sebastienbalard.skullscoring.ui.game.SKTurnViewModel
import com.sebastienbalard.skullscoring.ui.home.SKHomeViewModel
import com.sebastienbalard.skullscoring.ui.onboarding.SKOnboardingViewModel
import com.sebastienbalard.skullscoring.ui.players.SKPlayerViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.experimental.dsl.viewModel
import org.koin.dsl.module
import org.koin.experimental.builder.single

val dataModule = module {
    single {
        Room.databaseBuilder(androidContext(), SKDatabase::class.java, BuildConfig.ROOM_DB_NAME)
            .addMigrations(MIGRATION_1_2).build()
    }
    single { get<SKDatabase>().getGameDao() }
    single { get<SKDatabase>().getPlayerDao() }
    single { get<SKDatabase>().getTurnDao() }
    single { get<SKDatabase>().getGamePlayerJoinDao() }
    single { get<SKDatabase>().getTurnPlayerJoinDao() }
    single { get<SKDatabase>().getGroupDao() }
    single { get<SKDatabase>().getPlayerGroupJoinDao() }
}

val commonModule = module {
    single { androidApplication() as SKApplication }
    single { PreferenceManager.getDefaultSharedPreferences(androidContext()) }

    single { FirebaseAnalytics.getInstance(androidContext()) }
    single { FirebaseCrashlytics.getInstance() }

    single<SBAnalytics>()
    single<SBCrashReport>()

    single<SKGameRepository>()
    single<SKPlayerRepository>()
    single<SKTurnRepository>()
    single<SKPreferenceRepository>()
    single<SKGroupRepository>()
}

val appModule = module {
    viewModel<SKOnboardingViewModel>()
    viewModel<SKPlayerSearchViewModel>()
    viewModel<SKHomeViewModel>()
    viewModel<SKGameViewModel>()
    viewModel<SKTurnViewModel>()
    viewModel<SKPlayerViewModel>()
}

val skullScoringApp = listOf(appModule, commonModule, dataModule)