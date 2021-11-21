package dev.burnoo.compose.swr.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.burnoo.compose.swr.internal.getLocalPreviewState
import dev.burnoo.compose.swr.state.StaticSWRState

@Composable
inline fun <reified D> SWRPreview(
    data: D? = null,
    error: Throwable? = null,
    noinline content: @Composable () -> Unit
) {
    @Suppress("LocalVariableName")
    val LocalPreviewState = getLocalPreviewState<D>()
    CompositionLocalProvider(LocalPreviewState provides StaticSWRState(data, error)) {
        content()
    }
}