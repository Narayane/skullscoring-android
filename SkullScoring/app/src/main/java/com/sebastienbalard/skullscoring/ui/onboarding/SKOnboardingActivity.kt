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

package com.sebastienbalard.skullscoring.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
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
import com.sebastienbalard.skullscoring.ui.game.SKGameActivity
import com.sebastienbalard.skullscoring.ui.game.SKPlayerSearchActivity
import com.sebastienbalard.skullscoring.ui.game.SKPlayerSortingActivity
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewAdapter
import com.sebastienbalard.skullscoring.ui.widgets.SBVerticalSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_onboarding.*
import kotlinx.android.synthetic.main.item_onboarding_player.view.textViewPlayerName
import kotlinx.android.synthetic.main.item_sort_player.view.*
import kotlinx.android.synthetic.main.scene_onboarding_start.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*


open class SKOnboardingActivity : SBActivity(R.layout.activity_onboarding) {

    internal val onboardingViewModel: SKOnboardingViewModel by viewModel()

    private lateinit var sceneNewPlayer: Scene
    private lateinit var scenePlayerList: Scene
    private lateinit var playerListAdapter: PlayerListAdapter
    private var buttonAddPlayer: Button? = null
    private var hasAtLeastOneGame = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        toolbar.title = getString(R.string.first_game)

        initUI()
        initObservers()

        onboardingViewModel.loadOnboarding()
    }

    private fun initObservers() {
        onboardingViewModel.events.observe(this, { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventGameAtLeastOne -> {
                        this@SKOnboardingActivity.hasAtLeastOneGame = hasAtLeastOneGame
                    }
                    is EventPlayerCreated -> {
                        TransitionManager.go(scenePlayerList)
                    }
                    is EventGameCreated -> {
                        startActivity(
                            SKGameActivity.getIntent(
                                this@SKOnboardingActivity, gameId
                            )
                        )
                    }
                    is EventError -> toolbar.showSnackBarError(
                        getString(messageResId)
                    )
                    else -> {
                    }
                }
            }
        })

        onboardingViewModel.players.observe(this, { players ->
            playerListAdapter.setAllItems(players)
            buttonAddPlayer?.visibility = if (players.size < 6) VISIBLE else GONE
        })
    }

    private fun initSceneNewPlayer() {
        sceneNewPlayer =
            Scene.getSceneForLayout(layoutOnboarding, R.layout.scene_onboarding_new_user, this)

        sceneNewPlayer.setEnterAction {
            val editTextNewPlayer =
                sceneNewPlayer.sceneRoot.findViewById<EditText>(R.id.editTextOnboardingNewPlayer)
            editTextNewPlayer.setFocus(this)
            editTextNewPlayer.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    editTextNewPlayer.resetFocus()
                    onboardingViewModel.createPlayer(editTextNewPlayer.text.toString().trim())
                }
                true
            }

            val buttonCreatePlayer =
                sceneNewPlayer.sceneRoot.findViewById<Button>(R.id.buttonOnboardingCreatePlayer)
            buttonCreatePlayer.setOnClickListener {
                editTextNewPlayer.resetFocus()
                onboardingViewModel.createPlayer(editTextNewPlayer.text.toString().trim())
            }
        }
    }

    private fun initScenePlayerList() {
        playerListAdapter = PlayerListAdapter(
            this, listOf()
        )
        scenePlayerList = Scene.getSceneForLayout(
            layoutOnboarding, R.layout.scene_onboarding_player_list, this
        )
        scenePlayerList.setEnterAction {
            val recyclerViewPlayers =
                scenePlayerList.sceneRoot.findViewById<RecyclerView>(R.id.recyclerViewOnboarding)
            recyclerViewPlayers.layoutManager = LinearLayoutManager(this)
            recyclerViewPlayers.itemAnimator = DefaultItemAnimator()
            recyclerViewPlayers.addItemDecoration(SBVerticalSpacingItemDecoration(32))
            recyclerViewPlayers.adapter = playerListAdapter

            buttonAddPlayer =
                scenePlayerList.sceneRoot.findViewById(R.id.buttonOnboardingAddPlayer)
            buttonAddPlayer?.setOnClickListener {
                if (hasAtLeastOneGame) {
                    startActivity(
                        SKPlayerSearchActivity.getIntent(
                            this@SKOnboardingActivity
                        )
                    )
                } else {
                    TransitionManager.go(sceneNewPlayer)
                }
            }

            val buttonPlayGame =
                scenePlayerList.sceneRoot.findViewById<Button>(R.id.buttonOnboardingPlayGame)
            buttonPlayGame.setOnClickListener {
                val playerIds = playerListAdapter.getElements().map { it.id }
                startActivity(SKPlayerSortingActivity.getIntentForOnboarding(this, playerIds))
            }
        }
    }

    private fun initUI() {
        initSceneNewPlayer()
        initScenePlayerList()

        buttonOnboardingStart.setOnClickListener {
            if (hasAtLeastOneGame) {
                startActivity(
                    SKPlayerSearchActivity.getIntent(
                        this@SKOnboardingActivity
                    )
                )
            } else {
                TransitionManager.go(sceneNewPlayer)
            }
        }
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(
                context, SKOnboardingActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
    }

    private class PlayerListAdapter(context: Context, players: List<SKPlayer>) :
        SBRecyclerViewAdapter<SKPlayer, PlayerListAdapter.ViewHolder>(context, players) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_onboarding_player, parent, false
                )
            )
        }

        class ViewHolder(itemView: View) : SBRecyclerViewAdapter.ViewHolder<SKPlayer>(itemView) {

            override fun bind(context: Context, item: SKPlayer) {
                itemView.textViewPlayerName.text = item.name
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
