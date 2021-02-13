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

package com.sebastienbalard.skullscoring.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.formatDateTime
import com.sebastienbalard.skullscoring.extensions.showSnackBarError
import com.sebastienbalard.skullscoring.models.SKGame
import com.sebastienbalard.skullscoring.ui.EventErrorPluralWithArg
import com.sebastienbalard.skullscoring.ui.EventGameCreated
import com.sebastienbalard.skullscoring.ui.EventGameList
import com.sebastienbalard.skullscoring.ui.SBBottomNavigationViewActivity
import com.sebastienbalard.skullscoring.ui.game.SKGameActivity
import com.sebastienbalard.skullscoring.ui.game.SKPlayerSearchActivity
import com.sebastienbalard.skullscoring.ui.game.SKPlayerSortingActivity
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewAdapter
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewMultipleSelectionAdapter
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewOnItemTouchListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.item_game.view.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SKHomeActivity : SBBottomNavigationViewActivity(R.layout.activity_home) {

    internal val homeViewModel: SKHomeViewModel by viewModel()

    private lateinit var gameListAdapter: GameListAdapter
    private var actionMode: ActionMode? = null
    private var menuItemCopy: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")

        initToolbar(false)
        initUI()
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        Timber.v("onResume")

        homeViewModel.loadGames()
    }

    override fun getBottomNavigationMenuItemId(): Int {
        return R.id.menu_bottom_navigation_item_games
    }

    private fun initObservers() {
        homeViewModel.events.observe(this, Observer { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventGameList -> {
                        Timber.d("games count: ${games.size}")
                        gameListAdapter.setAllItems(games)
                    }
                    is EventErrorPluralWithArg -> toolbar.showSnackBarError(
                        resources.getQuantityString(
                            pluralMessageResId, arg as Int, arg
                        )
                    )
                    is EventGameCreated -> {
                        startActivity(
                            SKGameActivity.getIntent(
                                this@SKHomeActivity, gameId
                            )
                        )
                    }
                    else -> {
                    }
                }
            }
        })
    }

    private fun performSelection(position: Int) {
        gameListAdapter.toggleSelection(position)
        if (gameListAdapter.getSelectedItemsCount() == 0) {
            actionMode?.finish()
        } else {
            actionMode?.title = resources.getQuantityString(
                R.plurals.plural_game_selected,
                gameListAdapter.getSelectedItemsCount(),
                gameListAdapter.getSelectedItemsCount()
            )
        }
        menuItemCopy?.isVisible = gameListAdapter.getSelectedItemsCount() < 2
    }

    private fun initUI() {
        gameListAdapter = GameListAdapter(this, listOf())
        recycleViewHome.layoutManager = LinearLayoutManager(this)
        recycleViewHome.itemAnimator = DefaultItemAnimator()
        recycleViewHome.adapter = gameListAdapter
        recycleViewHome.addOnItemTouchListener(
            SBRecyclerViewOnItemTouchListener(this,
                recycleViewHome,
                object : SBRecyclerViewOnItemTouchListener.OnItemTouchListener {

                    override fun onClick(viewHolder: RecyclerView.ViewHolder, position: Int) {
                        Timber.v("onClick")
                        actionMode?.let {
                            performSelection(position)
                        } ?: run {
                            val clickedGame = gameListAdapter.getElements()[position]
                            Timber.i("open game of ${clickedGame.startDate.formatDateTime(this@SKHomeActivity)}")
                            startActivity(
                                SKGameActivity.getIntent(
                                    this@SKHomeActivity, clickedGame.id
                                )
                            )
                        }
                    }

                    override fun onLongClick(viewHolder: RecyclerView.ViewHolder, position: Int) {
                        Timber.v("onLongClick")
                        if (actionMode == null) {
                            startActionMode(actionModeCallback)
                        }
                        performSelection(position)
                    }

                    override fun isEnabled(position: Int): Boolean {
                        return true
                    }
                })
        )

        fabHome.setOnClickListener {
            startActivity(
                SKPlayerSearchActivity.getIntent(this)
            )
        }
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(
                context, SKHomeActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {

        override fun onCreateActionMode(
            mode: ActionMode?, menu: Menu?
        ): Boolean {
            mode?.menuInflater?.inflate(R.menu.menu_home_contextual, menu)
            menuItemCopy = menu?.findItem(R.id.menu_home_contextual_copy)
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
                R.id.menu_home_contextual_copy -> {
                    MaterialAlertDialogBuilder(this@SKHomeActivity).setTitle("Alerte")
                        .setMessage("Souhaitez-vous créer une nouvelle partie avec les mêmes joueurs que celle sélectionnée ?")
                        .setPositiveButton("Continuer") { dialog, _ ->
                            val playerIds = gameListAdapter.getSelectedItems().first().players.map { it.id }
                            startActivity(SKPlayerSortingActivity.getIntent(this@SKHomeActivity, playerIds))
                            dialog.dismiss()
                            actionMode?.finish()
                        }.setNegativeButton(
                            "Annuler"
                        ) { dialog, _ ->
                            dialog.dismiss()
                        }.show()
                }
                R.id.menu_home_contextual_delete -> {
                    homeViewModel.deleteGame(
                        *(gameListAdapter.getSelectedItems().toTypedArray())
                    )
                    actionMode?.finish()
                }
                else -> {
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            gameListAdapter.clearSelection()
            menuItemCopy = null
            actionMode = null
        }
    }

    private class GameListAdapter(context: Context, games: List<SKGame>) :
        SBRecyclerViewMultipleSelectionAdapter<SKGame, GameListAdapter.ViewHolder>(context, games) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_game, parent, false
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

        class ViewHolder(itemView: View) : SBRecyclerViewAdapter.ViewHolder<SKGame>(itemView) {

            override fun bind(context: Context, item: SKGame) {
                itemView.textViewGameDate.text = item.startDate.formatDateTime(context)
                itemView.textViewGameState.text =
                    if (item.isEnded) "Terminé" else "Tour ${item.currentTurnNumber}"
            }
        }
    }
}
