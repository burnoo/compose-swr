package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import dev.burnoo.compose.swr.di.get
import dev.burnoo.compose.swr.domain.SWR
import dev.burnoo.compose.swr.model.RecomposeCoroutineScope
import dev.burnoo.compose.swr.model.SWRConfigBlock
import dev.burnoo.compose.swr.model.SWRResult
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn

@Composable
fun <K, D> useSWR(
    key: K,
    fetcher: suspend (K) -> D,
    config: SWRConfigBlock<K, D> = {}
): State<SWRResult<D>> {
    val swr = get<SWR>()
    val scope = get<RecomposeCoroutineScope>().value
    swr.initIfNeeded(key, fetcher)
    LaunchedEffect(key) {
        swr.getLocalFlow(key, fetcher, config)
            .run { if (scope != null) flowOn(scope.coroutineContext) else this }
            .collect()
    }
    return swr.getGlobalFlow<K, D>(key).collectAsState(initial = swr.getInitialResult(config))
}