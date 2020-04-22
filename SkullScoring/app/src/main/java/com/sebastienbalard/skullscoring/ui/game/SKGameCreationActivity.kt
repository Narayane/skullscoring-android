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
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Scene
import androidx.transition.TransitionManager
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.setFocus
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.ui.SBActivity
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_game_creation.*
import kotlinx.android.synthetic.main.item_player.view.*
import kotlinx.android.synthetic.main.scene_game_onboarding.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

open class SKGameCreationActivity : SBActivity(R.layout.activity_game_creation) {

    internal val gameViewModel: SKGameViewModel by viewModel()

    private lateinit var sceneNewPlayer: Scene
    private lateinit var scenePlayerList: Scene
    private lateinit var playerListAdapter: PlayerListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        toolbar.title = getString(R.string.new_game)

        initUI()
        initObservers()
    }

    private fun initObservers() {
        gameViewModel.events.observe(this, Observer { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventPlayerCreated -> {
                        TransitionManager.go(scenePlayerList)
                    }
                    is EventGameCreated -> {
                        startActivity(SKGameActivity.getIntent(this@SKGameCreationActivity, gameId))
                    }
                    else -> {
                    }
                }
            }
        })

        gameViewModel.players.observe(this, Observer { players ->
            playerListAdapter.players = players
            playerListAdapter.notifyDataSetChanged()
        })
    }

    private fun initUI() {
        initSceneNewPlayer()
        initScenePlayerList()

        buttonStartOnboarding.setOnClickListener {
            TransitionManager.go(sceneNewPlayer)
        }
    }

    private fun initSceneNewPlayer() {
        sceneNewPlayer =
            Scene.getSceneForLayout(layoutGameCreation, R.layout.scene_game_new_user, this)
        sceneNewPlayer.setEnterAction {
            val editTextNewPlayer =
                sceneNewPlayer.sceneRoot.findViewById<EditText>(R.id.editTextNewPlayer)
            editTextNewPlayer.setFocus(this)

            val buttonCreatePlayer =
                sceneNewPlayer.sceneRoot.findViewById<Button>(R.id.buttonCreatePlayer)
            buttonCreatePlayer.setOnClickListener {
                if (editTextNewPlayer.text.isNotEmpty()) {
                    gameViewModel.createPlayer(editTextNewPlayer.text.toString().trim())
                }
            }
        }
    }

    private fun initScenePlayerList() {
        playerListAdapter = PlayerListAdapter(listOf())
        scenePlayerList = Scene.getSceneForLayout(
            layoutGameCreation, R.layout.scene_game_player_list, this
        )
        scenePlayerList.setEnterAction {
            val recyclerViewPlayers =
                scenePlayerList.sceneRoot.findViewById<RecyclerView>(R.id.recyclerViewPlayers)
            recyclerViewPlayers.layoutManager = LinearLayoutManager(this)
            recyclerViewPlayers.itemAnimator = DefaultItemAnimator()
            recyclerViewPlayers.adapter = playerListAdapter

            val buttonAddPlayer =
                scenePlayerList.sceneRoot.findViewById<Button>(R.id.buttonAddPlayer)
            buttonAddPlayer.setOnClickListener {
                TransitionManager.go(sceneNewPlayer)
            }

            val buttonPlayGame = scenePlayerList.sceneRoot.findViewById<Button>(R.id.buttonPlayGame)
            buttonPlayGame.setOnClickListener {
                gameViewModel.createGame()
            }
        }
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(
                context, SKGameCreationActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
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
