/**
 * Copyright © 2021 Skull Scoring (Sébastien BALARD)
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

package com.sebastienbalard.skullscoring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sebastienbalard.skullscoring.ui.theme.Secondary
import com.sebastienbalard.skullscoring.ui.theme.SkullScoringTheme
import org.koin.androidx.compose.koinViewModel

class SKDataSendingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkullScoringTheme {
                DataSendingScreen()
            }
        }
    }
}

@Composable
fun DataSendingScreen(dataSendingViewModel: SKDataSendingViewModel = koinViewModel()) {

    //var allowCrashDataSending by rememberSaveable { mutableStateOf(true) }
    //var allowUseDataSending by rememberSaveable { mutableStateOf(false) }
    val dataSendingUiState by dataSendingViewModel.uiState.collectAsState()

    DataSendingContent(
        dataSendingUiState.isCrashDataSendingAllowed,
        dataSendingUiState.isUseDataSendingAllowed,
        {
            dataSendingViewModel.isCrashDataSendingAllowed = it
        },
        {
            dataSendingViewModel.isUseDataSendingAllowed = it
        })
    DataSendingButton { dataSendingViewModel.saveDataSendingPermissions() }
}

@Composable
fun DataSendingContent(
    isCrashDataSendingAllowed: Boolean,
    isUseDataSendingAllowed: Boolean,
    onCrashDataSendingChanged: (Boolean) -> Unit,
    onUseDataSendingChanged: (Boolean) -> Unit
) {
    Surface {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                text = stringResource(id = R.string.data_permission_title),
                style = TextStyle(
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                text = stringResource(id = R.string.data_permission_processing),
                style = TextStyle(fontSize = 17.sp)
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                text = stringResource(id = R.string.data_permission_crash_description),
                style = TextStyle(fontSize = 14.sp)
            )
            Row(
                modifier = Modifier
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(1.0f, true)
                        .padding(end = 16.dp),
                    text = stringResource(id = R.string.data_permission_crash_label),
                    style = TextStyle(fontSize = 14.sp)
                )
                Switch(
                    checked = isCrashDataSendingAllowed,
                    onCheckedChange = onCrashDataSendingChanged
                )
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                text = stringResource(id = R.string.data_permission_use_description),
                style = TextStyle(fontSize = 14.sp)
            )
            Row(
                modifier = Modifier
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(1.0f, true)
                        .padding(end = 16.dp),
                    text = stringResource(id = R.string.data_permission_use_label),
                    style = TextStyle(fontSize = 14.sp)
                )
                Switch(
                    checked = isUseDataSendingAllowed,
                    onCheckedChange = onUseDataSendingChanged
                )
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                text = stringResource(id = R.string.data_permission_warning),
                style = TextStyle(fontSize = 17.sp)
            )
        }
    }
}

@Composable
fun DataSendingButton(
    onDataSendingValidated: () -> Unit
) {
    Button(
        modifier = Modifier
            .padding(top = 32.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Secondary,
            contentColor = Color.White
        ),
        onClick = onDataSendingValidated
    ) {
        Text(stringResource(id = R.string.validate_choices))
    }
}

@Preview("Light Theme", widthDp = 360, heightDp = 640, showSystemUi = true, showBackground = true)
@Composable
fun DataSendingScreenLightPreview() {
    SkullScoringTheme {
        DataSendingScreen()
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640, showSystemUi = true, showBackground = true)
@Composable
fun DataSendingScreenDarkPreview() {
    SkullScoringTheme(darkTheme = true) {
        DataSendingScreen()
    }
}
