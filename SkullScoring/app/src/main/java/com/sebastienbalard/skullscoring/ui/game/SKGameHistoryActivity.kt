/**
 * Copyright © 2021 Skull Scoring (Sébastien BALARD)
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
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.formatDateTime
import com.sebastienbalard.skullscoring.models.SKGame
import com.sebastienbalard.skullscoring.models.SKTurnPlayerJoin
import com.sebastienbalard.skullscoring.ui.EventGame
import com.sebastienbalard.skullscoring.ui.EventTurn
import com.sebastienbalard.skullscoring.ui.SBActivity
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_game_history.*
import kotlinx.android.synthetic.main.item_turn_history.view.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import kotlin.math.abs

open class SKGameHistoryActivity : SBActivity(R.layout.activity_game_history) {

    internal val gameViewModel: SKGameViewModel by viewModel()
    internal val turnViewModel: SKTurnViewModel by viewModel()

    private var gameId: Long? = null
    private var selectedTurn: Int? = null
    private lateinit var turns: IntArray

    private lateinit var turnHistoryListAdapter: TurnHistoryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")

        initToolbar(true)
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

    private fun initObservers() {
        gameViewModel.events.observe(this) { event ->
            event.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventGame -> {
                        refreshToolbar(game)
                        selectedTurn = if (game.isEnded) {
                            game.currentTurnNumber
                        } else {
                            game.currentTurnNumber - 1
                        }
                        turns = (selectedTurn!! downTo 1).toList().toIntArray()
                        val adapter = ArrayAdapter(this@SKGameHistoryActivity,
                            android.R.layout.simple_spinner_dropdown_item,
                            arrayListOf<String>().apply {
                                addAll(turns.map {
                                    getString(
                                        R.string.turn, it
                                    )
                                })
                            })
                        spinnerTurn.setAdapter(adapter)
                        spinnerTurn.setText(spinnerTurn.adapter.getItem(0).toString(), false)
                        turnViewModel.loadTurnResults(game.id, game.currentTurnNumber - 1)
                    }
                    else -> {
                    }
                }
            }
        }
        turnViewModel.events.observe(this) { event ->
            event.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventTurn -> {
                        turnHistoryListAdapter.setAllItems(turn.results)
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun refreshToolbar(game: SKGame) {
        toolbar.title =
            getString(R.string.game, game.startDate.formatDateTime(this@SKGameHistoryActivity))
        toolbar.subtitle = getString(R.string.turn_history)
    }

    private fun initUI() {
        turnHistoryListAdapter = TurnHistoryListAdapter(this, listOf())
        recyclerViewTurnHistory.layoutManager = LinearLayoutManager(this)
        recyclerViewTurnHistory.itemAnimator = DefaultItemAnimator()
        recyclerViewTurnHistory.adapter = turnHistoryListAdapter

        spinnerTurn.setOnClickListener {
            spinnerTurn.showDropDown()
        }
        spinnerTurn.setOnItemClickListener { _, _, position, _ ->
            gameId?.let { gameId ->
                selectedTurn = turns[position]
                turnViewModel.loadTurnResults(gameId, selectedTurn!!)
            }
        }
    }

    companion object {
        const val EXTRA_GAME_ID = "EXTRA_GAME_ID"

        fun getIntent(context: Context, gameId: Long): Intent {
            return Intent(
                context, SKGameHistoryActivity::class.java
            ).putExtra(EXTRA_GAME_ID, gameId)
        }
    }

    private inner class TurnHistoryListAdapter(context: Context, players: List<SKTurnPlayerJoin>) :
        SBRecyclerViewAdapter<SKTurnPlayerJoin, TurnHistoryListAdapter.ViewHolder>(
            context, players
        ) {

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ): TurnHistoryListAdapter.ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_turn_history, parent, false
                )
            )
        }

        inner class ViewHolder(itemView: View) :
            SBRecyclerViewAdapter.ViewHolder<SKTurnPlayerJoin>(itemView) {

            override fun bind(context: Context, item: SKTurnPlayerJoin) {
                itemView.textViewTurnHistoryPlayerName.text = item.player.name
                itemView.textViewTurnHistoryDeclaration.text =
                    getString(R.string.declaration_value, item.declaration ?: 0)
                var resultLabel = getString(R.string.result_value, item.result ?: 0)
                var turnScore = 0
                this@SKGameHistoryActivity.selectedTurn?.let { turnNumber ->
                    item.declaration?.let { declaration ->
                        item.result?.let { result ->
                            if (declaration == result) {
                                turnScore += if (result == 0) turnNumber * 10 else result * 20
                                val hasSkullKing = item.hasSkullKing ?: false
                                val pirateCount = item.pirateCount ?: 0
                                if (hasSkullKing && pirateCount > 0) {
                                    resultLabel += " (SK+$pirateCount)"
                                }
                                turnScore += if (hasSkullKing) pirateCount * 30 else 0
                                val hasMermaid = item.hasMermaid ?: false
                                if (hasMermaid) {
                                    resultLabel += " (M)"
                                }
                                turnScore += if (hasMermaid) 50 else 0
                            } else {
                                turnScore += if (declaration == 0) turnNumber * -10 else abs(result - declaration) * -10
                            }
                        }
                    }
                }
                itemView.textViewTurnHistoryResult.text = resultLabel
                itemView.textViewTurnHistoryScore.text = turnScore.toString()
            }
        }
    }
}
