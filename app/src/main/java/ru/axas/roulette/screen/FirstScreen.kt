package ru.axas.roulette.screen

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.axas.roulette.models.patchRoulette
import ru.axas.roulette.util.DrawPieChart

@Composable
fun FirstScreen() {
    val conf = LocalConfiguration.current
    val des = LocalDensity.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentRotation by remember { mutableStateOf(0f) }
    val bitmapPainter by remember {
        mutableStateOf(DrawPieChart().getPieChart(patchRoulette,
            with(des) { (conf.screenWidthDp.dp - 30.dp).toPx().toInt() }
        ))
    }
    val rotation = remember { Animatable(currentRotation) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            rotation.animateTo(
                targetValue = currentRotation + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            ) {
                currentRotation = value % 360
            }
        }

        if (currentRotation > 0f && !isPlaying) {
                // Slow down rotation on pause
                rotation.animateTo(
                    targetValue = currentRotation + 100,
                    animationSpec = tween(
                        durationMillis = 3000,
                        easing = LinearOutSlowInEasing
                    )
                ) {
                    currentRotation = value
                    val stoppedIndex = (value / 800 * patchRoulette.size).toInt()
                    val stoppedPatch = patchRoulette.getOrNull(stoppedIndex)
                    Log.d("FirstScreen", "$value - ${stoppedPatch?.text}")
                }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier
                .wrapContentSize()
                .rotate(currentRotation).
                paint(painter = bitmapPainter)
            )
            Button(
                modifier = Modifier.padding(20.dp),
                onClick = {
                    isPlaying = !isPlaying
                }) {
                Text(text = "Stop/Start")
            }
        }
    }
}