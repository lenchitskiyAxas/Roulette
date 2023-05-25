package ru.axas.roulette.models

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import ru.axas.roulette.util.DrawPieChart
import kotlin.math.floor
import kotlin.math.roundToInt

data class PatchRoulette(
    val value: Float,
    val text: String,
    val colors: List<Color>,
    @DrawableRes val drawableRes: Int
)

data class RouletteData(
    val patches: List<PatchRoulette>,
    val arrow: Painter?,
    val roulette: Painter?,
    val background: Painter?,
    val center: Painter?,
) {

    constructor(
        patches: List<PatchRoulette>,
        configurationScreen: Configuration,
        desScreen: Density,
        context: Context,
        arrow: Drawable?,
        background: Drawable?,
        center: Drawable?,
    ) : this(
        patches = patches,
        roulette = getBitmapRoulette(
            patches = patches,
            context = context,
            configurationScreen = configurationScreen,
            desScreen = desScreen),
        arrow = getBitmapArrow(
            configurationScreen =configurationScreen,
            arrow =arrow,
            desScreen =desScreen
        ),
        background = getBitmapBackground(
            configurationScreen =configurationScreen,
            background =background,
            desScreen =desScreen
        ),
        center = getBitmapCenter(
            configurationScreen =configurationScreen,
            center =center,
            desScreen =desScreen
        ),
    )

    companion object {
        private fun getBitmapRoulette(
            patches: List<PatchRoulette>,
            context: Context,
            configurationScreen: Configuration,
            desScreen: Density
        ): BitmapPainter {
            val size = with(desScreen) { (configurationScreen.screenWidthDp.dp- 30.dp).toPx().toInt() }
            return DrawPieChart(
                context = context,
                dataPatch = patches,
                size = size).getPieChart()
        }

        private fun getBitmapArrow(
            configurationScreen: Configuration,
            arrow: Drawable?,
            desScreen: Density,
        ): BitmapPainter? {
            arrow ?: return null
            val size = with(desScreen) { (configurationScreen.screenWidthDp.dp).toPx().toInt() }
            return BitmapPainter(arrow.toBitmap(size, size, null).asImageBitmap())
        }
        private fun getBitmapBackground(
            configurationScreen: Configuration,
            background: Drawable?,
            desScreen: Density,
        ): BitmapPainter? {
            background ?: return null
            val size = with(desScreen) { (configurationScreen.screenWidthDp.dp).toPx().toInt() }
            return BitmapPainter(background.toBitmap(size, size, null).asImageBitmap())
        }

        private fun getBitmapCenter(
            configurationScreen: Configuration,
            center: Drawable?,
            desScreen: Density,
        ): BitmapPainter? {
            center ?: return null
            val size = with(desScreen) { (configurationScreen.screenWidthDp.dp).toPx().toInt() }
            return BitmapPainter(center.toBitmap(size, size, null).asImageBitmap())
        }
    }

    fun getChoosePatch2(roulette:Float):PatchRoulette?{
        val totalWeight = this.patches.sumOf { it.value.toDouble() }.toFloat()
        val startingPosition = 0f // replace with your own starting position calculation
        val currentPosition = roulette % 360f / 360f * totalWeight

        var sum = 0f
        this.patches.forEach { patch ->
            sum += patch.value
            if (sum > startingPosition + currentPosition) {
                Log.i("getChoosePatch", "\n" +
                        "roulette: $roulette\n" +
                        "totalWeight: $totalWeight\n" +
                        "startingPosition: $startingPosition\n" +
                        "currentPosition: $currentPosition\n" +
                        "sum: $sum\n" +
                        "\n" +
                        "\n")
                return patch
            }
        }

        return null
    }

    fun getChoosePatch(roulette:Float):PatchRoulette?{
        val sizePatches = this.patches.size
        val currentIndex = (360f - (roulette % 360f) +270f ) /(360f / sizePatches)
        val index = ((floor(currentIndex).roundToInt()) % sizePatches)
        return this.patches.getOrNull(index)
    }
}
