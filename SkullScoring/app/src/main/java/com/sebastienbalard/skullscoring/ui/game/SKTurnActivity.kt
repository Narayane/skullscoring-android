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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.showSnackBarError
import com.sebastienbalard.skullscoring.models.SKTurnPlayerJoin
import com.sebastienbalard.skullscoring.ui.EventErrorWithArg
import com.sebastienbalard.skullscoring.ui.EventTurn
import com.sebastienbalard.skullscoring.ui.EventTurnDeclarationsUpdated
import com.sebastienbalard.skullscoring.ui.SBActivity
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_turn.*
import kotlinx.android.synthetic.main.item_turn_player.view.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SKTurnActivity : SBActivity(R.layout.activity_turn) {

    internal val turnViewModel: SKTurnViewModel by viewModel()

    private lateinit var turnPlayerListAdapter: TurnPlayerListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")

        initToolbar()
        initUI()
        initObservers()

        intent.extras?.getLong(EXTRA_GAME_ID)?.let { gameId ->
            turnViewModel.loadCurrentTurn(gameId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Timber.v("onCreateOptionsMenu")
        menuInflater.inflate(R.menu.menu_turn, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_turn_item_validate -> {
                turnViewModel.saveDeclarations(turnPlayerListAdapter.elements)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initObservers() {
        turnViewModel.events.observe(this, Observer { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventTurn -> {
                        Timber.d("turn number: ${turn.number}")
                        toolbar.title = "Tour ${turn.number} - Annonces"
                        turnPlayerListAdapter.elements = turn.results
                        turnPlayerListAdapter.notifyDataSetChanged()
                    }
                    is EventErrorWithArg -> toolbar.showSnackBarError(
                        getString(messageResId, arg)
                    )
                    is EventTurnDeclarationsUpdated -> finish()
                    else -> {
                    }
                }
            }
        })
    }

    private fun initUI() {
        turnPlayerListAdapter = TurnPlayerListAdapter(this, listOf())
        recyclerViewTurn.layoutManager = LinearLayoutManager(this)
        recyclerViewTurn.itemAnimator = DefaultItemAnimator()
        recyclerViewTurn.adapter = turnPlayerListAdapter
    }

    companion object {const val EXTRA_GAME_ID = "EXTRA_GAME_ID"

        fun getIntent(context: Context, gameId: Long): Intent {
            return Intent(
                context, SKTurnActivity::class.java
            ).putExtra(SKGameActivity.EXTRA_GAME_ID, gameId).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
    }

    private class TurnPlayerListAdapter(context: Context, players: List<SKTurnPlayerJoin>) :
        SBRecyclerViewAdapter<SKTurnPlayerJoin, TurnPlayerListAdapter.ViewHolder>(
            context, players
        ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_turn_player, parent, false
                )
            )
        }

        class ViewHolder(itemView: View) :
            SBRecyclerViewAdapter.ViewHolder<SKTurnPlayerJoin>(itemView) {

            override fun bind(context: Context, element: SKTurnPlayerJoin) {
                itemView.textViewTurnPlayerName.text = element.player.name
                element.declaration?.apply {
                    itemView.buttonStepperDeclaration.number = this.toString()
                }
                itemView.buttonStepperDeclaration.setOnValueChangeListener { _, _, newValue ->
                    element.declaration = newValue
                }
            }
        }
    }
}
