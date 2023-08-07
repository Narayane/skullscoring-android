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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.sebastienbalard.skullscoring.ui.theme.Primary
import com.sebastienbalard.skullscoring.ui.theme.Secondary
import com.sebastienbalard.skullscoring.ui.theme.SkullScoringTheme

class SKSplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkullScoringTheme {
                SplashScreen()
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Surface {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val guideline = createGuidelineFromTop(0.55f)
            val (card, text, indicator, label) = createRefs()

            Card(
                modifier = Modifier
                    .size(200.dp)
                    .constrainAs(card) {
                        bottom.linkTo(guideline)
                        centerHorizontallyTo(parent)
                    },
                shape = RoundedCornerShape(8.dp),
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .background(Primary)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_logo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentDescription = "@null"
                    )
                }
            }
            Text(
                modifier = Modifier.constrainAs(text) {
                    top.linkTo(guideline, margin = 24.dp)
                    centerHorizontallyTo(parent)
                },
                text = stringResource(id = R.string.app_name),
                style = TextStyle(fontSize = 36.sp)
            )
            CircularProgressIndicator(
                modifier = Modifier.constrainAs(indicator) {
                    bottom.linkTo(label.top, margin = 16.dp)
                    centerHorizontallyTo(parent)
                }, color = Secondary
            )
            Text(
                modifier = Modifier.constrainAs(label) {
                    bottom.linkTo(parent.bottom, margin = 16.dp)
                    centerHorizontallyTo(parent)
                },
                text = "Initialisation"
            )
        }
    }
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    SkullScoringTheme {
        SplashScreen()
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    SkullScoringTheme(darkTheme = true) {
        SplashScreen()
    }
}