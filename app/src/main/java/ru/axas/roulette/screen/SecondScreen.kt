package ru.axas.roulette.screen


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.isFinished
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ru.axas.roulette.models.PatchRoulette
import ru.axas.roulette.util.DrawPieChart
import kotlin.random.Random

@Composable
fun SecondScreen(
    rouletteData: List<PatchRoulette>
) {
    val conf = LocalConfiguration.current
    val des = LocalDensity.current
    val bitmapPainter by remember {
        mutableStateOf(DrawPieChart().getPieChart(rouletteData,
            with(des) { (conf.screenWidthDp.dp - 30.dp).toPx().toInt() }
        ))
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentRotation by remember { mutableStateOf(0f) }
    var rouletteChoose: PatchRoulette? by remember { mutableStateOf(null) }
    val sumPieChart = rouletteData.sumOf { it.value.toDouble() }.toFloat()
    val actualRatio = currentRotation / 360

    MovePieChart(
        isPlaying = isPlaying,
        currentRotation = currentRotation,
        inStop = {
            var inPieChartsSum = 0f
            for (item in rouletteData) {
                inPieChartsSum += item.value
                if (inPieChartsSum > sumPieChart * actualRatio) {
                    rouletteChoose = item
                    break
                }
            }
        },
        inRotation = { currentRotation = it }
    )

    Surface(modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier
                .wrapContentSize()
                .rotate(currentRotation)
                .paint(painter = bitmapPainter)
            )
            Button(
                modifier = Modifier.padding(20.dp),
                onClick = {
                    isPlaying = !isPlaying
                }) {
                Text(text = "Stop/Start")
            }

            Text(
                modifier = Modifier.padding(20.dp),
                text = rouletteChoose?.text ?: "null"
            )
        }
    }
}

@Composable
fun MovePieChart(
    isPlaying: Boolean,
    currentRotation: Float,
    inStop: () -> Unit,
    inRotation: (Float) -> Unit,
) {
    var randomTime by remember { mutableStateOf(1000) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            randomTime = Random.nextInt(700, 1500)
            rotation.snapTo(currentRotation)
            rotation.animateTo(
                targetValue = currentRotation + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )) {
                inRotation.invoke(value % 360)
            }
        } else {
            rotation.snapTo(currentRotation)
            val randomCircle = Random.nextInt(100, 400).toFloat()
            val animationResult =   rotation.animateTo(
                targetValue = currentRotation + randomCircle,
                animationSpec = tween(
                    durationMillis = randomTime * 2,
                    easing = CubicBezierEasing(0.0f, 0.2f, 0.8f, 1.0f)
                )) {
                inRotation.invoke(value % 360)
            }
            snapshotFlow { animationResult.endState }.collect { isFinished ->
                if (isFinished.isFinished) {
                    inStop.invoke()
                }
            }
        }
    }
}

