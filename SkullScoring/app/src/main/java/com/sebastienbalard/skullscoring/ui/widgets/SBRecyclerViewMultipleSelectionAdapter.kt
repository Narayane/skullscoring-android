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

abstract class SBRecyclerViewMultipleSelectionAdapter<T, VH : SBRecyclerViewAdapter.ViewHolder<T>>(
    context: Context, items: List<T>
) : SBRecyclerViewAdapter<T, VH>(context, items) {

    protected val selectedPositions = SparseBooleanArray()

    open fun insertItem(item: T, position: Int = _items.count(), isSelected: Boolean = false) {
        _items.add(position, item)
        super.items = _items.toList()
        notifyDataSetChanged()
        if (isSelected) {
            toggleSelection(position)
        }
    }

    open fun toggleSelection(position: Int) {
        if (selectedPositions.get(position, false)) {
            selectedPositions.delete(position)
        } else {
            selectedPositions.put(position, true)
        }
        notifyItemChanged(position)
    }

    open fun clearSelection() {
        selectedPositions.clear()
        notifyDataSetChanged()
    }

    open fun getSelectedItemsCount() = selectedPositions.size()

    open fun getSelectedItems(): List<T> {
        return items.withIndex()
            .filter { getSelectedItemsPositions().contains(it.index) }
            .map { it.value }
    }

    open fun isItemSelected(position: Int) = getSelectedItemsPositions().contains(position)

    internal fun getSelectedItemsPositions(): List<Int> {
        val positions: MutableList<Int> = ArrayList(selectedPositions.size())
        selectedPositions.forEach { key, _ ->
            positions.add(key)
        }
        return positions
    }
}