package ru.axas.roulette

import android.content.ContentValues.TAG
import android.graphics.Region
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FloatTweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
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
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        PieChart2(patchRoulette)
                    }

                }
            }
        }
    }
}

private fun getPositionFromAngle(angles: List<Float>, touchAngle: Double): Int {
    var totalAngle = 0f
    for ((i, angle) in angles.withIndex()) {
        totalAngle += angle
        if (touchAngle.toFloat() == totalAngle) {
            return i
        }
    }
    return -1
}


@Composable
fun PieChart(dataPatch: List<PatchRoulette>,
             size: Dp = 200.dp) {
    val radiusInSize = size / 2
    var radiusInPx = 0F
    val sum = dataPatch.sum()
    var startAngle = 0f
    val angles = mutableListOf<Float>()
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
                        val y = it.y - radiusInPx
//
//                        Log.e(TAG, "brute onTap: x = ${it.x} y = ${it.y}")
                        Log.e(TAG, "pointerInput onTap: x = $x y = $y")

                        var position = -1

                        regions.forEachIndexed { index, region ->

//                            Log.e(TAG, "onTapList: top ${region.bounds.left} x ${region.bounds.top} x ${region.bounds.right} x ${region.bounds.bottom}")
                            if (region.bounds.contains(x.toInt(), y.toInt())) {
                                position = index
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
                            "top = ${region.bounds.top.toInt()}  " +
                            "right = ${region.bounds.right.toInt()} " +
                            "bottom = ${region.bounds.bottom.toInt()}")

                    paths.add(index, path)
                    regions.add(index, region)
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
        for (i in sweepAngles.indices) {
            drawArc(
                color = dataPatch[i].color,
                startAngle = startAngle,
                sweepAngle = sweepAngles[i],
                useCenter = true
            )

            startAngle += sweepAngles[i]
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