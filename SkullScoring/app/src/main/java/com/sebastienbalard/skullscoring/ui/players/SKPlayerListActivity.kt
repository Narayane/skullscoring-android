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

package com.sebastienbalard.skullscoring.ui.players

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.showSnackBarWarning
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.ui.EventPlayerList
import com.sebastienbalard.skullscoring.ui.SBBottomNavigationViewActivity
import com.sebastienbalard.skullscoring.ui.game.SKPlayerSearchViewModel
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewAdapter
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewMultipleSelectionAdapter
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewOnItemTouchListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_player_list.*
import kotlinx.android.synthetic.main.item_player_group.view.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SKPlayerListActivity : SBBottomNavigationViewActivity(R.layout.activity_player_list) {

    internal val playerSearchViewModel: SKPlayerSearchViewModel by viewModel()

    private lateinit var playerListAdapter: PlayerListAdapter
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")

        initToolbar(false)
        initUI()
        initObservers()
    }

    override fun onResume() {
        super.onResume()

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

    private fun performSelection(position: Int) {
        playerListAdapter.toggleSelection(position)
        if (playerListAdapter.getSelectedItemsCount() == 0) {
            actionMode?.finish()
        } else {
            actionMode?.title = resources.getQuantityString(
                R.plurals.plural_player_selected,
                playerListAdapter.getSelectedItemsCount(),
                playerListAdapter.getSelectedItemsCount()
            )
        }
    }

    private fun initUI() {
        playerListAdapter = PlayerListAdapter(this, listOf())
        recycleViewPlayers.layoutManager = LinearLayoutManager(this)
        recycleViewPlayers.itemAnimator = DefaultItemAnimator()
        recycleViewPlayers.adapter = playerListAdapter
        recycleViewPlayers.addOnItemTouchListener(
            SBRecyclerViewOnItemTouchListener(this,
                recycleViewPlayers,
                object : SBRecyclerViewOnItemTouchListener.OnItemTouchListener {

                    override fun onClick(viewHolder: RecyclerView.ViewHolder, position: Int) {
                        Timber.v("onClick")
                        val selectedPlayer = playerListAdapter.getElements()[position]
                        actionMode?.let {
                            if (selectedPlayer.isDeletable) {
                                performSelection(position)
                            } else {
                                toolbar.showSnackBarWarning(getString(R.string.warning_player_not_deletable))
                            }
                        } ?: run {
                            startActivity(
                                SKPlayerActivity.getIntentToEdit(
                                    this@SKPlayerListActivity, selectedPlayer.id
                                )
                            )
                        }
                    }

                    override fun onLongClick(viewHolder: RecyclerView.ViewHolder, position: Int) {
                        Timber.v("onLongClick")
                        val selectedPlayer = playerListAdapter.getElements()[position]
                        if (selectedPlayer.isDeletable) {
                            if (actionMode == null) {
                                startActionMode(actionModeCallback)
                            }
                            performSelection(position)
                        } else {
                            toolbar.showSnackBarWarning(getString(R.string.warning_player_not_deletable))
                        }
                    }

                    override fun isEnabled(position: Int): Boolean {
                        return true
                    }
                })
        )

        fabNewPlayer.setOnClickListener {
            startActivity(SKPlayerActivity.getIntent(this))
        }
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(
                context, SKPlayerListActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {

        override fun onCreateActionMode(
            mode: ActionMode?, menu: Menu?
        ): Boolean {
            mode?.menuInflater?.inflate(R.menu.menu_player_list_contextual, menu)
            actionMode = mode
            return true
        }

        override fun onPrepareActionMode(
            mode: ActionMode?, menu: Menu?
        ): Boolean {
            return false
        }

        override fun onActionItemClicked(
            mode: ActionMode?, item: MenuItem?
        ): Boolean {
            when (item?.itemId) {
                R.id.menu_player_list_contextual_delete -> {
                    playerSearchViewModel.deletePlayer(
                        *(playerListAdapter.getSelectedItems().toTypedArray())
                    )
                    actionMode?.finish()
                }
                else -> {
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            playerListAdapter.clearSelection()
            actionMode = null
        }
    }

    private inner class PlayerListAdapter(context: Context, players: List<SKPlayer>) :
        SBRecyclerViewMultipleSelectionAdapter<SKPlayer, PlayerListAdapter.ViewHolder>(
            context, players
        ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_player_group, parent, false
                )
            )
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            super.onBindViewHolder(viewHolder, position)
            if (getSelectedItemsPositions().contains(position)) {
                viewHolder.itemView.background = ContextCompat.getDrawable(
                    context, R.color.colorSecondary
                )
            } else {
                viewHolder.itemView.background =
                    ContextCompat.getDrawable(context, R.drawable.ripple)
            }
        }

        inner class ViewHolder(itemView: View) :
            SBRecyclerViewAdapter.ViewHolder<SKPlayer>(itemView) {

            override fun bind(context: Context, item: SKPlayer) {
                itemView.textViewPlayerGroupName.text = item.name
                itemView.layoutPlayerGroupChipGroup.removeAllViews()
                if (item.groups.isNotEmpty()) {
                    item.groups.sortedBy { it.name }.forEach { group ->
                        val chip = Chip(this@SKPlayerListActivity).apply {
                            id = ViewCompat.generateViewId()
                            text = group.name
                            setChipBackgroundColorResource(R.color.colorPrimary)
                            isCloseIconVisible = false
                            setTextColor(getColor(R.color.white))
                        }
                        itemView.layoutPlayerGroupChipGroup.addView(chip)
                    }
                }
            }
        }
    }
}