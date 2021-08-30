package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.burnoo.compose.swr.di.get
import dev.burnoo.compose.swr.domain.SWR
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRConfigBlock
import dev.burnoo.compose.swr.model.SWRState
import dev.burnoo.compose.swr.model.plus
import kotlinx.coroutines.flow.launchIn

@Composable
fun <K, D> useSWR(
    key: K,
    fetcher: suspend (K) -> D,
    config: SWRConfigBlock<K, D> = {}
): SWRState<D> {
    val swr = get<SWR>()
    val swrConfig = SWRConfig(config + { this.fetcher = fetcher })
    swr.initIfNeeded(key, fetcher, swrConfig)
    LaunchedEffect(key) {
        swr.getLocalFlow(key, fetcher, swrConfig)
            .launchIn(swrConfig.scope ?: this)
    }
    return SWRState(stateFlow = swr.getGlobalFlow(key), config = swrConfig)
}

@Composable
fun <K, D> useSWR(
    key: K,
    config: SWRConfigBlock<K, D>
): SWRState<D> {
    val fetcher = SWRConfig(config).fetcher
    return useSWR(key, fetcher, config)
}