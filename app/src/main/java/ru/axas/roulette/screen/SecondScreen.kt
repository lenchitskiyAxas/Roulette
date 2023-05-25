package ru.axas.roulette.screen


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.isFinished
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.axas.roulette.R
import ru.axas.roulette.models.PatchRoulette
import ru.axas.roulette.models.RouletteData
import kotlin.math.absoluteValue
import kotlin.random.Random

@Composable
fun SecondScreen(
    rouletteData: RouletteData
) {

    var isPlaying by remember { mutableStateOf(false) }
    var currentRotation by remember { mutableStateOf(0f) }
    var rouletteChoose: PatchRoulette? by remember { mutableStateOf(null) }

    MovePieChart(
        isPlaying = isPlaying,
        currentRotation = currentRotation,
        inStop = {
            rouletteChoose = rouletteData.getChoosePatch(currentRotation)
        },
        inRotation = { currentRotation = it }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.image_casino),
                contentScale = ContentScale.FillHeight,
            )
            .background(Color.Black.copy(0.5f)),
    ) {

        RouletteBox(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        isPlaying = !isPlaying
                        rouletteChoose = null
                    }) ,
            background = rouletteData.background,
            roulette = rouletteData.roulette,
            arrow = rouletteData.arrow,
            center = rouletteData.center,
            currentRotation = currentRotation
        )

        rouletteChoose?.drawableRes?.let { drawable ->
            Box(modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(20.dp)
                .paint(painter = painterResource(id = drawable))
            )
        }
    }
}

@Composable
fun FirstScreen(
    rouletteData: RouletteData
) {

    var currentRotation by remember { mutableStateOf(0f) }
    var rouletteChoose: PatchRoulette? by remember {
        mutableStateOf(rouletteData.getChoosePatch(currentRotation))
    }
    val angle: Float by animateFloatAsState(
        targetValue = currentRotation,
        animationSpec = tween(8000,
            easing = CubicBezierEasing(0.7f, 0.2f, 0.1f, 0.8f)),
        finishedListener = {
            rouletteChoose = rouletteData.getChoosePatch(currentRotation)
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.image_casino),
                contentScale = ContentScale.FillHeight,
            )
            .background(Color.Black.copy(0.5f)),
    ) {

        RouletteBox(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        currentRotation += (3000..10000).random().toFloat()
                        rouletteChoose = null
                    })
                .fillMaxWidth(),
            background = rouletteData.background,
            roulette = rouletteData.roulette,
            arrow = rouletteData.arrow,
            center = rouletteData.center,
            currentRotation = angle
        )

        rouletteChoose?.drawableRes?.let { drawable ->
            Box(modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(20.dp)
                .paint(painter = painterResource(id = drawable))
            )
        }

    }
}

@Composable
fun RouletteBox(
    modifier:Modifier,
    background: Painter?,
    roulette: Painter?,
    arrow: Painter?,
    center: Painter?,
    currentRotation:Float
){
    Box(modifier = modifier) {

        background?.let { image ->
            Box(modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .paint(painter = image)
            )
        }

        roulette?.let { image ->
            Box(modifier = Modifier
                .padding(38.dp)
                .align(Alignment.Center)
                .fillMaxWidth()
                .rotate(currentRotation)
                .paint(painter = image)
            )
        }

        arrow?.let { image ->
            Box(modifier = Modifier
                .padding(30.dp)
                .size(30.dp)
                .align(Alignment.TopCenter)
                .paint(
                    painter = image,
                    contentScale = ContentScale.FillHeight
                )
            )
        }

        center?.let { image ->
            Box(modifier = Modifier
                .size(30.dp)
                .align(Alignment.Center)
                .paint(painter = image)
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
                    animation = tween(1000, easing = CubicBezierEasing(0.6f, 0.4f, 0.6f, 0.7f)),
                    repeatMode = RepeatMode.Restart
                )) {
                inRotation.invoke(value % 360)
            }
        } else {
            rotation.snapTo(currentRotation)
            val randomCircle = Random.nextInt(100, 400).toFloat()
            val animationResult = rotation.animateTo(
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

