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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.formatDateTime
import com.sebastienbalard.skullscoring.models.SKGame
import com.sebastienbalard.skullscoring.ui.SBActivity
import com.sebastienbalard.skullscoring.ui.game.EventGame
import com.sebastienbalard.skullscoring.ui.game.SKGameActivity
import com.sebastienbalard.skullscoring.ui.game.SKPlayerSearchActivity
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewAdapter
import com.sebastienbalard.skullscoring.ui.widgets.SBRecyclerViewOnItemTouchListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.item_game.view.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SKHomeActivity : SBActivity(R.layout.activity_home) {

    internal val homeViewModel: SKHomeViewModel by viewModel()

    private lateinit var gameListAdapter: GameListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        toolbar.title = getString(R.string.app_name)

        initUI()
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        Timber.v("onResume")

        homeViewModel.loadGames()
    }

    private fun initObservers() {
        homeViewModel.events.observe(this, Observer { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventGame -> {
                        Timber.d("open game of ${game.startDate.formatDateTime(this@SKHomeActivity)}")
                        startActivity(SKGameActivity.getIntent(this@SKHomeActivity, game.id))
                    }
                    is EventGameList -> {
                        Timber.d("games count: ${games.size}")
                        gameListAdapter.elements = games
                        gameListAdapter.notifyDataSetChanged()
                    }
                    else -> {
                    }
                }
            }
        })
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
                        homeViewModel.loadGame(gameListAdapter.elements[position])
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

    private class GameListAdapter(context: Context, games: List<SKGame>) :
        SBRecyclerViewAdapter<SKGame, GameListAdapter.ViewHolder>(context, games) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_game, parent, false
                )
            )
        }

        class ViewHolder(itemView: View) : SBRecyclerViewAdapter.ViewHolder<SKGame>(itemView) {

            override fun bind(context: Context, element: SKGame) {
                itemView.textViewGameDate.text = element.startDate.formatDateTime(context)
            }
        }

    }
}
