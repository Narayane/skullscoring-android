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
import android.text.Editable
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.florent37.expansionpanel.viewgroup.ExpansionLayoutCollection
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.*
import com.sebastienbalard.skullscoring.models.SKTurnPlayerJoin
import com.sebastienbalard.skullscoring.ui.*
import com.sebastienbalard.skullscoring.ui.widgets.SBEditTextMinMaxTextWatcher
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
                if (!it) turnViewModel.loadCurrentTurnDeclarations(gameId) else turnViewModel.loadCurrentTurnResults(
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
                    turnViewModel.saveTurnDeclarations(turnDeclarationListAdapter.getElements())
                } else {
                    intent.extras?.getLong(EXTRA_GAME_ID)?.let { gameId ->
                        turnViewModel.saveTurnResults(turnResultListAdapter.getElements(), gameId)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initObservers() {
        turnViewModel.states.observe(this, { event ->
            event?.apply {
                Timber.v("state -> ${this::class.java.simpleName}")
                initUI()
                when (this) {
                    is StateTurnDeclarations -> {
                        Timber.d("turn number: ${turn.number}")
                        toolbar.title = getString(R.string.declarations)
                        toolbar.subtitle = getString(R.string.turn, turn.number)
                        turnDeclarationListAdapter.setAllItems(turn.results)
                    }
                    is StateTurnResults -> {
                        Timber.d("turn number: ${turn.number}")
                        toolbar.title = getString(R.string.results)
                        toolbar.subtitle = getString(R.string.turn, turn.number)
                        turnResultListAdapter.setAllItems(turn.results)
                    }
                    else -> {
                    }
                }
            }
        })
        turnViewModel.events.observe(this, { event ->
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

        private val expansionsCollection = ExpansionLayoutCollection()

        init {
            expansionsCollection.openOnlyOne(true)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_turn_result, parent, false
                )
            )
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            super.onBindViewHolder(viewHolder, position)
            expansionsCollection.add(viewHolder.itemView.expansionLayout)
            viewHolder.itemView.editTextTurnPirateCount.apply {
                addTextChangedListener(object : SBEditTextMinMaxTextWatcher(1, 5) {

                    override fun beforeTextChanged(
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        s?.let {
                            var value: Int
                            val players = this@TurnResultListAdapter.getElements()
                            post {
                                try {
                                    value = it.toString().toInt()
                                    if (value < min) {
                                        setText("")
                                        append(min.toString())
                                    } else if (value > max) {
                                        setText("")
                                        append(max.toString())
                                    }
                                    if (players.isNotEmpty()) {
                                        players[position].pirateCount = text.toString().toInt()
                                    }
                                } catch (nfe: NumberFormatException) {
                                    if (s.toString() != "") {
                                        setText("")
                                    }
                                    if (players.isNotEmpty()) {
                                        players[position].pirateCount = null
                                    }
                                }
                            }
                        }
                    }
                })
            }
        }

        fun removeBonusExcept(item: SKTurnPlayerJoin) {
            getElements().apply {
                val protectedIndex = indexOf(item)
                withIndex().filter { it.value.hasBonus && it.index != protectedIndex }
                    .mapIndexed { index, item ->
                        item.value.hasSkullKing = false
                        item.value.pirateCount = null
                        item.value.hasMermaid = false
                        notifyItemChanged(index)
                    }
            }
        }

        inner class ViewHolder(itemView: View) :
            SBRecyclerViewAdapter.ViewHolder<SKTurnPlayerJoin>(itemView) {

            override fun bind(context: Context, item: SKTurnPlayerJoin) {
                itemView.textViewTurnResultPlayerName.text = context.getString(
                    R.string.item_turn_result, item.player.name, item.declaration ?: 0
                )
                setResult(item)
                setBonus(item)
                itemView.buttonStepperTurnResult.setOnValueChangeListener { _, _, newValue ->
                    item.result = newValue
                    itemView.headerIndicator.apply {
                        post {
                            hide(item.result == 0)
                        }
                    }
                    itemView.expansionLayout.apply {
                        post {
                            if (item.result == 0) {
                                collapse(false)
                            }
                        }
                    }
                }
                itemView.checkboxTurnHasSkullKing.setOnCheckedChangeListener { _, isChecked ->
                    item.hasSkullKing = isChecked
                    refreshTurnPirateCount(isChecked, context, item)
                    if (isChecked) {
                        itemView.checkboxTurnHasMarmaid check false
                        this@TurnResultListAdapter.removeBonusExcept(item)
                    }
                }
                itemView.checkboxTurnHasMarmaid.setOnCheckedChangeListener { _, isChecked ->
                    item.hasMermaid = isChecked
                    if (isChecked) {
                        itemView.checkboxTurnHasSkullKing check false
                        refreshTurnPirateCount(false, context, item)
                        this@TurnResultListAdapter.removeBonusExcept(item)
                    }
                }
            }

            private fun refreshTurnPirateCount(
                isChecked: Boolean, context: Context, item: SKTurnPlayerJoin
            ) {
                itemView.editTextTurnPirateCount show isChecked
                itemView.editTextTurnPirateCount.apply {
                    post {
                        isEnabled = isChecked
                        if (isChecked) {
                            append("")
                            setFocus(context)
                            setOnEditorActionListener { _, actionId, _ ->
                                if (actionId == EditorInfo.IME_ACTION_DONE) {
                                    item.pirateCount = text.toString().toInt()
                                    resetFocus()
                                }
                                true
                            }
                        } else {
                            setText("")
                            item.pirateCount = null
                            resetFocus()
                        }
                    }
                }
            }

            private fun expandIfBonus(item: SKTurnPlayerJoin) {
                itemView.expansionLayout.apply {
                    post {
                        if (item.hasBonus) {
                            expand(false)
                        }
                    }
                }
            }

            private fun setBonus(item: SKTurnPlayerJoin) {
                item.hasSkullKing?.let { hasSkullKing ->
                    itemView.checkboxTurnHasSkullKing check hasSkullKing
                    if (hasSkullKing) {
                        itemView.editTextTurnPirateCount.apply {
                            isEnabled = hasSkullKing
                            item.pirateCount?.let {
                                append(it.toString())
                            }
                        }
                    }
                }
                item.hasMermaid?.apply {
                    itemView.checkboxTurnHasMarmaid check this
                }
                expandIfBonus(item)
            }

            private fun setResult(item: SKTurnPlayerJoin) {
                itemView.buttonStepperTurnResult.number = item.result?.run {
                    "$this"
                } ?: run {
                    item.result = item.declaration
                    "${item.declaration}"
                }
                itemView.headerIndicator.apply {
                    post {
                        hide(item.result == 0)
                    }
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

            override fun bind(context: Context, item: SKTurnPlayerJoin) {
                itemView.textViewTurnDeclarationPlayerName.text = item.player.name
                setDeclaration(item)
                itemView.buttonStepperTurnDeclaration.setOnValueChangeListener { _, _, newValue ->
                    item.declaration = newValue
                }
            }

            private fun setDeclaration(item: SKTurnPlayerJoin) {
                itemView.buttonStepperTurnDeclaration.number = item.declaration?.run {
                    this.toString()
                } ?: run {
                    item.declaration = 0
                    "0"
                }
            }
        }
    }
}
