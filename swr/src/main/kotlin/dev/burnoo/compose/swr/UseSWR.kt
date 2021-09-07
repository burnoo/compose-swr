package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.burnoo.compose.swr.domain.LocalCache
import dev.burnoo.compose.swr.domain.SWR
import dev.burnoo.compose.swr.domain.getLocalConfigBlock
import dev.burnoo.compose.swr.model.config.SWRConfig
import dev.burnoo.compose.swr.model.config.SWRConfigBlock
import dev.burnoo.compose.swr.model.config.plus
import dev.burnoo.compose.swr.model.config.withConfigBlock
import dev.burnoo.compose.swr.model.state.EmptySWRState
import dev.burnoo.compose.swr.model.state.SWRState
import kotlinx.coroutines.flow.launchIn

@Composable
inline fun <reified K, reified D> useSWR(
    getKey: () -> K?,
    noinline fetcher: suspend (K) -> D,
    noinline config: SWRConfigBlock<K, D> = {}
): SWRState<D> {
    return useSWR(runCatching(getKey).getOrNull(), fetcher, config)
}

@Composable
inline fun <reified K, reified D> useSWR(
    key: K?,
    noinline fetcher: suspend (K) -> D,
    noinline config: SWRConfigBlock<K, D> = {}
): SWRState<D> {
    return useSWR(key, config + { this.fetcher = fetcher })
}

@Composable
inline fun <reified K, reified D> useSWR(
    getKey: () -> K?,
    noinline config: SWRConfigBlock<K, D> = {}
): SWRState<D> {
    return useSWR(runCatching(getKey).getOrNull(), config)
}

@Composable
inline fun <reified K, reified D> useSWR(
    key: K?,
    noinline config: SWRConfigBlock<K, D> = {}
): SWRState<D> {
    if (key == null) return EmptySWRState()
    @Suppress("LocalVariableName")
    val LocalConfigBlock = getLocalConfigBlock<K>()
    val configBlock = LocalConfigBlock.current.withConfigBlock(config)
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

