package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.burnoo.compose.swr.domain.getConfigBlockComposition
import dev.burnoo.compose.swr.model.SWRConfigBlock
import dev.burnoo.compose.swr.model.plus

@Composable
inline fun <reified K, reified D> SWRConfigProvider(
    noinline value: SWRConfigBlock<K, D>,
    noinline content: @Composable () -> Unit
) {
    val localConfigBlock = getConfigBlockComposition<K, D>()
    val currentConfigBlock = localConfigBlock.current
    CompositionLocalProvider(localConfigBlock provides (currentConfigBlock + value)) {
        content()
    }
}