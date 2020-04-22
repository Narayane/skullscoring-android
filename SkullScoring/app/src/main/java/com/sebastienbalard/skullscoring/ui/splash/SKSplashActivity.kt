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

package com.sebastienbalard.skullscoring.ui.splash

import com.sebastienbalard.skullscoring.ui.SBActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Observer
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.ui.game.SKGameCreationActivity
import com.sebastienbalard.skullscoring.ui.home.SKHomeActivity
import kotlinx.android.synthetic.main.activity_splash.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

open class SKSplashActivity : SBActivity(R.layout.activity_splash) {

    internal val viewModel: SKSplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.states.observe(this, Observer { state ->
            state?.let {
                Timber.v("state -> ${it::class.java.simpleName}")
                when (it) {
                    is StateSplashConfig -> textViewTitle.text = "Configuration"
                    else -> {
                    }
                }
            }
        })

        viewModel.events.observe(this, Observer { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventSplashStartOnboarding -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            startActivity(
                                SKGameCreationActivity.getIntent(
                                    this@SKSplashActivity
                                )
                            )
                        }, 2000)
                    }
                    is EventSplashGoToHome -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            startActivity(
                                SKHomeActivity.getIntent(
                                    this@SKSplashActivity
                                )
                            )
                        }, 2000)
                    }
                    else -> {
                    }
                }
            }
        })

        viewModel.loadConfig()
    }
}
