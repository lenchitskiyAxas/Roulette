package ru.axas.roulette.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PathMeasure
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
import ru.axas.roulette.models.PatchRoulette

class DrawPieChart(
    private val context: Context,
    private val dataPatch: List<PatchRoulette>,
    private val size: Int,
) {

    fun getPieChart(): BitmapPainter {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val radiusCircle = size / 2f
        val sum = dataPatch.sumOf { it.value.toDouble() }.toFloat()
        var startAngle = -0f

        dataPatch.forEach { item ->
            val sweepAngle = calculateSweepAngle(item.value, sum)

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

            val vertices = getTriangleVertices(trianglePath)
            if (vertices.size != 3) return BitmapPainter(bitmap.asImageBitmap())
            val medianX = (vertices[1].x + vertices[2].x + vertices[0].x) / 3
            val medianY = (vertices[1].y + vertices[2].y + vertices[0].y) / 3
            val medianXCurrent = (vertices[1].x + vertices[2].x) / 2
            val medianYCurrent = (vertices[1].y + vertices[2].y) / 2

            val paint = Paint()
            paint.shader = LinearGradientShader(
                from = Offset(medianXCurrent, medianYCurrent),
                to = Offset(vertices[0].x, vertices[0].y),
                colors = item.colors,
            )

            val drawableSize = size / 20
            val drawable = context.getDrawable(item.drawableRes)
            val bitmapDrawable = (drawable as BitmapDrawable).bitmap
            val scaledBitmap =
                Bitmap.createScaledBitmap(bitmapDrawable, drawableSize, drawableSize, true)

            val matrix = Matrix()
            matrix.postRotate(10f)

            val rotatedBitmap = Bitmap.createBitmap(
                scaledBitmap,
                0,
                0,
                scaledBitmap.width,
                scaledBitmap.height,
                matrix,
                true
            )

            val xDrawableCoordinates = ((medianX + medianXCurrent) /2) - (drawableSize * 2)
            val yDrawableCoordinates = ((medianY + medianYCurrent)/2) - drawableSize * 2
            canvas.drawPath(trianglePath.asAndroidPath(), paint)
            canvas.drawBitmap(rotatedBitmap, xDrawableCoordinates, yDrawableCoordinates, null)
            startAngle += sweepAngle
        }
        return BitmapPainter(bitmap.asImageBitmap())
    }


    fun getPieChartText(): BitmapPainter {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val radiusCircle = size / 2f
        val sum = dataPatch.sumOf { it.value.toDouble() }.toFloat()
        var startAngle = -0f

        dataPatch.forEach { item ->
            val sweepAngle = calculateSweepAngle(item.value, sum)

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

            val vertices = getTriangleVertices(trianglePath)
            if (vertices.size != 3) return BitmapPainter(bitmap.asImageBitmap())
            val medianXCurrent = (vertices[1].x + vertices[2].x) / 2
            val medianYCurrent = (vertices[1].y + vertices[2].y) / 2
            val textSize = radiusCircle * 0.1f

            val paint = Paint()
            paint.shader = LinearGradientShader(
                from = Offset(medianXCurrent, medianYCurrent),
                to = Offset(vertices[0].x, vertices[0].y),
                colors = item.colors,
            )

            canvas.drawPath(trianglePath.asAndroidPath(), paint)
            canvas.drawTextOnLineApp(
                text = item.text,
                textColor = Color.Black,
                textSize = textSize,
                from = Offset(medianXCurrent, medianYCurrent),
                to = Offset(vertices[0].x, vertices[0].y),
                offsetX = 0f,
                offsetY = textSize / 2,
            )
            startAngle += sweepAngle
        }
        return BitmapPainter(bitmap.asImageBitmap())
    }

    private fun Canvas.drawTextOnLineApp(
        text: String,
        textColor: Color,
        textSize: Float,
        from: Offset,
        to: Offset,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
    ) {
        val paint = Paint()
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = textSize
        paint.color = textColor.toArgb()

        val textPath = Path()
        textPath.moveTo(from.x, from.y)
        textPath.lineTo(to.x, to.y)

        this.drawTextOnPath(
            text,
            textPath.asAndroidPath(),
            offsetX,
            offsetY,
            paint
        )
    }

    private fun calculateSweepAngle(value: Float, totalValue: Float): Float {
        return 360 * (value / totalValue)
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
}

