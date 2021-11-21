package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.burnoo.compose.swr.config.SWRConfig
import dev.burnoo.compose.swr.config.SWRConfigBlock
import dev.burnoo.compose.swr.config.plus
import dev.burnoo.compose.swr.config.withConfigBlock
import dev.burnoo.compose.swr.internal.LocalCache
import dev.burnoo.compose.swr.internal.SWR
import dev.burnoo.compose.swr.internal.getLocalConfigBlock
import dev.burnoo.compose.swr.internal.getLocalPreviewState
import dev.burnoo.compose.swr.state.SWRState
import dev.burnoo.compose.swr.state.StaticSWRState
import kotlinx.coroutines.flow.launchIn

@Composable
inline fun <reified K, reified D> useSWR(
    getKey: () -> K?,
    noinline fetcher: (suspend (K) -> D)? = null,
    noinline config: SWRConfigBlock<K, D> = {}
): SWRState<D> {
    return useSWR(runCatching(getKey).getOrNull(), fetcher, config)
}

@Composable
inline fun <reified K, reified D> useSWR(
    key: K?,
    noinline fetcher: (suspend (K) -> D)? = null,
    noinline config: SWRConfigBlock<K, D> = {}
): SWRState<D> {
    getLocalPreviewState<D>().current?.let { return it }
    if (key == null) return StaticSWRState()
    @Suppress("LocalVariableName")
    val LocalConfigBlock = getLocalConfigBlock<K>()
    val configWithFetcher = if (fetcher == null) config else config + { this.fetcher = fetcher }
    val configBlock = LocalConfigBlock.current.withConfigBlock(configWithFetcher)
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

