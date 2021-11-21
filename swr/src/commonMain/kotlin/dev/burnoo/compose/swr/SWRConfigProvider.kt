package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.burnoo.compose.swr.internal.LocalCache
import dev.burnoo.compose.swr.internal.getLocalConfigBlock
import dev.burnoo.compose.swr.config.SWRConfig
import dev.burnoo.compose.swr.config.SWRLocalConfigBlock
import dev.burnoo.compose.swr.config.plus

@Composable
inline fun <reified K> SWRConfigProvider(
    noinline value: SWRLocalConfigBlock<K>,
    noinline content: @Composable () -> Unit
) {
    @Suppress("LocalVariableName")
    val LocalConfigBlock = getLocalConfigBlock<K>()
    val configBlock = LocalConfigBlock.current + value
    CompositionLocalProvider(
        LocalConfigBlock provides configBlock,
        LocalCache provides SWRConfig(configBlock).provider()
    ) {
        content()
    }
}