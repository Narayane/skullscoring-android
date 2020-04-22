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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.formatDateTime
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.ui.SBActivity
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.item_player.view.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

open class SKGameActivity : SBActivity(R.layout.activity_game) {

    internal val gameViewModel: SKGameViewModel by viewModel()

    private lateinit var playerListAdapter: PlayerListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")

        initToolbar()
        initUI()
        initObservers()

        intent.extras?.getLong(EXTRA_GAME_ID)?.let { gameId ->
            gameViewModel.loadGame(gameId)
        }
    }

    private fun initObservers() {
        gameViewModel.events.observe(this, Observer { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventGame -> {
                        Timber.d("load a game with ${game.players.size} players")
                        toolbar.title =
                            "Partie du ${game.startDate.formatDateTime(this@SKGameActivity)}"
                        playerListAdapter.players = game.players
                        playerListAdapter.notifyDataSetChanged()
                    }
                    else -> {
                    }
                }
            }
        })
    }

    private fun initUI() {
        playerListAdapter = PlayerListAdapter(listOf())
        recyclerViewGame.layoutManager = LinearLayoutManager(this)
        recyclerViewGame.itemAnimator = DefaultItemAnimator()
        recyclerViewGame.adapter = playerListAdapter
    }

    companion object {
        const val EXTRA_GAME_ID = "EXTRA_GAME_ID"

        fun getIntent(context: Context, gameId: Long): Intent {
            return Intent(
                context, SKGameActivity::class.java
            ).putExtra(EXTRA_GAME_ID, gameId)
        }
    }

    private class PlayerListAdapter(var players: List<SKPlayer>) :
        RecyclerView.Adapter<PlayerListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_player, parent, false
                )
            )
        }

        override fun onBindViewHolder(
            viewHolder: ViewHolder, position: Int
        ) {
            viewHolder.bind(players[position])
        }

        override fun getItemCount() = players.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bind(player: SKPlayer) {
                itemView.textViewPlayerName.text = player.name
            }
        }
    }
}
