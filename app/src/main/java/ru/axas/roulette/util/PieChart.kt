package ru.axas.roulette.util

import android.content.ContentValues
import android.graphics.Paint
import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ru.axas.roulette.models.PatchRoulette
import kotlin.math.atan2

@Composable
fun PieChart(
    dataPatch: List<PatchRoulette>,
) {
    val den = LocalDensity.current
    val conf = LocalConfiguration.current
    val sizePieDp by remember { mutableStateOf(conf.screenWidthDp.dp - 50.dp) }
    val radiusInPx by remember { mutableStateOf(with(den) { (sizePieDp / 2).toPx() }) }
    val sum by remember(dataPatch) {
        mutableStateOf(dataPatch.sumOf { it.value.toDouble() }.toFloat())
    }
    var startAngle by remember { mutableStateOf(-90f) }

    Canvas(
        modifier = Modifier
            .size(size = sizePieDp)
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.Blue)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val grad = getGradFromOffset(offset, radiusInPx)
                        val position = getPositionFromGrad(grad, dataPatch)
                        Log.e(ContentValues.TAG, "onTap: position = $position, grad = $grad")
                        Log.e(ContentValues.TAG,
                            "onTap: offsetX = ${offset.x}, offsetY = ${offset.y}")
                    }
                )
            }
    ) {
        translate(radiusInPx, radiusInPx) {
                dataPatch.forEachIndexed { index, patchRoulette ->
                    val sweepAngle = calculateSweepAngle(patchRoulette.value, sum)
                    val path = createPath(startAngle, sweepAngle, with(den) { sizePieDp.toPx() })

                    drawPath(path = path, color = patchRoulette.color)

                    val bounds = path.getBounds()
                    val centerX = bounds.center.x
                    val centerY = bounds.center.y
                    val rotate = calculateTextRotation(centerX, centerY)

                    val vertex1X = bounds.left
                    val vertex1Y = bounds.top + (bounds.height / 2)
                    val vertex2X = bounds.right
                    val vertex2Y = bounds.top + (bounds.height / 2)
                    val vertex3X = bounds.center.x
                    val vertex3Y = bounds.bottom

                    val paint = Paint().apply {
                        textAlign = Paint.Align.CENTER
                        textSize = (sizePieDp.value / 5)
                        color = 0xFF000000.toInt()
                    }

                    val textHeight = paint.descent() - paint.ascent()
                    val textOffset = (textHeight / 2) - paint.descent()

                    val textWidth = paint.measureText(dataPatch[index].text)
                    val textHeight2 = paint.fontMetrics.bottom - paint.fontMetrics.top

                    val (textLeft, textTop) = calculateTextPositionInTriangle(
                        vertex1X = vertex1X,
                        vertex1Y = vertex1Y,
                        vertex2X = vertex2X,
                        vertex2Y = vertex2Y,
                        vertex3X = vertex3X,
                        vertex3Y = vertex3Y,
                        textWidth = textWidth,
                        textHeight = textHeight2,
                    )

                    drawIntoCanvas {
                        it.rotate(-rotate, centerX, centerY)

//                    it.nativeCanvas.drawText(
//                        dataPatch[index].text,
//                        centerX ,
//                        centerY + textOffset ,
//                        paint
//                    )

                        it.nativeCanvas.drawText(
                            dataPatch[index].text,
                            textLeft + textOffset,
                            textTop,
                            paint
                        )
                        it.rotate(rotate, centerX, centerY)
                        it.nativeCanvas.save()
                    }
                    startAngle += sweepAngle

            }
        }
    }
}


@Composable
fun PieOnlyColor(
    dataPatch: List<PatchRoulette>,
) {

    val conf = LocalConfiguration.current
    val sizePieDp by remember { mutableStateOf(conf.screenWidthDp.dp - 50.dp) }
    val sum by remember(dataPatch) {
        mutableStateOf(dataPatch.sumOf { it.value.toDouble() }.toFloat())
    }
    var startAngle by remember { mutableStateOf(0f) }

    Canvas(
        modifier = Modifier.size(size = sizePieDp)
    ) {

        val radius = size.minDimension / 2
        val centerXTriangle = size.width / 2
        val centerYTriangle = size.height / 2

        dataPatch.forEachIndexed { index, patchRoulette ->
            val sweepAngle = calculateSweepAngle(patchRoulette.value, sum)

            val path = createPathTriangle(
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                centerX = centerXTriangle,
                centerY = centerYTriangle,
                radius = radius)

            val paint = Paint().apply {
                textAlign = Paint.Align.CENTER
                textSize = (sizePieDp.value / 5)
                color = 0xFF000000.toInt()
            }

            val bounds = path.getBounds()

            val vertex1X = bounds.left
            val vertex1Y = bounds.top + (bounds.height / 2)
            val vertex2X = bounds.right
            val vertex2Y = bounds.top + (bounds.height / 2)
            val vertex3X = bounds.center.x
            val vertex3Y = bounds.bottom
            val centerY = bounds.center.y


            val textWidth = paint.measureText(dataPatch[index].text)
            val textHeight2 = paint.fontMetrics.bottom - paint.fontMetrics.top

            val rotate = calculateTextRotation(bounds.center.x, bounds.center.y)

            val (textLeft, textTop) = calculateTextPositionInTriangle(
                vertex1X = vertex1X,
                vertex1Y = vertex1Y,
                vertex2X = vertex2X,
                vertex2Y = vertex2Y,
                vertex3X = vertex3X,
                vertex3Y = vertex3Y,
                textWidth = textWidth,
                textHeight = textHeight2,
            )

            drawPath(path, color = patchRoulette.color)
            drawIntoCanvas {
                it.rotate(-rotate, vertex3X, centerY)
                it.nativeCanvas.drawText(
                    dataPatch[index].text,
                    textLeft,
                    textTop,
                    paint
                )
                it.rotate(rotate, vertex3X, centerY)
                it.nativeCanvas.save()
            }
            startAngle += sweepAngle
        }
    }
}

private fun calculateTextPositionInTriangle(
    vertex1X: Float,
    vertex1Y: Float,
    vertex2X: Float,
    vertex2Y: Float,
    vertex3X: Float,
    vertex3Y: Float,
    textWidth: Float,
    textHeight: Float
): Pair<Float, Float> {
    val triangleCenter = calculateTriangleCenter(
        PointF(vertex1X, vertex1Y),
        PointF(vertex2X, vertex2Y),
        PointF(vertex3X, vertex3Y)
    )
    val textLeft = triangleCenter.x - textWidth / 2
    val textTop = triangleCenter.y - textHeight / 2

    return textLeft to textTop
}

private fun calculateTriangleCenter(p1: PointF, p2: PointF, p3: PointF): PointF {
    val centerX = (p1.x + p2.x + p3.x) / 3
    val centerY = (p1.y + p2.y + p3.y) / 3
    return PointF(centerX, centerY)
}

fun getGradFromOffset(offset: Offset, radiusInPx: Float): Float {
    val x = offset.x - radiusInPx
    val y = (offset.y - radiusInPx) * -1
    return Math.toDegrees(atan2(x, y).toDouble()).toFloat()
}

fun getPositionFromGrad(grad: Float, dataPatch: List<PatchRoulette>): Int {
    val totalProportions = dataPatch.sumOf { it.value.toDouble() }.toFloat()
    var sweepAnglesSum = 0f
    dataPatch.forEachIndexed { index, patchRoulette ->
        sweepAnglesSum += calculateSweepAngle(patchRoulette.value, totalProportions)
        if (grad < sweepAnglesSum) {
            return index
        }
    }
    return -1
}

fun calculateSweepAngle(value: Float, totalValue: Float): Float {
    return 360 * (value / totalValue)
}

private fun createPathTriangle(
    startAngle: Float,
    sweepAngle: Float,
    centerX: Float,
    centerY: Float,
    radius: Float
): Path {
    return Path().apply {
        moveTo(centerX, centerY)
        arcTo(
            Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius),
            startAngle,
            sweepAngle,
            false
        )
        close()
    }
}

private fun createPath(startAngle: Float, sweepAngle: Float, sizePx: Float): Path {
    val radius = sizePx / 2
    val rect = Rect(Offset(-radius, -radius), Size(sizePx, sizePx))
    return Path().apply {
        moveTo(0f, 0f)
        arcTo(rect = rect, startAngle, sweepAngle, false)
        close()
    }
}

private fun calculateTextRotation(centerX: Float, centerY: Float): Float {
    val a1 = Math.toDegrees(atan2(centerX.toDouble(), centerY.toDouble())).toFloat()
    return a1 - 90
}

