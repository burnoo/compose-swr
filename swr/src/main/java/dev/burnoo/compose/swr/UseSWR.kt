package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.burnoo.compose.swr.di.get
import dev.burnoo.compose.swr.domain.SWR
import dev.burnoo.compose.swr.domain.getConfigBlockComposition
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRConfigBlock
import dev.burnoo.compose.swr.model.SWRState
import dev.burnoo.compose.swr.model.plus
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
    val globalConfigBody = getConfigBlockComposition<K, D>().current
    val swrConfig = globalConfigBody + config
    return useSWRInternal(key, swrConfig)
}

@PublishedApi
@Composable
internal fun <K, D> useSWRInternal(
    key: K,
    configBlock: SWRConfigBlock<K, D>
): SWRState<D> {
    val swr = get<SWR>()
    val config = SWRConfig(block = configBlock)
    swr.initIfNeeded(key, config)
    LaunchedEffect(key) {
        swr.getLocalFlow(key, config)
            .launchIn(config.scope ?: this)
    }
    return SWRState(stateFlow = swr.getGlobalFlow(key), config = config)
}

