package ru.axas.roulette.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import ru.axas.roulette.models.patchRoulette
import ru.axas.roulette.util.PieChartCanvas

@Composable
fun ForthScreen(){
    val conf = LocalConfiguration.current
    val sizePieDp by remember { mutableStateOf(conf.screenWidthDp.dp - 30.dp) }
    Box(modifier = Modifier.fillMaxSize()){
        PieChartCanvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(sizePieDp),
            dataPatch = patchRoulette
        )
    }
}