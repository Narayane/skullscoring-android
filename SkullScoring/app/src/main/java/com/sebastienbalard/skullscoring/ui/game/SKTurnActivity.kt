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
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.resetFocus
import com.sebastienbalard.skullscoring.extensions.setFocus
import com.sebastienbalard.skullscoring.extensions.showSnackBarError
import com.sebastienbalard.skullscoring.models.SKTurnPlayerJoin
import com.sebastienbalard.skullscoring.ui.*
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_turn.*
import kotlinx.android.synthetic.main.item_turn_declaration.view.*
import kotlinx.android.synthetic.main.item_turn_result.view.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class SKTurnActivity : SBActivity(R.layout.activity_turn) {

    internal val turnViewModel: SKTurnViewModel by viewModel()

    private lateinit var turnDeclarationListAdapter: TurnDeclarationListAdapter
    private lateinit var turnResultListAdapter: TurnResultListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")

        initToolbar(true)
        initObservers()

        intent.extras?.getLong(EXTRA_GAME_ID)?.let { gameId ->
            intent.extras?.getBoolean(EXTRA_IS_GAME_RESULTS)?.let {
                if (!it) turnViewModel.loadTurnDeclarations(gameId) else turnViewModel.loadTurnResults(
                    gameId
                )
            } ?: finish()

        } ?: finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Timber.v("onCreateOptionsMenu")
        menuInflater.inflate(R.menu.menu_turn, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_turn_item_validate -> {
                if (turnViewModel.states.value is StateTurnDeclarations) {
                    turnViewModel.saveTurnDeclarations(turnDeclarationListAdapter.elements)
                } else {
                    intent.extras?.getLong(EXTRA_GAME_ID)?.let { gameId ->
                        turnViewModel.saveTurnResults(turnResultListAdapter.elements, gameId)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initObservers() {
        turnViewModel.states.observe(this, Observer { event ->
            event?.apply {
                Timber.v("state -> ${this::class.java.simpleName}")
                initUI()
                when (this) {
                    is StateTurnDeclarations -> {
                        Timber.d("turn number: ${turn.number}")
                        toolbar.title = "Annonces"
                        toolbar.subtitle = "Tour ${turn.number}"
                        turnDeclarationListAdapter.elements = turn.results
                        turnDeclarationListAdapter.notifyDataSetChanged()
                    }
                    is StateTurnResults -> {
                        Timber.d("turn number: ${turn.number}")
                        toolbar.title = "Résultats"
                        toolbar.subtitle = "Tour ${turn.number}"
                        turnResultListAdapter.elements = turn.results
                        turnResultListAdapter.notifyDataSetChanged()
                    }
                    else -> {
                    }
                }
            }
        })
        turnViewModel.events.observe(this, Observer { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventErrorWithArg -> toolbar.showSnackBarError(
                        getString(messageResId, arg)
                    )
                    is EventTurnDeclarationsUpdated -> finish()
                    is EventTurnResultsUpdated -> finish()
                    else -> {
                    }
                }
            }
        })
    }

    private fun initUI() {
        if (turnViewModel.states.value is StateTurnDeclarations) {
            turnDeclarationListAdapter = TurnDeclarationListAdapter(this, listOf())
            recyclerViewTurn.layoutManager = LinearLayoutManager(this)
            recyclerViewTurn.itemAnimator = DefaultItemAnimator()
            recyclerViewTurn.adapter = turnDeclarationListAdapter
        } else {
            turnResultListAdapter = TurnResultListAdapter(this, listOf())
            recyclerViewTurn.layoutManager = LinearLayoutManager(this)
            recyclerViewTurn.itemAnimator = DefaultItemAnimator()
            recyclerViewTurn.adapter = turnResultListAdapter
        }
    }

    companion object {
        const val EXTRA_GAME_ID = "EXTRA_GAME_ID"
        const val EXTRA_IS_GAME_RESULTS = "EXTRA_IS_GAME_RESULTS"

        fun getIntentForDeclarations(context: Context, gameId: Long): Intent {
            return Intent(
                context, SKTurnActivity::class.java
            ).putExtra(EXTRA_GAME_ID, gameId).putExtra(EXTRA_IS_GAME_RESULTS, false)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }

        fun getIntentForResults(context: Context, gameId: Long): Intent {
            return Intent(
                context, SKTurnActivity::class.java
            ).putExtra(EXTRA_GAME_ID, gameId).putExtra(EXTRA_IS_GAME_RESULTS, true)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
    }

    private class TurnResultListAdapter(context: Context, players: List<SKTurnPlayerJoin>) :
        SBRecyclerViewAdapter<SKTurnPlayerJoin, TurnResultListAdapter.ViewHolder>(
            context, players
        ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_turn_result, parent, false
                )
            )
        }

        class ViewHolder(itemView: View) :
            SBRecyclerViewAdapter.ViewHolder<SKTurnPlayerJoin>(itemView) {

            override fun bind(context: Context, element: SKTurnPlayerJoin) {
                itemView.textViewTurnResultPlayerName.text =
                    "${element.player.name} (annonce : ${element.declaration ?: 0})"
                itemView.buttonStepperTurnResult.number = element.result?.run {
                    "$this"
                } ?: run {
                    element.result = element.declaration
                    "${element.declaration}"
                }
                element.hasSkullKing?.let { hasSkullKing ->
                    itemView.checkboxTurnHasSkullKing.isChecked = hasSkullKing
                    if (hasSkullKing) {
                        itemView.editTextTurnPirateCount.apply {
                            isEnabled = hasSkullKing
                            element.pirateCount?.let {
                                append(it.toString())
                            }
                        }
                    }
                }
                element.hasMermaid?.apply {
                    itemView.checkboxTurnHasMarmaid.isChecked = this
                }
                itemView.buttonStepperTurnResult.setOnValueChangeListener { _, _, newValue ->
                    element.result = newValue
                }
                itemView.checkboxTurnHasSkullKing.setOnCheckedChangeListener { _, isChecked ->
                    element.hasSkullKing = isChecked
                    itemView.editTextTurnPirateCount.visibility =
                        if (isChecked) View.VISIBLE else View.GONE
                    itemView.editTextTurnPirateCount.apply {
                        post {
                            isEnabled = isChecked
                            if (isChecked) {
                                append("0")
                                setFocus(context)
                                setOnEditorActionListener { _, actionId, _ ->
                                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                                        element.pirateCount = text.toString().toInt()
                                        resetFocus()
                                    }
                                    true
                                }
                            } else {
                                setText("")
                                element.pirateCount = null
                                resetFocus()
                            }
                        }
                    }
                }
                /*itemView.editTextTurnPirateCount.setOnClickListener {
                    setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            element.pirateCount = text.toString().toInt()
                            resetFocus()
                        }
                        true
                    }
                }*/
                itemView.checkboxTurnHasMarmaid.setOnCheckedChangeListener { _, isChecked ->
                    element.hasMermaid = isChecked
                }
            }
        }
    }

    private class TurnDeclarationListAdapter(context: Context, players: List<SKTurnPlayerJoin>) :
        SBRecyclerViewAdapter<SKTurnPlayerJoin, TurnDeclarationListAdapter.ViewHolder>(
            context, players
        ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_turn_declaration, parent, false
                )
            )
        }

        class ViewHolder(itemView: View) :
            SBRecyclerViewAdapter.ViewHolder<SKTurnPlayerJoin>(itemView) {

            override fun bind(context: Context, element: SKTurnPlayerJoin) {
                itemView.textViewTurnDeclarationPlayerName.text = element.player.name
                itemView.buttonStepperTurnDeclaration.number = element.declaration?.run {
                    this.toString()
                } ?: run {
                    element.declaration = 0
                    "0"
                }
                itemView.buttonStepperTurnDeclaration.setOnValueChangeListener { _, _, newValue ->
                    element.declaration = newValue
                }
            }
        }
    }
}
