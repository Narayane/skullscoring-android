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
import android.util.SparseBooleanArray
import android.view.*
import androidx.core.util.keyIterator
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.showSnackBarError
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.ui.EventError
import com.sebastienbalard.skullscoring.ui.SBActivity
import com.sebastienbalard.skullscoring.ui.onboarding.EventGameCreated
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewAdapter
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewOnItemTouchListener
import kotlinx.android.synthetic.main.activity_player_search.*
import kotlinx.android.synthetic.main.item_player_search.view.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SKPlayerSearchActivity : SBActivity(R.layout.activity_player_search) {

    internal val playerSearchViewModel: SKPlayerSearchViewModel by viewModel()

    private lateinit var playerListAdapter: PlayerListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        toolbar.title = "Nouvelle partie"
        toolbar.subtitle = "Sélectionner les joueurs"

        initToolbar()
        initUI()
        initObservers()

        playerSearchViewModel.loadPlayers()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Timber.v("onCreateOptionsMenu")
        menuInflater.inflate(R.menu.menu_player_search, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_player_search_item_add -> {

                true
            }
            R.id.menu_player_search_item_validate -> {
                playerSearchViewModel.createGame(playerListAdapter.selectedPlayers)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initObservers() {
        playerSearchViewModel.events.observe(this, Observer { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventPlayerList -> {
                        Timber.d("players count: ${players.size}")
                        playerListAdapter.elements = players
                        playerListAdapter.notifyDataSetChanged()
                    }
                    is EventGameCreated -> {
                        startActivity(
                            SKGameActivity.getIntent(
                                this@SKPlayerSearchActivity, gameId
                            )
                        )
                    }
                    is EventError -> toolbar.showSnackBarError(
                        this@SKPlayerSearchActivity, messageResId, Snackbar.LENGTH_SHORT
                    )
                    else -> {
                    }
                }
            }
        })
    }

    private fun initUI() {
        playerListAdapter = PlayerListAdapter(this, listOf())
        recycleViewPlayerSearch.layoutManager = LinearLayoutManager(this)
        recycleViewPlayerSearch.itemAnimator = DefaultItemAnimator()
        recycleViewPlayerSearch.adapter = playerListAdapter
        recycleViewPlayerSearch.addOnItemTouchListener(
            SBRecyclerViewOnItemTouchListener(this,
                recycleViewPlayerSearch,
                object : SBRecyclerViewOnItemTouchListener.OnItemTouchListener {
                    override fun onClick(viewHolder: RecyclerView.ViewHolder, position: Int) {
                        Timber.v("onClick")
                        playerListAdapter.toggleSelection(position)
                    }

                    override fun isEnabled(position: Int): Boolean {
                        return playerListAdapter.selectedPlayers.count() < 6 || playerListAdapter.isPlayerSelected(
                            position
                        )
                    }
                })
        )
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(
                context, SKPlayerSearchActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
    }

    private class PlayerListAdapter(context: Context, players: List<SKPlayer>) :
        SBRecyclerViewAdapter<SKPlayer, PlayerListAdapter.ViewHolder>(context, players) {

        val selectedPlayers: List<SKPlayer>
            get() = selectedElements.keyIterator().asSequence().toList().map { elements[it] }

        private var selectedElements: SparseBooleanArray = SparseBooleanArray()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_player_search, parent, false
                )
            )
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            super.onBindViewHolder(viewHolder, position)
            viewHolder.isChecked(selectedElements.get(position, false))
        }

        fun isPlayerSelected(position: Int): Boolean {
            return selectedElements.get(position, false)
        }

        fun toggleSelection(position: Int) {
            if (selectedElements.get(position, false)) {
                selectedElements.delete(position)
            } else {
                selectedElements.put(position, true)
            }
            notifyItemChanged(position)
        }

        class ViewHolder(itemView: View) : SBRecyclerViewAdapter.ViewHolder<SKPlayer>(itemView) {

            override fun bind(context: Context, element: SKPlayer) {
                itemView.textViewPlayerSearchName.text = element.name
            }

            fun isChecked(value: Boolean) {
                itemView.checkBoxPlayerSearch.isChecked = value
            }
        }
    }
}