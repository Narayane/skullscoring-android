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
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.widget_recyclerview_section_pinned_header.view.*


abstract class SBSectionRecyclerViewAdapter<V, T, VH : SBSectionRecyclerViewAdapter.ViewHolder<T>>(
    private val context: Context, var sections: Map<V, List<T>>
) : RecyclerView.Adapter<VH>() {

    open fun getHeaderState(position: Int): Int {
        if (position < 0 || itemCount == 0) {
            return HEADER_GONE
        }
        val section = getSectionIndexForPosition(position)
        val nextSectionPosition = getPositionForSectionIndex(section + 1)
        return if (nextSectionPosition != -1 && position == nextSectionPosition - 1) {
            HEADER_PUSHED_UP
        } else HEADER_VISIBLE
    }

    abstract fun configureFloatingHeader(pHeader: View?, pPosition: Int)

    abstract fun getSectionLabel(key: V): String

    open fun getPositionForSectionIndex(index: Int): Int {
        var currentIndex = index
        if (currentIndex < 0) {
            currentIndex = 0
        }
        if (currentIndex >= sections.size) {
            currentIndex = sections.size - 1
        }
        var elementIndex = 0
        var sectionIndex = 0
        for ((_, value) in sections) {
            if (currentIndex == sectionIndex) {
                return elementIndex
            }
            elementIndex += value.size
            sectionIndex++
        }
        return 0
    }

    open fun getSectionIndexForPosition(position: Int): Int {
        var elementIndex = 0
        var sectionIndex = 0
        for ((_, value) in sections) {
            if (position >= elementIndex && position < elementIndex + value.size) {
                return sectionIndex
            }
            elementIndex += value.size
            sectionIndex++
        }
        return -1
    }

    open fun getSections(): Array<String> {
        val res = arrayOf<String>()
        var index = 0
        for ((key) in sections) {
            res[index] = getSectionLabel(key)
            index++
        }
        return res
    }

    open fun getItem(position: Int): T? {
        var index = 0
        for ((_, value) in sections) {
            if (position >= index && position < index + value.size) {
                return value[position - index]
            }
            index += value.size
        }
        return null
    }

    override fun getItemCount(): Int {
        var count = 0
        for ((_, value) in sections.entries) {
            count += value.size
        }
        return count
    }

    override fun onBindViewHolder(viewHolder: VH, position: Int) {
        val sectionIndex = getSectionIndexForPosition(position)
        val displaySectionHeaders = getPositionForSectionIndex(sectionIndex) == position
        if (displaySectionHeaders) {
            viewHolder.getTextViewPinnedHeaderLabel().visibility = View.VISIBLE
            viewHolder.getTextViewPinnedHeaderLabel().text =
                getSections()[getSectionIndexForPosition(position)]
        } else {
            viewHolder.getTextViewPinnedHeaderLabel().visibility = View.GONE
        }
    }

    abstract class ViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

        abstract fun bind(context: Context, element: T)

        open fun getTextViewPinnedHeaderLabel(): TextView {
            return itemView.widget_recyclerview_textview_section_pinned_header
        }
    }

    companion object {
        const val HEADER_GONE = 0
        const val HEADER_VISIBLE = 1
        const val HEADER_PUSHED_UP = 2
    }
}