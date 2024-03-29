package ru.axas.roulette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.core.view.WindowCompat
import ru.axas.roulette.models.RouletteData
import ru.axas.roulette.models.patchRoulette
import ru.axas.roulette.screen.MainScreen
import ru.axas.roulette.ui.theme.RouletteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val density = LocalDensity.current
            val configuration = LocalConfiguration.current
            val dataRoulette by remember {
                mutableStateOf(RouletteData(
                    patches = patchRoulette,
                    context = applicationContext,
                    arrow = getDrawable(R.drawable.image_arrow_roulette),
                    background =  getDrawable(R.drawable.image_background_roulette),
                    center =  getDrawable(R.drawable.image_center_roulette),
                    configurationScreen = configuration,
                    desScreen = density,
                ))
            }

            RouletteTheme {
                MainScreen(rouletteData = dataRoulette)
            }
        }
    }
}
