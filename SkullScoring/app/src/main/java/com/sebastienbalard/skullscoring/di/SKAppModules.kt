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

import androidx.room.Room
import com.sebastienbalard.skullscoring.BuildConfig
import com.sebastienbalard.skullscoring.SKApplication
import com.sebastienbalard.skullscoring.repositories.SKGameRepository
import com.sebastienbalard.skullscoring.repositories.SKPlayerRepository
import com.sebastienbalard.skullscoring.data.SKDatabase
import com.sebastienbalard.skullscoring.ui.game.SKGameViewModel
import com.sebastienbalard.skullscoring.ui.home.SKHomeViewModel
import com.sebastienbalard.skullscoring.ui.splash.SKSplashViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.experimental.dsl.viewModel
import org.koin.dsl.module
import org.koin.experimental.builder.single

val dataModule = module {
    single {
        Room.databaseBuilder(androidContext(), SKDatabase::class.java, BuildConfig.ROOM_DB_NAME)
            .build()
    }
    single { get<SKDatabase>().getGameDao() }
    single { get<SKDatabase>().getPlayerDao() }
    single { get<SKDatabase>().getTurnDao() }
    single { get<SKDatabase>().getGamePlayerJoinDao() }
    single { get<SKDatabase>().getTurnPlayerJoinDao() }
}

val commonModule = module {
    single { androidApplication() as SKApplication }
    single<SKGameRepository>()
    single<SKPlayerRepository>()
}

val appModule = module {
    viewModel<SKSplashViewModel>()
    viewModel<SKHomeViewModel>()
    viewModel<SKGameViewModel>()
}

val skullScoringApp = listOf(appModule, commonModule, dataModule)