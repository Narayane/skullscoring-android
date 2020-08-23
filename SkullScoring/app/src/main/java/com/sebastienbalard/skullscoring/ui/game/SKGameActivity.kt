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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.formatDateTime
import com.sebastienbalard.skullscoring.models.SKGame
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.ui.EventGame
import com.sebastienbalard.skullscoring.ui.SBActivity
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewAdapter
import com.sebastienbalard.skullscoring.ui.widgets.SBSectionRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.item_game_player.view.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

open class SKGameActivity : SBActivity(R.layout.activity_game) {

    internal val gameViewModel: SKGameViewModel by viewModel()

    private lateinit var playerListAdapter: PlayerListAdapter
    private var gameId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")

        initToolbar()
        initUI()
        initObservers()

        gameId = intent.extras?.getLong(EXTRA_GAME_ID)
    }

    override fun onResume() {
        super.onResume()

        gameId?.let { gameId ->
            gameViewModel.loadGame(gameId)
        }
    }

    override fun onBackPressed() {
        if (speedDialGame.isOpen) {
            speedDialGame.close()
        } else {
            super.onBackPressed()
        }
    }

    private fun initObservers() {
        gameViewModel.events.observe(this, Observer { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventGame -> {
                        Timber.d("player count: ${game.players.size}")
                        refreshToolbar(game)
                        refreshSpeedDial(game)
                        refreshRecyclerView(game)
                    }
                    else -> {
                    }
                }
            }
        })
    }

    private fun refreshRecyclerView(game: SKGame) {
        playerListAdapter.elements = game.players
        playerListAdapter.notifyDataSetChanged()
    }

    @SuppressLint("ResourceType")
    private fun refreshSpeedDial(game: SKGame) {
        speedDialGame.visibility = if (game.isEnded) GONE else VISIBLE
        if (speedDialGame.actionItems.size > 1) {
            speedDialGame.actionItems.withIndex().filter { it.index > 0 }.forEach {
                speedDialGame.removeActionItem(it.value)
            }
        }
        if (speedDialGame.isVisible) {
            if (game.areCurrentTurnDeclarationsSet) {
                speedDialGame.addActionItem(
                    SpeedDialActionItem.Builder(2, R.drawable.ic_result_24dp).setLabel("Résultats")
                        .create()
                )
            }
            if (game.areCurrentTurnResultsSet) {
                speedDialGame.addActionItem(
                    SpeedDialActionItem.Builder(3, R.drawable.ic_skip_next_24)
                        .setLabel("Tour suivant").setFabBackgroundColor(
                            ResourcesCompat.getColor(
                                resources, R.color.colorSecondary, theme
                            )
                        ).create()
                )
            }
            if (speedDialGame.actionItems.size == 3 && game.currentTurnNumber == 10) {
                val itemEndGame =
                    SpeedDialActionItem.Builder(4, R.drawable.ic_stop_24).setLabel("Fin de partie")
                        .setFabBackgroundColor(
                            ResourcesCompat.getColor(
                                resources, R.color.colorSecondary, theme
                            )
                        ).create()
                speedDialGame.replaceActionItem(itemEndGame, 2)
            }
        }
    }

    private fun refreshToolbar(game: SKGame) {
        toolbar.title = "Partie du ${game.startDate.formatDateTime(this@SKGameActivity)}"
        toolbar.subtitle = if (game.isEnded) "Terminé" else "Tour ${game.currentTurnNumber}"
    }

    @SuppressLint("ResourceType")
    private fun initUI() {
        playerListAdapter = PlayerListAdapter(this, listOf())
        //playerListAdapter = PlayerSectionListAdapter(this,mapOf("Tour 0" to listOf()))
        recyclerViewGame.layoutManager = LinearLayoutManager(this)
        recyclerViewGame.itemAnimator = DefaultItemAnimator()
        recyclerViewGame.adapter = playerListAdapter

        speedDialGame.apply {
            addActionItem(
                SpeedDialActionItem.Builder(1, R.drawable.ic_bet_24dp).setLabel("Annonces").create()
            )
        }.setOnActionSelectedListener {
            return@setOnActionSelectedListener when (it.id) {
                1 -> {
                    gameId?.let { gameId ->
                        startActivity(
                            SKTurnActivity.getIntentForDeclarations(
                                this@SKGameActivity, gameId
                            )
                        )
                    }
                    false
                }
                2 -> {
                    gameId?.let { gameId ->
                        startActivity(
                            SKTurnActivity.getIntentForResults(
                                this@SKGameActivity, gameId
                            )
                        )
                    }
                    false
                }
                3 -> {
                    gameId?.let { gameId ->
                        gameViewModel.startNextTurn(gameId)
                    }
                    false
                }
                4 -> {
                    gameId?.let { gameId ->
                        gameViewModel.endGame(gameId)
                    }
                    false
                }
                else -> true
            }
        }
    }

    companion object {
        const val EXTRA_GAME_ID = "EXTRA_GAME_ID"

        fun getIntent(context: Context, gameId: Long): Intent {
            return Intent(
                context, SKGameActivity::class.java
            ).putExtra(EXTRA_GAME_ID, gameId)
        }
    }

    private class PlayerListAdapter(context: Context, players: List<SKPlayer>) :
        SBRecyclerViewAdapter<SKPlayer, PlayerListAdapter.ViewHolder>(context, players) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_game_player, parent, false
                )
            )
        }

        class ViewHolder(itemView: View) : SBRecyclerViewAdapter.ViewHolder<SKPlayer>(itemView) {

            override fun bind(context: Context, element: SKPlayer) {
                itemView.textViewGamePlayerName.text = element.name
                element.currentTurnDeclaration?.apply {
                    itemView.textViewGamePlayerDeclaration.visibility = VISIBLE
                    itemView.textViewGamePlayerDeclaration.text = "$this"
                } ?: run { itemView.textViewGamePlayerDeclaration.visibility = GONE }
                itemView.textViewGamePlayerDeclaration.visibility
                itemView.textViewGamePlayerScore.text = element.score.toString()
            }
        }
    }

    /*private class PlayerSectionListAdapter(
        context: Context, sections: Map<String, List<SKPlayer>>
    ) : SBSectionRecyclerViewAdapter<String, SKPlayer, PlayerSectionListAdapter.ViewHolder>(
        context, sections
    ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.section_item_game_player, parent, false
                )
            )
        }

        override fun configureFloatingHeader(header: View?, position: Int) {
            (header as? TextView)?.apply {
                text = getSections()[getSectionIndexForPosition(position)]
            }
        }

        override fun getSectionLabel(key: String): String {
            return key
        }

        class ViewHolder(itemView: View) :
            SBSectionRecyclerViewAdapter.ViewHolder<SKPlayer>(itemView) {

            override fun bind(context: Context, element: SKPlayer) {
                itemView.textViewGamePlayerName.text = element.name
                itemView.textViewGamePlayerScore.text = "0"
            }
        }
    }*/
}
