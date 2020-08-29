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
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.core.util.keyIterator
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Scene
import androidx.transition.TransitionManager
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.resetFocus
import com.sebastienbalard.skullscoring.extensions.setFocus
import com.sebastienbalard.skullscoring.extensions.showSnackBarError
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.ui.*
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewAdapter
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewOnItemTouchListener
import kotlinx.android.synthetic.main.activity_player_search.*
import kotlinx.android.synthetic.main.item_player_search.view.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SKPlayerSearchActivity : SBActivity(R.layout.activity_player_search) {

    internal val playerSearchViewModel: SKPlayerSearchViewModel by viewModel()

    private lateinit var scenePlayerList: Scene
    private lateinit var sceneNewPlayer: Scene
    private lateinit var playerListAdapter: PlayerListAdapter

    private lateinit var menuItemAdd: MenuItem
    private lateinit var menuItemValidate: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        toolbar.title = "Nouvelle partie"
        toolbar.subtitle = "Sélectionner les joueurs"

        initToolbar(true)
        initUI()
        initObservers()

        playerSearchViewModel.loadPlayers()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Timber.v("onCreateOptionsMenu")
        menuInflater.inflate(R.menu.menu_player_search, menu)
        menuItemAdd = menu.findItem(R.id.menu_player_search_item_add)
        menuItemValidate = menu.findItem(R.id.menu_player_search_item_validate)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_player_search_item_add -> {
                if (playerListAdapter.selectedPlayers.size < 6) {
                    showNewPlayerScene()
                } else {
                    toolbar.showSnackBarError(
                        getString(R.string.error_players_too_many_selected)
                    )
                }
                true
            }
            R.id.menu_player_search_item_validate -> {
                playerSearchViewModel.createGame(playerListAdapter.selectedPlayers)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        Scene.getCurrentScene(layoutPlayerSearch)?.let { currentScene ->
            if (currentScene == sceneNewPlayer) {
                showPlayerListScene()
            } else {
                super.onBackPressed()
            }
        } ?: super.onBackPressed()
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
                    is EventPlayer -> {
                        showPlayerListScene()
                        val players = mutableListOf<SKPlayer>().apply {
                            addAll(playerListAdapter.elements)
                            add(player)
                            sortBy { it.name }
                        }
                        playerListAdapter.elements = players
                        val index = players.indexOf(player)
                        playerListAdapter.toggleSelection(index)
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
                        getString(messageResId)
                    )
                    is EventErrorWithArg -> toolbar.showSnackBarError(
                        getString(messageResId, arg)
                    )
                    else -> {
                    }
                }
            }
        })
    }

    private fun showPlayerListScene() {
        toolbar.subtitle = "Sélectionner les joueurs"
        menuItemAdd.isVisible = true
        menuItemValidate.isVisible = true
        TransitionManager.go(scenePlayerList)
    }

    private fun showNewPlayerScene() {
        toolbar.subtitle = "Ajouter un joueur"
        menuItemAdd.isVisible = false
        menuItemValidate.isVisible = false
        TransitionManager.go(sceneNewPlayer)
    }

    private fun initSceneNewPlayer() {
        sceneNewPlayer =
            Scene.getSceneForLayout(layoutPlayerSearch, R.layout.scene_onboarding_new_user, this)
        sceneNewPlayer.setEnterAction {
            val editTextNewPlayer =
                sceneNewPlayer.sceneRoot.findViewById<EditText>(R.id.editTextOnboardingNewPlayer)
            editTextNewPlayer.setFocus(this)
            editTextNewPlayer.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    editTextNewPlayer.resetFocus()
                    playerSearchViewModel.createPlayer(editTextNewPlayer.text.toString().trim())
                }
                true
            }

            val buttonCreatePlayer =
                sceneNewPlayer.sceneRoot.findViewById<Button>(R.id.buttonOnboardingCreatePlayer)
            buttonCreatePlayer.setOnClickListener {
                editTextNewPlayer.resetFocus()
                playerSearchViewModel.createPlayer(editTextNewPlayer.text.toString().trim())
            }
        }
    }

    private fun initScenePlayerList() {
        playerListAdapter = PlayerListAdapter(this, listOf())
        scenePlayerList = Scene.getSceneForLayout(
            layoutPlayerSearch, R.layout.scene_player_search_player_list, this
        )
        scenePlayerList.setEnterAction {
            val recycleViewPlayerSearch =
                scenePlayerList.sceneRoot.findViewById<RecyclerView>(R.id.recycleViewPlayerSearch)
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
                            val isEnabled =
                                playerListAdapter.selectedPlayers.count() < 6 || playerListAdapter.isPlayerSelected(
                                    position
                                )
                            if (!isEnabled) {
                                toolbar.showSnackBarError(
                                    getString(R.string.error_players_too_many_selected)
                                )
                            }
                            return isEnabled
                        }
                    })
            )
        }
    }

    private fun initUI() {
        initScenePlayerList()
        initSceneNewPlayer()

        TransitionManager.go(scenePlayerList)
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