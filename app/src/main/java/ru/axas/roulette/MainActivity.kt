package ru.axas.roulette

import android.content.ContentValues.TAG
import android.graphics.Region
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.axas.roulette.ui.theme.RouletteTheme
import kotlin.math.PI
import kotlin.math.atan2

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RouletteTheme {
                val isPlaying = remember { mutableStateOf(false) }
                var currentRotation by remember { mutableStateOf(0f) }
                val rotation = remember { Animatable(currentRotation) }

                LaunchedEffect(isPlaying.value) {

                    if (isPlaying.value) {
                        // Infinite repeatable rotation when is playing
                        rotation.animateTo(
                            targetValue = currentRotation + 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            )
                        ) {
                            currentRotation = value
                        }
                    } else {
                        if (currentRotation > 0f) {
                            // Slow down rotation on pause
                            rotation.animateTo(
                                targetValue = currentRotation + 50,
                                animationSpec = tween(
                                    durationMillis = 1250,
                                    easing = LinearOutSlowInEasing
                                )
                            ) {
                                currentRotation = value
                            }
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
                            .rotate(rotation.value),
                            contentAlignment = Alignment.Center
                        ) {

                            PieChart(patchRoulette)
                        }

                        Button(onClick = {
                            isPlaying.value = !isPlaying.value
                        }) {
                            Text(text = "Stop/Start")

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(dataPatch: List<PatchRoulette>,
             size: Dp = 200.dp) {
    val radiusInSize = size / 2
    var radiusInPx = 0F
    val sum = dataPatch.sum()
    var startAngle = -90f
    val regions = mutableListOf<Region>()
    val paths = mutableListOf<Path>()
    var start by remember { mutableStateOf(false) }
    val sweepPre by animateFloatAsState(
        targetValue = if (start) 1f else 0f,
        animationSpec = FloatTweenSpec(duration = 1000)
    )
    val proportions = dataPatch.map {
        it.value * 100 / sum
    }

    val sweepAngles = proportions.map {
        360 * it / 100
    }

    Canvas(
        modifier = Modifier
            .size(size = size)
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.Blue)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        val x = it.x - radiusInPx
                        val y = (it.y - radiusInPx) * -1
                        val a1 = atan2(x, y) * 180 / PI
                        val a2 = atan2(x * -1, y * -1) * 180 / PI
                        val grad = if (a1 < 0) 180 + a2 else a1
                        var position = -1
                        var sweepAnglesSum = 0F

                        run breaking@{
                            sweepAngles.forEachIndexed { index, angles ->
                                sweepAnglesSum += angles
                                if (grad < sweepAnglesSum) {
                                    position = index
                                    return@breaking
                                }
                            }
                        }

                        Log.e(TAG, "onTap: $position")

                    }
                )
            }
    ) {
        radiusInPx = radiusInSize.toPx()
        translate(radiusInPx, radiusInPx) {
            drawIntoCanvas {
                start = true

                dataPatch.forEachIndexed { index, patchRoulette ->

                    val rect = Rect(Offset(-radiusInPx, -radiusInPx),
                        Size(size.toPx(), size.toPx()))
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        arcTo(rect = rect, startAngle, sweepAngles[index] * sweepPre, false)
                    }

                    val left = path.getBounds().left
                    val top = path.getBounds().top
                    val right = path.getBounds().right
                    val bottom = path.getBounds().bottom

                    val region = Region(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

                    Log.e(TAG, "size rect: i = $index " +
                            "left = ${region.bounds.left}  " +
                            "top = ${region.bounds.top}  " +
                            "right = ${region.bounds.right} " +
                            "bottom = ${region.bounds.bottom}")


                    val paint = android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 10f
                        color = 0xFF000000.toInt()
                    }

                    val first = (left + top + right + bottom) / 4

                    Log.e(TAG,
                        "center rect: x = ${path.getBounds().topCenter.x}  y = ${path.getBounds().bottomCenter.y} ")

                    it.nativeCanvas.drawText(
                        dataPatch[index].text,
                        path.getBounds().topCenter.x,
                        path.getBounds().bottomCenter.y,
                        paint)

                    drawPath(path = path, color = patchRoulette.color)
                    path.reset()


                    startAngle += sweepAngles[index]


                }

            }
        }
    }
}


@Composable
fun PieChart2(
    dataPatch: List<PatchRoulette>,
    size: Dp = 200.dp) {

    var radiusInPx = 0F
    val sum = dataPatch.sum()
    var startAngle = -90f
    val radiusInSize = size / 2
    val regions = mutableListOf<Region>()
    val proportions = dataPatch.map { it.value * 100 / sum }
    val sweepAngles = proportions.map { 360 * it / 100 }

    Canvas(
        modifier = Modifier
            .size(size = size)
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.Blue)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        val x = it.x - radiusInPx
                        val y = (it.y - radiusInPx) * -1

                        val a1 = atan2(x, y) * 180 / PI
                        val a2 = atan2(x * -1, y * -1) * 180 / PI
                        val grad = if (a1 < 0) 180 + a2 else a1

//                        Log.e(TAG, "pointerInput onTap: x = $x y = $y")
//                        Log.e(TAG, "atan2 onTap: grad = $grad")
//                        Log.e(TAG, "sweepAngles onTap: ${sweepAngles.toList()}")

                        var position = -1
                        var sweepAnglesSum = 0F


                        run breaking@{
                            sweepAngles.forEachIndexed { index, angles ->
                                sweepAnglesSum += angles
                                if (grad < sweepAnglesSum) {
                                    position = index
                                    return@breaking
                                }
                            }
                        }

                        Log.e(TAG, "onTap: $position")

                    }
                )
            }
    ) {
        radiusInPx = radiusInSize.toPx()

        sweepAngles.forEachIndexed { index, fl ->

            drawIntoCanvas {
                drawArc(
                    color = dataPatch[index].color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngles[index],
                    useCenter = true
                )

                val paint = android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.RIGHT
                    textSize = 20f
                    color = 0xFF000000.toInt()
                }


                it.nativeCanvas.drawText(dataPatch[index].text, center.x, center.y, paint)
            }

            startAngle += sweepAngles[index]


        }
    }
}

data class PatchRoulette(
    val value: Float,
    val text: String,
    val color: Color,
)

fun List<PatchRoulette>.sum(): Float {
    var sum: Float = 0.0f
    this.forEach {
        sum += it.value
    }
    return sum
}


val patchRoulette = listOf<PatchRoulette>(
    PatchRoulette(
        value = 1.0f,
        text = "Скидка 5%",
        color = Color.Blue
    ),
    PatchRoulette(
        value = 1.0f,
        text = "Скидка 10%",
        color = Color.Yellow
    ),
    PatchRoulette(
        value = 1.0f,
        text = "Скидка 20%",
        color = Color.Green
    ),
    PatchRoulette(
        value = 1.0f,
        text = "Скидка 30%",
        color = Color.Gray
    ),
    PatchRoulette(
        value = 1.0f,
        text = "Скидка 40%",
        color = Color.Red
    ),
    PatchRoulette(
        value = 1.0f,
        text = "Скидка 50%",
        color = Color.Cyan
    ),
    PatchRoulette(
        value = 1.0f,
        text = "Скидка 60%",
        color = Color.Blue
    ),
    PatchRoulette(
        value = 1.0f,
        text = "Скидка 70%",
        color = Color.Magenta
    ),
    PatchRoulette(
        value = 1.0f,
        text = "Скидка 80%",
        color = Color.LightGray
    )
)