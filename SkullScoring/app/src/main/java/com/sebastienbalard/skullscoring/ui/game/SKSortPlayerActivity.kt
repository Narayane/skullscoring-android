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

package com.sebastienbalard.skullscoring.ui.game

import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.ui.SBActivity
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SKSortPlayerActivity : SBActivity(R.layout.activity_sort_players) {

    internal val playerSearchViewModel: SKPlayerSearchViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        toolbar.title = "Nouvelle partie"
        toolbar.subtitle = "Ordonner les joueurs"

        initToolbar(true)
        initUI()
        initObservers()

        playerSearchViewModel.loadPlayers()
    }

    private fun initObservers() {
        playerSearchViewModel.events.observe(this, { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    else -> {
                    }
                }
            }
        })
    }

    private fun initUI() {
        //playerListAdapter = SKGameActivity.PlayerListAdapter(this, listOf())
        //recycleViewSortPlayer.layoutManager = LinearLayoutManager(this)
        //recycleViewSortPlayer.itemAnimator = DefaultItemAnimator()
        //recycleViewSortPlayer.adapter = playerListAdapter
    }
}