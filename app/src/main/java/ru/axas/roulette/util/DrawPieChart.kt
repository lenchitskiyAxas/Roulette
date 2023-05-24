package ru.axas.roulette.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PathMeasure
import android.graphics.PointF
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
import ru.axas.roulette.models.PatchRoulette

class DrawPieChart() {
    fun getPieChart(
        dataPatch: List<PatchRoulette>,
        size: Int,
    ): BitmapPainter {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val radiusCircle = size / 2f
        val sum =dataPatch.sumOf { it.value.toDouble() }.toFloat()
        var startAngle =-0f

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

            val paint = Paint()
            paint.color = item.color.toArgb()

            canvas.drawPath(trianglePath.asAndroidPath(), paint)
            val vertices = getTriangleVertices(trianglePath)
            if (vertices.size != 3) return  BitmapPainter(bitmap.asImageBitmap())
            val medianXCurrent = (vertices[1].x + vertices[2].x) / 2
            val medianYCurrent = (vertices[1].y + vertices[2].y) / 2
            Log.i("pieChartTriangle", "$medianXCurrent")
            val textSize = radiusCircle * 0.1f

            canvas.drawTextOnLineApp(
                text = item.text,
                textColor = Color.Black,
                textSize = textSize,
                moveTo = Offset(medianXCurrent, medianYCurrent),
                lineTo = Offset(vertices[0].x, vertices[0].y),
                offsetX = 0f,
                offsetY = textSize / 2,
            )


            startAngle += sweepAngle
        }
      return  BitmapPainter(bitmap.asImageBitmap())
    }

   private fun Canvas.drawTextOnLineApp(
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

        this.drawTextOnPath(
            text,
            textPath.asAndroidPath(),
            offsetX,
            offsetY,
            paint
        )
    }

    private  fun calculateSweepAngle(value: Float, totalValue: Float): Float {
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

