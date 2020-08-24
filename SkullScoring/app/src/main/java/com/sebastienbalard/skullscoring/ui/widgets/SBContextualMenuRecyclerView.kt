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

package com.sebastienbalard.skullscoring.ui.widgets

import android.content.Context
import android.util.SparseBooleanArray
import androidx.core.util.forEach

abstract class SBContextualMenuRecyclerView<T, VH : SBRecyclerViewAdapter.ViewHolder<T>>(
    context: Context, elements: List<T>
) : SBRecyclerViewAdapter<T, VH>(context, elements) {

    private val selectedElements = SparseBooleanArray()

    open fun toggleSelection(position: Int) {
        if (selectedElements.get(position, false)) {
            selectedElements.delete(position)
        } else {
            selectedElements.put(position, true)
        }
        notifyItemChanged(position)
    }

    open fun clearSelection() {
        selectedElements.clear()
        notifyDataSetChanged()
    }

    open fun getSelectedItemsCount() = selectedElements.size()

    open fun getSelectedItemsPositions(): List<Int> {
        val positions: MutableList<Int> = ArrayList(selectedElements.size())
        selectedElements.forEach { key, _ ->
            positions.add(selectedElements.keyAt(key))
        }
        return positions
    }
}