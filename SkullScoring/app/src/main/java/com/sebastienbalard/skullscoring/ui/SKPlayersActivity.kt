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

package com.sebastienbalard.skullscoring.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.ui.game.SKPlayerSearchViewModel
import com.sebastienbalard.skullscoring.ui.home.SKHomeActivity
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_players.*
import kotlinx.android.synthetic.main.activity_turn.*
import kotlinx.android.synthetic.main.item_player.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SKPlayersActivity : SBBottomNavigationViewActivity(R.layout.activity_players) {

    internal val playerSearchViewModel: SKPlayerSearchViewModel by viewModel()

    private lateinit var playerListAdapter: PlayerListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")

        initToolbar(false)
        initUI()
        initObservers()

        playerSearchViewModel.loadPlayers()
    }

    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.menu_bottom_navigation_item_players
    }

    private fun initObservers() {
        playerSearchViewModel.events.observe(this, { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventPlayerList -> {
                        Timber.d("players count: ${players.size}")
                        playerListAdapter.setAllItems(players)
                    }
                    else -> {
                    }
                }
            }
        })
    }

    private fun initUI() {
        playerListAdapter = PlayerListAdapter(this, listOf())
        recycleViewPlayers.layoutManager = LinearLayoutManager(this)
        recycleViewPlayers.itemAnimator = DefaultItemAnimator()
        recycleViewPlayers.adapter = playerListAdapter
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(
                context, SKPlayersActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private class PlayerListAdapter(context: Context, players: List<SKPlayer>) :
        SBRecyclerViewAdapter<SKPlayer, PlayerListAdapter.ViewHolder>(context, players) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_player, parent, false
                )
            )
        }

        class ViewHolder(itemView: View) : SBRecyclerViewAdapter.ViewHolder<SKPlayer>(itemView) {

            override fun bind(context: Context, item: SKPlayer) {
                itemView.textViewPlayerName.text = item.name
            }
        }
    }
}