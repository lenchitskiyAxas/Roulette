package ru.axas.roulette.util

import android.graphics.Paint
import android.graphics.PathMeasure
import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import ru.axas.roulette.models.PatchRoulette
import kotlin.math.PI
import kotlin.math.atan2

@Composable
fun PieChartCanvas(
    modifier: Modifier = Modifier,
    dataPatch: List<PatchRoulette>,
) {
    val sum by remember(dataPatch) {
        mutableStateOf(dataPatch.sumOf { it.value.toDouble() }.toFloat())
    }
    var startAngle by remember { mutableStateOf(-0f) }
    Canvas(modifier = modifier) {
        dataPatch.forEachIndexed { _, patchRoulette ->
            val sweepAngle = calculateSweepAngle(patchRoulette.value, sum)
            pieChartTriangle(
                text = patchRoulette.text,
                color = patchRoulette.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
            )
            startAngle += sweepAngle
        }
    }
}

fun DrawScope.pieChartTriangle(
    text: String,
    color: Color,
    startAngle: Float,
    sweepAngle: Float,
) {
    val radiusCircle = size.minDimension / 2
    val trianglePath = Path().apply {
        moveTo(radiusCircle, radiusCircle)
        arcTo(
            Rect(0f, 0f, radiusCircle * 2, radiusCircle * 2),
            startAngle,
            sweepAngle,
            false
        )
        close()
    }
    drawPath(path = trianglePath, color = color)
    val vertices = getTriangleVertices(trianglePath)
    if (vertices.size != 3) return
    val medianXCurrent = (vertices[1].x + vertices[2].x) / 2
    val medianYCurrent = (vertices[1].y + vertices[2].y) / 2
    Log.i("pieChartTriangle", "$medianXCurrent")
    val textSize = radiusCircle * 0.1f
    drawTextOnLineApp(
        text = text,
        textColor = Color.Black,
        textSize = textSize,
        moveTo = Offset(medianXCurrent, medianYCurrent),
        lineTo = Offset(vertices[0].x, vertices[0].y),
        offsetX = 0f,
        offsetY = textSize / 2,
    )
}

private fun DrawScope.drawTextOnLineApp(
    text: String,
    textColor: Color,
    textSize: Float,
    moveTo: Offset,
    lineTo: Offset,
    offsetX: Float = 0f,
    offsetY: Float = 0f,
) {
    val paint = Paint()
    paint.textAlign = Paint.Align.LEFT
    paint.textSize = textSize
    paint.color = textColor.toArgb()

    val textPath = Path()
    textPath.moveTo(moveTo.x, moveTo.y)
    textPath.lineTo(lineTo.x, lineTo.y)

    drawIntoCanvas {
        it.nativeCanvas.drawTextOnPath(
            text,
            textPath.asAndroidPath(),
            offsetX,
            offsetY,
            paint
        )
        it.nativeCanvas.save()
    }
}

private fun getTriangleVertices(path: Path): List<PointF> {
    val vertices = mutableListOf<PointF>()
    val pathMeasure = PathMeasure(path.asAndroidPath(), false)
    val length = pathMeasure.length
    val stepSize = length / 3
    for (i in 0..2) {
        val position = FloatArray(2)
        val tangent = FloatArray(2)
        val distance = i * stepSize
        pathMeasure.getPosTan(distance, position, tangent)
        vertices.add(PointF(position[0], position[1]))
    }
    return vertices
}

//    val proportions = remember(dataPatch) { dataPatch.map { it.value * 100 / sum } }
//    val sweepAngles = remember(proportions) { proportions.map { 360 * it / 100 } }


//            .pointerInput(Unit) {
//                val radiusInPx = this.size.height / 2
//                detectTapGestures(
//                    onTap = { offset ->
//                        val grad = getGradFromOffsetChart(
//                            offsetX = offset.x,
//                            offsetY = offset.y,
//                            radiusInPx = radiusInPx)
//
//                        val position = findPosition(
//                            grad = grad,
//                            sweepAngles = sweepAngles
//                        )
//                        if (position != -1) {
//                            val text = dataPatch[position].text
//                            Log.d("PieChartCanvas", "Triangle $position text: $text")
//                        }
//                    }
//                )
//            }

fun getGradFromOffsetChart(offsetX: Float, offsetY: Float, radiusInPx: Int): Double {
    val x = offsetX - radiusInPx
    val y = (offsetY - radiusInPx) * -1
    val a1 = atan2(x, y) * 180 / PI
    val a2 = atan2(x * -1, y * -1) * 180 / PI
    return if (a1 < 0) 180 + a2 else a1
}

fun findPosition(grad: Double, sweepAngles: List<Float>): Int {
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
    return position
}

