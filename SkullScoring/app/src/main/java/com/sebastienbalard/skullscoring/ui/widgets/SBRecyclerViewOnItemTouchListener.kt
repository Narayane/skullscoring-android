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
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener

open class SBRecyclerViewOnItemTouchListener(
    context: Context, recyclerView: RecyclerView, val itemTouchListener: OnItemTouchListener
) : OnItemTouchListener {

    interface OnItemTouchListener {
        fun onClick(viewHolder: RecyclerView.ViewHolder, position: Int)
        fun onLongClick(viewHolder: RecyclerView.ViewHolder, position: Int) {
            // optional
        }
        fun isEnabled(position: Int): Boolean
    }

    private var gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
                recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)?.let {
                    itemTouchListener.onClick(
                        recyclerView.getChildViewHolder(it),
                        recyclerView.getChildAdapterPosition(it)
                    )
                }
                return true
            }

            override fun onLongPress(motionEvent: MotionEvent) {
                recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)?.let {
                    itemTouchListener.onLongClick(
                        recyclerView.getChildViewHolder(it),
                        recyclerView.getChildAdapterPosition(it)
                    )
                }
            }
        })
    }

    override fun onInterceptTouchEvent(
        recyclerView: RecyclerView, motionEvent: MotionEvent
    ): Boolean {
        return recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)?.let {
            return if (!itemTouchListener.isEnabled(recyclerView.getChildAdapterPosition(it))) {
                onRequestDisallowInterceptTouchEvent(true)
                true
            } else {
                gestureDetector.onTouchEvent(motionEvent)
                false
            }
        } ?: false
    }

    override fun onTouchEvent(
        recyclerView: RecyclerView, motionEvent: MotionEvent
    ) {
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}