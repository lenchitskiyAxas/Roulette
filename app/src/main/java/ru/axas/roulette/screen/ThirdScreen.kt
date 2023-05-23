package ru.axas.roulette.screen

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.axas.roulette.models.PatchRoulette
import ru.axas.roulette.models.patchRoulette
import ru.axas.roulette.util.calculateSweepAngle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import android.graphics.Rect as RectNative

@Composable
fun ThirdScreen() {
//    DividedCircle()
    WheelOfFortune(patchRoulette)
}

@Composable
fun DividedCircleWithText() {
    val circleColor = Color.Red
    val textColor = Color.White
    val textSize = 20.sp

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
        ) {
            val radius = size.minDimension / 2
            val centerX = size.width / 2
            val centerY = size.height / 2
            val angleOffset = -45f // Смещение угла, чтобы начать с вертикальной позиции
            val textOffset = 180f // Смещение текста относительно треугольника


            repeat(4) { index ->
                val startAngle = index * 90f + angleOffset
                val sweepAngle = 90f

                val path = Path().apply {
                    moveTo(centerX, centerY)
                    arcTo(
                        Rect(centerX - radius,
                            centerY - radius,
                            centerX + radius,
                            centerY + radius),
                        startAngle,
                        sweepAngle,
                        false
                    )
                    close()
                }

                val text = "Text $index"

                val textBounds = RectNative()
                val textPaint = androidx.compose.ui.graphics.Paint().asFrameworkPaint()
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.color = textColor.toArgb()
                textPaint.textSize = textSize.toPx()

                textPaint.getTextBounds(text, 0, text.length, textBounds)

                val textX =
                    centerX + (radius * cos(Math.toRadians(startAngle + sweepAngle / 2.toDouble()))).toFloat()
                val textY =
                    centerY + (radius * sin(Math.toRadians(startAngle + sweepAngle / 2.toDouble()))).toFloat()

                val textRotation = startAngle + textOffset

                drawPath(path, color = circleColor)
                drawIntoCanvas {
                    it.nativeCanvas.save()
                    it.nativeCanvas.rotate(textRotation, textX, textY)
                    it.nativeCanvas.drawText(
                        text,
                        textX,
                        textY + textBounds.height() / 2f,
                        textPaint
                    )
                }
            }
        }
    }
}


@Composable
fun DividedCircle() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
        ) {
            val radius = size.minDimension / 2
            val centerX = size.width / 2
            val centerY = size.height / 2
            val angleOffset = -45f // Смещение угла, чтобы начать с вертикальной позиции

            repeat(4) { index ->
                val startAngle = index * 90f + angleOffset
                val sweepAngle = 90f
                val path = Path().apply {
                    moveTo(centerX, centerY)
                    arcTo(
                        Rect(centerX - radius,
                            centerY - radius,
                            centerX + radius,
                            centerY + radius),
                        startAngle,
                        sweepAngle,
                        false
                    )
                    close()
                }
                drawPath(path, color = listColor.random())
                drawIntoCanvas {
                    it.nativeCanvas.save()
                }
            }
        }
    }
}

val listColor = listOf(Color.Gray, Color.Yellow, Color.Blue, Color.Black, Color.Magenta)


@Composable
fun WheelOfFortune(patchRoulette: List<PatchRoulette>) {
    val des = LocalDensity.current
    val conf = LocalConfiguration.current
    var startAngle by remember { mutableStateOf(0f) }
    val sum by remember(patchRoulette) {
        mutableStateOf(patchRoulette.sumOf { it.value.toDouble() }.toFloat())
    }
    val sizePieDp by remember { mutableStateOf(conf.screenWidthDp.dp - 50.dp) }
    Canvas(
        modifier = Modifier
            .size(size = sizePieDp),
    ) {
        val radius = size.minDimension / 2 - 32.dp.toPx()
        val angle = 2 * PI / patchRoulette.size

        patchRoulette.forEachIndexed { index, patchRoulette ->
            val sweepAngle = calculateSweepAngle(patchRoulette.value, sum)
            translate(size.width / 2, size.height / 2) {

                    drawContextRadiantSlice(
                        index = index,
                        patchRoulette= patchRoulette,
                        des = des,
                        radius = radius,
                        angle = startAngle,
                    )

            }
            startAngle += sweepAngle
        }
    }
}

private fun DrawScope.drawContextRadiantSlice(
    index: Int,
    patchRoulette: PatchRoulette,
    des: Density,
    radius: Float,
    angle: Float,
) {
        val offset = radius / 2
        val x1 = cos(angle.toFloat()) * offset
        val x2 = cos(0f) * radius
        val x3 = cos(angle.toFloat()) * radius
        val y1 = sin(angle.toFloat()) * offset
        val y2 = sin(0f) * radius
        val y3 = sin(angle.toFloat()) * radius
        val path = Path().apply {
            moveTo(x1, y1)
            lineTo(x2, y2)
            lineTo(x3, y3)
            close()
        }

        drawPath(path, patchRoulette.color)

        val text = patchRoulette.text
        val textBounds = calculateTextBounds(
            des = des,
            text = text)
        val textWidth = textBounds.width()
        val textHeight = textBounds.height()

        val textX = (radius - textHeight) / 2
        val textY = (textWidth / 2) - (textHeight / 2)

        drawIntoCanvas {
            it.nativeCanvas.drawText(
                text,
                x1 + textX,
                y1 + textY,
                Paint().apply {
                    color = Color.White.toArgb()
                    textSize = 24.sp.toPx()
                    textAlign = Paint.Align.CENTER
                }
            )
            it.nativeCanvas.save()
        }
}


private fun calculateTextBounds(
    des: Density,
    text: String
): RectNative {
    val paint = Paint().apply {
        textSize = with(des) { 24.sp.toPx() }
    }
    val bounds = RectNative()
    paint.getTextBounds(text, 0, text.length, bounds)
    return bounds
}


