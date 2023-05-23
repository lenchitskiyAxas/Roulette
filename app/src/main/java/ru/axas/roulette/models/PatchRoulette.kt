package ru.axas.roulette.models

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

data class PatchRoulette(
    val value: Float,
    val text: String,
    val color: Color,
   @DrawableRes val drawableRes: Int
)
