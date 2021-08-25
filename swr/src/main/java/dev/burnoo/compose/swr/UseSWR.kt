package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.burnoo.compose.swr.di.get
import dev.burnoo.compose.swr.domain.SWR
import dev.burnoo.compose.swr.model.RecomposeCoroutineScope
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRConfigBlock
import dev.burnoo.compose.swr.model.SWRState
import kotlinx.coroutines.flow.launchIn

@Composable
fun <K, D> useSWR(
    key: K,
    fetcher: suspend (K) -> D,
    config: SWRConfigBlock<K, D> = {}
): SWRState<D> {
    val swr = get<SWR>()
    val overriddenScope = get<RecomposeCoroutineScope>().value
    val swrConfig = SWRConfig(config)
    swr.initIfNeeded(key, fetcher, swrConfig)
    LaunchedEffect(key) {
        swr.getLocalFlow(key, fetcher, swrConfig)
            .launchIn(overriddenScope ?: this)
    }
    val globalStateFlow = swr.getGlobalFlow<Any, D>(key as Any)
    return SWRState(
        stateFlow = globalStateFlow,
        initialValue = swrConfig.initialData,
        config = swrConfig
    )
}