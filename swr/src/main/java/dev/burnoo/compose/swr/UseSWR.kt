package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.burnoo.compose.swr.domain.LocalCache
import dev.burnoo.compose.swr.domain.SWR
import dev.burnoo.compose.swr.domain.getCurrentConfigBlock
import dev.burnoo.compose.swr.model.SWRState
import dev.burnoo.compose.swr.model.config.SWRConfig
import dev.burnoo.compose.swr.model.config.SWRConfigBlock
import dev.burnoo.compose.swr.model.config.plus
import kotlinx.coroutines.flow.launchIn

@Composable
inline fun <reified K, reified D> useSWR(
    key: K,
    noinline fetcher: suspend (K) -> D,
    noinline config: SWRConfigBlock<K, D> = {}
): SWRState<D> {
    return useSWR(key, config + { this.fetcher = fetcher })
}

@Composable
inline fun <reified K, reified D> useSWR(
    key: K,
    noinline config: SWRConfigBlock<K, D> = {}
): SWRState<D> {
    val currentConfigBlock = getCurrentConfigBlock<K, D>()
    val configBlock = currentConfigBlock + config
    val swrConfig = SWRConfig(configBlock)
    return useSWRInternal(key, swrConfig)
}

@PublishedApi
@Composable
internal fun <K, D> useSWRInternal(
    key: K,
    config: SWRConfig<K, D>
): SWRState<D> {
    val cache = LocalCache.current
    val swr = SWR(key, config, cache)
    LaunchedEffect(key) {
        swr.getLocalFlow().launchIn(config.scope ?: this)
    }
    return SWRState(swr.getGlobalFlow(), config, cache)
}

