package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import dev.burnoo.compose.swr.domain.LocalCache
import dev.burnoo.compose.swr.domain.getLocalConfigBlock
import dev.burnoo.compose.swr.model.SWRConfigState
import dev.burnoo.compose.swr.model.config.SWRConfig
import dev.burnoo.compose.swr.model.config.SWRLocalConfigBlockTyped

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