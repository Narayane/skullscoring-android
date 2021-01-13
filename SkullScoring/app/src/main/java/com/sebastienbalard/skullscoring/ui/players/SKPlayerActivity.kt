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
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.chip.Chip
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.resetFocus
import com.sebastienbalard.skullscoring.ui.EventPlayer
import com.sebastienbalard.skullscoring.ui.SBActivity
import kotlinx.android.synthetic.main.activity_player.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class SKPlayerActivity : SBActivity(R.layout.activity_player) {

    internal val playerViewModel: SKPlayerViewModel by viewModel()

    private var playerId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")

        playerId = intent.extras?.getLong(EXTRA_PLAYER_ID)

        initToolbar(true)
        initUI()
        initObservers()
    }

    override fun onResume() {
        super.onResume()

        playerId?.let { playerId ->
            playerViewModel.getPlayer(playerId)
        }
    }

    private fun openCreateGroupBottomsheet() {
        MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(null, "Nouveau groupe")
            customView(R.layout.widget_bottomsheet_create_group)
            positiveButton(null, "Créer") { dialog ->
                val editTextGroupName: EditText =
                    dialog.getCustomView().findViewById(R.id.editTextGroupName)
                createChip(editTextGroupName.text.toString().trim())
            }
            negativeButton(null, "Annuler")
        }
    }

    private fun createChip(name: String) {
        val chip = Chip(this).apply {
            id = ViewCompat.generateViewId()
            text = name
            setChipBackgroundColorResource(R.color.colorPrimary)
            isCloseIconVisible = true
            setCloseIconTintResource(R.color.white)
            setTextColor(getColor(R.color.white))
            setOnCloseIconClickListener {
                layoutChipGroup.removeView(it)
            }
        }
        layoutChipGroup.addView(chip)
    }

    private fun initObservers() {
        playerViewModel.events.observe(this, Observer { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventPlayer -> {
                        editTextNewPlayer.setText(player.name)
                        player.groups.forEach { group ->
                            createChip(group.name)
                        }
                    }
                    else -> {
                    }
                }
            }
        })
    }

    private fun updatePlayer(playerId: Long) {
        val groupNames = layoutChipGroup.children.map { (it as Chip).text.toString() }
        playerViewModel.updatePlayer(
            playerId, editTextNewPlayer.text.toString().trim(), groupNames.toList()
        )
        finish()
    }

    private fun createPlayer() {
        val groupNames = layoutChipGroup.children.map { (it as Chip).text.toString() }
        playerViewModel.createPlayer(
            editTextNewPlayer.text.toString().trim(), groupNames.toList()
        )
        finish()
    }

    private fun initUI() {

        supportActionBar?.title = if (playerId != null) "Modifier un joueur" else "Nouveau joueur"

        buttonCreatePlayer.setOnClickListener {
            editTextNewPlayer.resetFocus()
            playerId?.let { playerId ->
                updatePlayer(playerId)
            } ?: run {
                createPlayer()
            }
        }

        buttonCreateGroup.setOnClickListener {
            openCreateGroupBottomsheet()
        }
    }

    companion object {

        const val EXTRA_PLAYER_ID = "EXTRA_PLAYER_ID"

        fun getIntent(context: Context): Intent {
            return Intent(
                context, SKPlayerActivity::class.java
            )
        }

        fun getIntentToEdit(context: Context, playerId: Long): Intent {
            return Intent(
                context, SKPlayerActivity::class.java
            ).putExtra(EXTRA_PLAYER_ID, playerId)
        }
    }
}