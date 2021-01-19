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
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Scene
import androidx.transition.TransitionManager
import com.google.android.material.chip.Chip
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.resetFocus
import com.sebastienbalard.skullscoring.extensions.setFocus
import com.sebastienbalard.skullscoring.extensions.showSnackBarError
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.ui.*
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewAdapter
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewMultipleSelectionAdapter
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewOnItemTouchListener
import com.sebastienbalard.skullscoring.ui.widgets.SBVerticalSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.activity_player_search.*
import kotlinx.android.synthetic.main.item_player_search.*
import kotlinx.android.synthetic.main.item_player_search.view.*
import kotlinx.android.synthetic.main.item_sort_player.view.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*

class SKPlayerSearchActivity : SBActivity(R.layout.activity_player_search) {

    internal val playerSearchViewModel: SKPlayerSearchViewModel by viewModel()

    private lateinit var scenePlayerList: Scene
    private lateinit var sceneNewPlayer: Scene
    private lateinit var sceneSortPlayerList: Scene
    private lateinit var sortPlayerListAdapter: SortPlayerListAdapter
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
                if (playerListAdapter.getSelectedItemsCount() < 6) {
                    showNewPlayerScene()
                } else {
                    toolbar.showSnackBarError(
                        getString(R.string.error_players_too_many_selected)
                    )
                }
                true
            }
            R.id.menu_player_search_item_validate -> {
                if (playerListAdapter.getSelectedItemsCount() < 2) {
                    toolbar.showSnackBarError(
                        getString(R.string.error_players_not_enough_selected)
                    )
                } else {
                    showSortPlayerScene()
                }
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
        playerSearchViewModel.events.observe(this, { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventPlayerList -> {
                        Timber.d("players count: ${players.size}")
                        playerListAdapter.setAllItems(players)
                    }
                    is EventPlayer -> {
                        showPlayerListScene()
                        playerListAdapter.insertItem(player)
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

    private fun showSortPlayerScene() {
        toolbar.subtitle = "Ordonner les joueurs"
        menuItemAdd.isVisible = false
        menuItemValidate.isVisible = false
        TransitionManager.go(sceneSortPlayerList)
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

    private fun initSceneSortPlayer() {
        sortPlayerListAdapter = SortPlayerListAdapter(
            this, listOf()
        )

        sceneSortPlayerList = Scene.getSceneForLayout(
            layoutPlayerSearch, R.layout.scene_onboarding_sort_player, this
        )
        sceneSortPlayerList.setEnterAction {
            val recycleViewSortPlayer =
                sceneSortPlayerList.sceneRoot.findViewById<RecyclerView>(R.id.recyclerViewSortPlayer)
            recycleViewSortPlayer.layoutManager = LinearLayoutManager(this)
            recycleViewSortPlayer.itemAnimator = DefaultItemAnimator()
            recycleViewSortPlayer.addItemDecoration(SBVerticalSpacingItemDecoration(32))
            val callback: ItemTouchHelper.Callback = PlayerMoveCallback(sortPlayerListAdapter)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(recycleViewSortPlayer)
            sortPlayerListAdapter.setAllItems(playerListAdapter.getSelectedItems())
            recycleViewSortPlayer.adapter = sortPlayerListAdapter

            val buttonValidateOrder =
                scenePlayerList.sceneRoot.findViewById<Button>(R.id.buttonOnboardingValidateOrder)
            buttonValidateOrder.setOnClickListener {
                playerSearchViewModel.createGame(sortPlayerListAdapter.getElements())
            }
        }
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
                            Timber.v("click on position $position")
                            playerListAdapter.toggleSelection(position)
                        }

                        override fun isEnabled(position: Int): Boolean {
                            val isEnabled =
                                playerListAdapter.getSelectedItemsCount() < 6 || playerListAdapter.isItemSelected(
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
        initSceneSortPlayer()

        TransitionManager.go(scenePlayerList)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(
                context, SKPlayerSearchActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
    }

    private inner class PlayerListAdapter(context: Context, players: List<SKPlayer>) :
        SBRecyclerViewMultipleSelectionAdapter<SKPlayer, PlayerListAdapter.ViewHolder>(
            context, players
        ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_player_search, parent, false
                )
            )
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            super.onBindViewHolder(viewHolder, position)
            viewHolder.isChecked(selectedPositions.get(position, false))
        }

        inner class ViewHolder(itemView: View) :
            SBRecyclerViewAdapter.ViewHolder<SKPlayer>(itemView) {

            override fun bind(context: Context, item: SKPlayer) {
                itemView.textViewPlayerSearchName.text = item.name
                itemView.layoutPlayerSearchChipGroup.removeAllViews()
                item.groups.forEach {
                    val chip = Chip(this@SKPlayerSearchActivity).apply {
                        id = ViewCompat.generateViewId()
                        text = it.name
                        setChipBackgroundColorResource(R.color.colorPrimary)
                        isCloseIconVisible = false
                        setTextColor(getColor(R.color.white))
                    }
                    itemView.layoutPlayerSearchChipGroup.addView(chip)
                }
            }

            fun isChecked(value: Boolean) {
                itemView.checkBoxPlayerSearch.isChecked = value
            }
        }
    }

    inner class SortPlayerListAdapter(context: Context, val players: List<SKPlayer>) :
        SBRecyclerViewAdapter<SKPlayer, SortPlayerListAdapter.ViewHolder>(context, players),
        PlayerMoveCallback.PlayerTouchListener {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_sort_player, parent, false
                )
            )
        }

        override fun onRowMoved(fromPosition: Int, toPosition: Int) {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(items, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(items, i, i - 1)
                }
            }
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onRowSelected(viewHolder: SortPlayerListAdapter.ViewHolder?) {
            //viewHolder?.itemView?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorSecondary, null))
        }

        override fun onRowClear(viewHolder: SortPlayerListAdapter.ViewHolder?) {
            notifyDataSetChanged()
            //viewHolder?.itemView?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, null))
        }

        inner class ViewHolder(itemView: View) :
            SBRecyclerViewAdapter.ViewHolder<SKPlayer>(itemView) {

            override fun bind(context: Context, item: SKPlayer) {
                itemView.imageViewDealer.visibility =
                    if (items.indexOf(item) == 0) VISIBLE else INVISIBLE
                itemView.textViewPlayerName.text = item.name
            }
        }
    }

    private class PlayerMoveCallback(private val adapter: PlayerTouchListener) :
        ItemTouchHelper.Callback() {

        override fun isLongPressDragEnabled(): Boolean {
            return true
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {}

        override fun getMovementFlags(
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
        ): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            return makeMovementFlags(dragFlags, 0)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            adapter.onRowMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSelectedChanged(
            viewHolder: RecyclerView.ViewHolder?, actionState: Int
        ) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder is SortPlayerListAdapter.ViewHolder) {
                    adapter.onRowSelected(viewHolder)
                }
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
        ) {
            super.clearView(recyclerView, viewHolder)
            if (viewHolder is SortPlayerListAdapter.ViewHolder) {
                adapter.onRowClear(viewHolder)
            }
        }

        interface PlayerTouchListener {
            fun onRowMoved(fromPosition: Int, toPosition: Int)
            fun onRowSelected(viewHolder: SortPlayerListAdapter.ViewHolder?)
            fun onRowClear(viewHolder: SortPlayerListAdapter.ViewHolder?)
        }
    }
}