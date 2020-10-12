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

package com.sebastienbalard.skullscoring.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.LayoutRes
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.extensions.showSnackBarWarning
import com.sebastienbalard.skullscoring.ui.home.SKHomeActivity
import com.sebastienbalard.skullscoring.ui.settings.SKAboutActivity
import com.sebastienbalard.skullscoring.ui.settings.SKSettingsActivity
import kotlinx.android.synthetic.main.widget_appbar.*
import kotlinx.android.synthetic.main.widget_bottomnavigationview.*
import timber.log.Timber

abstract class SBBottomNavigationViewActivity(@LayoutRes contentLayoutId: Int) : SBActivity(
    contentLayoutId
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initUI()
    }

    override fun onStart() {
        super.onStart()
        bottomNavigationView.menu.findItem(getBottomNavigationMenuItemId()).isChecked = true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Timber.v("onCreateOptionsMenu")
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_home_item_settings -> {
                Timber.i("click on menu item: settings")
                startActivity(SKSettingsActivity.getIntent(this))
                true
            }
            R.id.menu_home_item_about -> {
                Timber.i("click on menu item: about")
                startActivity(SKAboutActivity.getIntent(this))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    abstract fun getBottomNavigationMenuItemId(): Int

    private fun initUI() {
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            bottomNavigationView.post {
                when (menuItem.itemId) {
                    R.id.menu_bottom_navigation_item_games -> {
                        startActivity(SKHomeActivity.getIntent(this))
                        finish()
                    }
                    R.id.menu_bottom_navigation_item_players -> {
                        startActivity(SKPlayersActivity.getIntent(this))
                        finish()
                    }
                    R.id.menu_bottom_navigation_item_statistics -> {
                        toolbar.showSnackBarWarning(
                            getString(R.string.warning_not_implemented)
                        )
                    }
                }
                //finish()
            }
        }
    }
}