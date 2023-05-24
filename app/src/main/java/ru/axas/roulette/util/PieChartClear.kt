package ru.axas.roulette.util

import kotlin.math.PI
import kotlin.math.atan2

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

