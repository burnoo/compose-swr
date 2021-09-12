package dev.burnoo.compose.swr.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private var shouldInitKoin = true

@Composable
fun KoinPreview(content: @Composable () -> Unit) {
    if (shouldInitKoin) {
        initKoin(LocalContext.current)
        shouldInitKoin = false
    }
    content()
}