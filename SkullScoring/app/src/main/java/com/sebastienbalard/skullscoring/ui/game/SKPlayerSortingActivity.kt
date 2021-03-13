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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.showSnackBarError
import com.sebastienbalard.skullscoring.models.SKPlayer
import com.sebastienbalard.skullscoring.ui.*
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewAdapter
import com.sebastienbalard.skullscoring.ui.widgets.SBVerticalSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.activity_player_search.*
import kotlinx.android.synthetic.main.activity_player_sorting.*
import kotlinx.android.synthetic.main.item_player_search.*
import kotlinx.android.synthetic.main.item_player_search.view.*
import kotlinx.android.synthetic.main.item_sort_player.view.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*

class SKPlayerSortingActivity : SBActivity(R.layout.activity_player_sorting) {

    internal val playerSearchViewModel: SKPlayerSearchViewModel by viewModel()

    private lateinit var sortPlayerListAdapter: SortPlayerListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        intent.extras?.getLongArray(EXTRA_IS_ONBOARDING)?.let {
            toolbar.title = getString(R.string.first_game)
        } ?: run {
            toolbar.title = getString(R.string.new_game)
        }
        toolbar.subtitle = getString(R.string.sort_players)

        initToolbar(true)
        initUI()
        initObservers()

        intent.extras?.getLongArray(EXTRA_PLAYERS_IDS)?.let {
            playerSearchViewModel.loadPlayers(it.toList())
        }
    }

    private fun initObservers() {
        playerSearchViewModel.events.observe(this, { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventPlayerList -> {
                        Timber.d("players count: ${players.size}")
                        sortPlayerListAdapter.setAllItems(players)
                    }
                    is EventGameCreated -> {
                        startActivity(
                            SKGameActivity.getIntent(
                                this@SKPlayerSortingActivity, gameId
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

    private fun initUI() {
        recyclerViewSortPlayer.layoutManager = LinearLayoutManager(this)
        recyclerViewSortPlayer.itemAnimator = DefaultItemAnimator()
        recyclerViewSortPlayer.addItemDecoration(SBVerticalSpacingItemDecoration(32))
        sortPlayerListAdapter = SortPlayerListAdapter(this, listOf())
        val callback: ItemTouchHelper.Callback = PlayerMoveCallback(sortPlayerListAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerViewSortPlayer)
        recyclerViewSortPlayer.adapter = sortPlayerListAdapter

        buttonOnboardingValidateOrder.setOnClickListener {
            playerSearchViewModel.createGame(sortPlayerListAdapter.getElements())
        }
    }

    companion object {

        private const val EXTRA_PLAYERS_IDS = "EXTRA_PLAYERS_IDS"
        private const val EXTRA_IS_ONBOARDING = "EXTRA_IS_ONBOARDING"

        fun getIntent(context: Context, playerIds: List<Long>): Intent {
            return Intent(
                context, SKPlayerSortingActivity::class.java
            ).putExtra(EXTRA_PLAYERS_IDS, playerIds.toLongArray())
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }

        fun getIntentForOnboarding(context: Context, playerIds: List<Long>): Intent {
            return Intent(
                context, SKPlayerSortingActivity::class.java
            ).putExtra(EXTRA_PLAYERS_IDS, playerIds.toLongArray())
                .putExtra(EXTRA_IS_ONBOARDING, true).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
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