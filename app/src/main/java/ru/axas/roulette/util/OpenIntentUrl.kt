package ru.axas.roulette.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler


@Composable
fun rememberOpenIntentUrl(onError: (String) -> Unit = {}): (intentName:String?, uri:String) -> Unit {
    val uriHandler = LocalUriHandler.current
    return { intentName, uri ->
        try {
            uriHandler.openUri(intentName.orEmpty() + uri)
        } catch (e: Exception) {
            e.printStackTrace()
            onError.invoke(uri)
        }
    }
}