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
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.ui.widgets.SBSectionRecyclerViewAdapter.Companion.HEADER_GONE
import com.sebastienbalard.skullscoring.ui.widgets.SBSectionRecyclerViewAdapter.Companion.HEADER_PUSHED_UP
import com.sebastienbalard.skullscoring.ui.widgets.SBSectionRecyclerViewAdapter.Companion.HEADER_VISIBLE


class SBSectionRecyclerView(context: Context, attrs: AttributeSet?, defStyle: Int) :
    RecyclerView(context, attrs, defStyle) {

    private lateinit var sectionAdapter: SBSectionRecyclerViewAdapter<*, *, *>

    private var headerView: View? = null
    private var isHeaderViewVisible = false
    private var headerViewWidth = 0
    private var headerViewHeight = 0

    constructor(pContext: Context) : this(pContext, null, 0)
    constructor(pContext: Context, attrs: AttributeSet) : this(pContext, attrs, 0)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        headerView?.apply {
            measureChild(this, widthMeasureSpec, heightMeasureSpec)
            headerViewWidth = measuredWidth
            headerViewHeight = measuredHeight
        }
    }

    override fun onLayout(
        changed: Boolean, left: Int, top: Int, right: Int, bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        headerView?.apply {
            setFadingEdgeLength(0)
            layout(0, 0, headerViewWidth, headerViewHeight)
            configureHeaderView((layoutManager as? LinearLayoutManager)!!.findFirstVisibleItemPosition())
        }
    }

    internal fun configureHeaderView(position: Int) {
        if (headerView == null) {
            headerView = LayoutInflater.from(context)
                .inflate(R.layout.widget_recyclerview_section_pinned_header, this, false)
        }
        when (sectionAdapter.getHeaderState(position)) {
            HEADER_GONE -> {
                isHeaderViewVisible = false
            }
            HEADER_VISIBLE -> {
                sectionAdapter.configureFloatingHeader(headerView, position)
                if (headerView?.top != 0) {
                    headerView?.layout(0, 0, headerViewWidth, headerViewHeight)
                }
                isHeaderViewVisible = true
            }
            HEADER_PUSHED_UP -> {
                getChildAt(0)?.apply {
                    headerView?.let { headerView ->
                        val y = if (bottom < headerView.height) {
                            bottom - headerView.height
                        } else {
                            0
                        }
                        sectionAdapter.configureFloatingHeader(headerView, position)
                        if (headerView.top != y) {
                            headerView.layout(0, y, headerViewWidth, headerView.height + y)
                        }
                        isHeaderViewVisible = true
                    }
                }
            }
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (isHeaderViewVisible) {
            drawChild(canvas, headerView, drawingTime)
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        if (adapter !is SBSectionRecyclerViewAdapter<*, *, *>) {
            throw IllegalArgumentException(
                "${SBSectionRecyclerView::class.java.simpleName} must use an adapter of type ${SBSectionRecyclerViewAdapter::class.java.simpleName}"
            )
        }
        clearOnScrollListeners()
        sectionAdapter = adapter
        addOnScrollListener(SBRecyclerViewOnScrollListener())
        super.setAdapter(adapter)
    }

    override fun getAdapter(): SBSectionRecyclerViewAdapter<*, *, *> {
        return sectionAdapter
    }

    class SBRecyclerViewOnScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (recyclerView is SBSectionRecyclerView) {
                recyclerView.configureHeaderView(
                    (recyclerView.layoutManager as? LinearLayoutManager)!!.findFirstVisibleItemPosition()
                )
            }
        }
    }
}