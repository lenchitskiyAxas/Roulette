package ru.axas.roulette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ru.axas.roulette.models.patchRoulette
import ru.axas.roulette.screen.FirstScreen
import ru.axas.roulette.screen.SecondScreen
import ru.axas.roulette.ui.theme.RouletteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RouletteTheme {
//                FirstScreen()
                SecondScreen(patchRoulette)
            }
        }
    }
}
