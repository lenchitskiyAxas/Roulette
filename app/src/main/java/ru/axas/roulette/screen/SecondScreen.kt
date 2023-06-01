package ru.axas.roulette.screen


import android.animation.TimeInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.isFinished
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
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
import ru.axas.roulette.util.rememberOpenIntentUrl
import kotlin.random.Random

private val FirstEasing = DecelerateInterpolator().toEasing()

fun TimeInterpolator.toEasing() = Easing { x ->
    getInterpolation(x)
}

@Composable
fun MainScreen(
    rouletteData: RouletteData
) {
    var isInfinityRoulette by remember { mutableStateOf(false) }
    var rouletteChoose: PatchRoulette? by remember { mutableStateOf(null) }
val openUri = rememberOpenIntentUrl()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.image_casino),
                contentScale = ContentScale.FillHeight,
            )
            .background(Color.Black.copy(0.5f))
            .systemBarsPadding(),
    ) {

        when (isInfinityRoulette) {
            true -> InfinityRoulette(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                rouletteData = rouletteData,
                inStop = {
                    rouletteChoose = it
                }
            )
            false -> OneClickRoulette(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                rouletteData = rouletteData,
                inStop = {
                    rouletteChoose = it
                }
            )
        }

        rouletteChoose?.drawableRes?.let { drawable ->
            Box(modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(20.dp)
                .paint(painter = painterResource(id = drawable))
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .clip(RoundedCornerShape(10))
                .background(Color.White.copy(.2f))
                .padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Switch(
                modifier = Modifier
                    .padding(start = 10.dp),
                checked = isInfinityRoulette,
                onCheckedChange = {
                    isInfinityRoulette = it
                    rouletteChoose = null
                })

            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = "Infinity",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                modifier = Modifier.padding(10.dp),
                onClick = {
                    openUri.invoke(null, "lucky_delivery://pay") }) {
                Text(text = "open = custom URI")
            }
        }



    }
}


@Composable
fun InfinityRoulette(
    modifier: Modifier = Modifier,
    rouletteData: RouletteData,
    inStop: (PatchRoulette?) -> Unit,
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentRotation by remember { mutableStateOf(0f) }

    MovePieChart(
        isPlaying = isPlaying,
        currentRotation = currentRotation,
        inStop = {
            inStop.invoke(rouletteData.getChoosePatch(currentRotation))
        },
        inRotation = { currentRotation = it }
    )

    RouletteBox(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPlaying = !isPlaying
                    inStop.invoke(null)
                }),
        background = rouletteData.background,
        roulette = rouletteData.roulette,
        arrow = rouletteData.arrow,
        center = rouletteData.center,
        currentRotation = currentRotation
    )
}


@Composable
fun OneClickRoulette(
    modifier: Modifier = Modifier,
    rouletteData: RouletteData,
    inStop: (PatchRoulette?) -> Unit,
) {
    var currentRotation by remember { mutableStateOf(0f) }
    val angle: Float by animateFloatAsState(
        targetValue = currentRotation,
        animationSpec = tween(10000,
            easing = FirstEasing),
        finishedListener = {
            inStop.invoke(rouletteData.getChoosePatch(currentRotation))
        }
    )
    RouletteBox(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    currentRotation += (3000..10000)
                        .random()
                        .toFloat()
                    inStop.invoke(null)
                })
            .fillMaxWidth(),
        background = rouletteData.background,
        roulette = rouletteData.roulette,
        arrow = rouletteData.arrow,
        center = rouletteData.center,
        currentRotation = angle
    )
}

@Composable
fun RouletteBox(
    modifier: Modifier,
    background: Painter?,
    roulette: Painter?,
    arrow: Painter?,
    center: Painter?,
    currentRotation: Float
) {
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
                    animation = tween(randomTime, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )) {
                inRotation.invoke(value % 360)
            }
        } else if (currentRotation != 0f) {
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

