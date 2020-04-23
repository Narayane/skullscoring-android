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
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class SBRecyclerViewAdapter<T, VH : SBRecyclerViewAdapter.ViewHolder<T>>(
    private val context: Context, var elements: List<T>
) : RecyclerView.Adapter<VH>() {

    abstract override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): VH

    override fun onBindViewHolder(
        viewHolder: VH, position: Int
    ) {
        viewHolder.bind(context, elements[position])
    }

    override fun getItemCount() = elements.size

    abstract class ViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

        abstract fun bind(context: Context, element: T)
    }
}