package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.burnoo.compose.swr.domain.LocalCache
import dev.burnoo.compose.swr.domain.getLocalConfigBlock
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRConfigBlock
import dev.burnoo.compose.swr.model.plus

@Composable
inline fun <reified K> SWRConfigProvider(
    noinline value: SWRConfigBlock<K, Any>,
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