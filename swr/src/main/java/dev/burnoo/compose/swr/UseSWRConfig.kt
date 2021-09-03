package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import dev.burnoo.compose.swr.domain.LocalCache
import dev.burnoo.compose.swr.domain.getCurrentConfigBlock
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRConfigState

@Suppress("UNCHECKED_CAST")
@Composable
inline fun <reified K, reified D> useSWRConfig(): SWRConfigState<K, D> {
    val currentConfigBlock = getCurrentConfigBlock<K, D>()
    val config = SWRConfig(currentConfigBlock)
    val cache = LocalCache.current
    return SWRConfigState(config, cache)
}