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

package com.sebastienbalard.skullscoring.ui.onboarding

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.sebastienbalard.skullscoring.BuildConfig
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.ui.*
import com.sebastienbalard.skullscoring.ui.home.SKHomeActivity
import kotlinx.android.synthetic.main.activity_splash.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

open class SKSplashActivity : SBActivity(R.layout.activity_splash) {

    internal val viewModel: SKOnboardingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initObservers()

        viewModel.requestDataSendingPermissions()
    }

    private fun openDataPermissionScreen() {
        startActivity(
            SBDataPermissionActivity.getIntent(this)
        )
    }

    private fun initObservers() {
        viewModel.states.observe(this, { state ->
            state?.let {
                Timber.v("state -> ${it::class.java.simpleName}")
                when (it) {
                    is StateSplashConfig -> textViewTitle.text = getString(R.string.configuration)
                    else -> {
                    }
                }
            }
        })

        viewModel.events.observe(this, { event ->
            event?.let {
                Timber.v("event -> ${it::class.java.simpleName}")
                when (it) {
                    is EventSplashStartOnboarding -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            startActivity(
                                SKOnboardingActivity.getIntent(
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
                    is EventSplashRequestDataPermissions -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (it.needed or BuildConfig.DEBUG) {
                                openDataPermissionScreen()
                            } else {
                                viewModel.load()
                            }
                        }, 2000)
                    }
                    else -> {
                    }
                }
            }
        })
    }
}
