package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import dev.burnoo.compose.swr.domain.LocalCache
import dev.burnoo.compose.swr.domain.getConfigBlockComposition
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRConfigState

@Composable
inline fun <reified K, reified D> useSWRConfig(): SWRConfigState<K, D> {
    val localConfigBlock = getConfigBlockComposition<K, D>()
    val config = SWRConfig(localConfigBlock.current)
    val cache = LocalCache.current
    return SWRConfigState(config, cache)
}