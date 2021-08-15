package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import dev.burnoo.compose.swr.di.get
import dev.burnoo.compose.swr.domain.SWR
import dev.burnoo.compose.swr.model.RecomposeCoroutineScope
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRConfigBlock
import dev.burnoo.compose.swr.model.SWRState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn

@Composable
fun <K, D> useSWR(
    key: K,
    fetcher: suspend (K) -> D,
    config: SWRConfigBlock<K, D> = {}
): State<SWRState<D>> {
    val swr = get<SWR>()
    val scope = get<RecomposeCoroutineScope>().value
    val swrConfig = SWRConfig(config)
    swr.initIfNeeded(key, fetcher, swrConfig)
    LaunchedEffect(key) {
        swr.getLocalFlow(key, fetcher, swrConfig)
            .run { if (scope != null) flowOn(scope.coroutineContext) else this }
            .collect()
    }
    return swr.getGlobalFlow<K, D>(key).collectAsState(initial = swr.getInitialState(key, config))
}