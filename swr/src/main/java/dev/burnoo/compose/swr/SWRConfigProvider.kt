package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.burnoo.compose.swr.domain.LocalCache
import dev.burnoo.compose.swr.domain.getLocalConfigBlock
import dev.burnoo.compose.swr.model.config.SWRConfig
import dev.burnoo.compose.swr.model.config.SWRProviderConfigBlock
import dev.burnoo.compose.swr.model.config.plusProviderConfigBlock

@Composable
inline fun <reified K> SWRConfigProvider(
    noinline value: SWRProviderConfigBlock<K>,
    noinline content: @Composable () -> Unit
) {
    @Suppress("LocalVariableName")
    val LocalConfigBlock = getLocalConfigBlock<K>()
    val configBlock = LocalConfigBlock.current.plusProviderConfigBlock(value)
    CompositionLocalProvider(
        LocalConfigBlock provides configBlock,
        LocalCache provides SWRConfig(configBlock).provider()
    ) {
        content()
    }
}