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
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.resetFocus
import com.sebastienbalard.skullscoring.models.SKGroup
import com.sebastienbalard.skullscoring.ui.EventGroupList
import com.sebastienbalard.skullscoring.ui.EventPlayer
import com.sebastienbalard.skullscoring.ui.SBActivity
import kotlinx.android.synthetic.main.activity_game_history.*
import kotlinx.android.synthetic.main.activity_player.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*


class SKPlayerActivity : SBActivity(R.layout.activity_player) {

    internal val playerViewModel: SKPlayerViewModel by viewModel()

    private var playerId: Long? = null
    private var displayedGroupStream =
        MutableLiveData<List<SKGroup>>().apply { postValue(listOf()) }

    private lateinit var searchViewDropDownListAdapter: SBGroupFilterableArrayAdapter

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

        playerViewModel.getGroups()
        playerId?.let { playerId ->
            playerViewModel.getPlayer(playerId)
        }
    }

    private fun openCreateGroupBottomsheet() {
        MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(null, getString(R.string.new_group))
            customView(R.layout.widget_bottomsheet_create_group)
            positiveButton(null, getString(R.string.create)) { dialog ->
                val editTextGroupName: EditText =
                    dialog.getCustomView().findViewById(R.id.editTextGroupName)
                displayedGroupStream.value?.let { displayedGroups ->
                    val existingGroups = mutableListOf<SKGroup>().apply {
                        addAll(displayedGroups)
                    }
                    val newGroupName = editTextGroupName.text.toString().trim()
                    if (!existingGroups.map { it.name }.contains(newGroupName)) {
                        existingGroups.add(SKGroup(newGroupName))
                        displayedGroupStream.postValue(existingGroups)
                    }
                }
            }
            negativeButton(null, getString(R.string.cancel))
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
                val index = layoutChipGroup.indexOfChild(it)
                displayedGroupStream.value?.let { displayedGroups ->
                    val existingGroups = mutableListOf<SKGroup>().apply {
                        addAll(displayedGroups)
                    }
                    existingGroups.removeAt(index)
                    displayedGroupStream.postValue(existingGroups)
                }
            }
        }
        layoutChipGroup.addView(chip)
    }

    private fun initObservers() {
        playerViewModel.events.observe(this, { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventPlayer -> {
                        editTextNewPlayer.setText(player.name)
                        if (player.groups.isNotEmpty()) {
                            displayedGroupStream.postValue(player.groups)
                        }
                    }
                    is EventGroupList -> {
                        searchViewDropDownListAdapter = SBGroupFilterableArrayAdapter(
                            this@SKPlayerActivity, groups
                        )
                        searchViewGroup.setAdapter(searchViewDropDownListAdapter)
                    }
                    else -> {
                    }
                }
            }
        })
        displayedGroupStream.observe(this, { list ->
            layoutChipGroup.removeAllViews()
            list.sortedBy { it.name }.forEach { group ->
                createChip(group.name)
            }
        })
    }

    private fun updatePlayer(playerId: Long) {
        displayedGroupStream.value?.let {
            playerViewModel.updatePlayer(
                playerId, editTextNewPlayer.text.toString().trim(), it
            )
        }
        finish()
    }

    private fun createPlayer() {
        displayedGroupStream.value?.let {
            playerViewModel.createPlayer(
                editTextNewPlayer.text.toString().trim(), it
            )
        }
        finish()
    }

    private fun initUI() {

        supportActionBar?.title = if (playerId != null) "Modifier un joueur" else "Nouveau joueur"

        layoutSearchViewGroup.endIconMode = END_ICON_NONE
        searchViewGroup.gravity = Gravity.TOP
        searchViewGroup.threshold = 2
        searchViewGroup.setOnItemClickListener { _, _, position, _ ->
            searchViewGroup.resetFocus()
            searchViewGroup.clearFocus()
            searchViewGroup.setText("")
            displayedGroupStream.value?.let { displayedGroups ->
                val existingGroups = mutableListOf<SKGroup>().apply {
                    addAll(displayedGroups)
                }
                val selectedGroup = searchViewDropDownListAdapter.filteredItems[position]
                if (!existingGroups.contains(selectedGroup)) {
                    existingGroups.add(selectedGroup)
                    displayedGroupStream.postValue(existingGroups)
                }
            }
        }
        searchViewGroup.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchViewGroup.resetFocus()
                    searchViewGroup.clearFocus()
                    searchViewGroup.setText("")
                    return true
                }
                return false
            }
        })

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

    class SBGroupFilterableArrayAdapter(context: Context, val items: List<SKGroup>) :
        ArrayAdapter<SKGroup>(context, android.R.layout.simple_spinner_dropdown_item, items),
        Filterable {

        var filteredItems = items

        override fun getCount(): Int {
            return filteredItems.count()
        }

        override fun getItem(position: Int): SKGroup {
            return filteredItems[position]
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: TextView = convertView as TextView? ?: LayoutInflater.from(context)
                .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false) as TextView
            view.text = filteredItems[position].name
            return view
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun publishResults(
                    charSequence: CharSequence?, filterResults: FilterResults
                ) {
                    filteredItems = filterResults.values as List<SKGroup>
                    notifyDataSetChanged()
                }

                override fun performFiltering(charSequence: CharSequence?): FilterResults {
                    val queryString = charSequence?.toString()?.toLowerCase(Locale.getDefault())

                    val filterResults = FilterResults()
                    filterResults.values =
                        if (queryString == null || queryString.isEmpty()) this@SBGroupFilterableArrayAdapter.items
                        else this@SBGroupFilterableArrayAdapter.items.filter {
                            it.name.toLowerCase(Locale.getDefault()).contains(queryString)
                        }
                    return filterResults
                }
            }
        }
    }
}