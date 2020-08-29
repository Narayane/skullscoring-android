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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.sebastienbalard.skullscoring.R
import com.sebastienbalard.skullscoring.SBCrashReport
import com.sebastienbalard.skullscoring.ui.EventDataSendingPermissionsLoaded
import com.sebastienbalard.skullscoring.ui.EventSplashGoToHome
import com.sebastienbalard.skullscoring.ui.EventSplashStartOnboarding
import com.sebastienbalard.skullscoring.ui.SBActivity
import com.sebastienbalard.skullscoring.ui.home.SKHomeActivity
import kotlinx.android.synthetic.main.activity_data_sending_permission.*
import kotlinx.android.synthetic.main.widget_appbar.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SBDataPermissionActivity : SBActivity(R.layout.activity_data_sending_permission) {

    internal open val viewModel: SKOnboardingViewModel by viewModel()
    internal val crashReport: SBCrashReport by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")

        initUI()
        initObservers()

        viewModel.loadDataSendingPermissions()
    }

    private fun initObservers() {
        viewModel.events.observe(this, { event ->
            event?.apply {
                Timber.v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventDataSendingPermissionsLoaded -> {
                        switchAllowCrashDataSending.isChecked = allowCrashDataSending
                        switchAllowUseDataSending.isChecked = allowUseDataSending
                    }
                    is EventSplashStartOnboarding -> {
                        Handler(Looper.getMainLooper()).post {
                            startActivity(
                                SKOnboardingActivity.getIntent(
                                    this@SBDataPermissionActivity
                                )
                            )
                        }
                    }
                    is EventSplashGoToHome -> {
                        Handler(Looper.getMainLooper()).post {
                            startActivity(
                                SKHomeActivity.getIntent(
                                    this@SBDataPermissionActivity
                                )
                            )
                        }
                    }
                    else -> {
                    }
                }
            }
        })
    }

    private fun initUI() {
        toolbar.title = getString(R.string.first_game)

        textViewDataSendingProcessing.text =
            getString(R.string.data_permission_processing, getString(R.string.app_name))
        textViewDataSendingCrashDescription.text =
            getString(R.string.data_permission_crash_description, getString(R.string.app_name))
        textViewDataSendingUseDescription.text =
            getString(R.string.data_permission_use_description, getString(R.string.app_name))

        buttonValidateDataPermissions.setOnClickListener {
            val message =
                "set initial data sending permissions: crash (${switchAllowCrashDataSending.isChecked}), use (${switchAllowUseDataSending.isChecked})"
            Timber.i(message)
            crashReport.logInfo(
                SBDataPermissionActivity::class.java.simpleName, message
            )
            viewModel.saveDataSendingPermissions(
                switchAllowCrashDataSending.isChecked, switchAllowUseDataSending.isChecked
            )
        }

    }

    companion object {

        fun getIntent(context: Context): Intent {
            return Intent(
                context, SBDataPermissionActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
    }
}