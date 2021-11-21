package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import dev.burnoo.compose.swr.internal.LocalCache
import dev.burnoo.compose.swr.internal.getLocalConfigBlock
import dev.burnoo.compose.swr.state.SWRConfigState
import dev.burnoo.compose.swr.config.SWRConfig
import dev.burnoo.compose.swr.config.SWRLocalConfigBlockTyped

@Suppress("UNCHECKED_CAST")
@Composable
inline fun <reified K, reified D> useSWRConfig(): SWRConfigState<K, D> {
    @Suppress("LocalVariableName")
    val LocalConfigBlock = getLocalConfigBlock<K>()
    val currentLocalConfigBlock = LocalConfigBlock.current as SWRLocalConfigBlockTyped<K, D>
    val config = SWRConfig(currentLocalConfigBlock)
    val cache = LocalCache.current
    return SWRConfigState(config, cache)
}