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

package com.sebastienbalard.skullscoring.extensions

import android.graphics.Color
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.sebastienbalard.skullscoring.R

fun Toolbar.showSnackBarError(
    message: String, duration: Int = Snackbar.LENGTH_SHORT
) {
    Snackbar.make(this, message, duration).apply {
        view.setBackgroundColor(Color.RED)
        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
            maxLines = 3
            setTextColor(Color.WHITE)
        }
        show()
    }
}

fun Toolbar.showSnackBarWarning(
    message: String, duration: Int = Snackbar.LENGTH_SHORT
) {
    Snackbar.make(this, message, duration).apply {
        view.setBackgroundColor(resources.getColor(R.color.orange, null))
        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
            maxLines = 3
            setTextColor(Color.WHITE)
        }
        show()
    }
}