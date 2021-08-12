package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import dev.burnoo.compose.swr.di.get
import dev.burnoo.compose.swr.domain.SWR
import dev.burnoo.compose.swr.model.RecomposeCoroutineScope
import dev.burnoo.compose.swr.model.SWRConfigBlock
import dev.burnoo.compose.swr.model.SWRResult
import kotlinx.coroutines.flow.flowOn

@Composable
fun <K, D> useSWR(
    key: K,
    fetcher: suspend (K) -> D,
    config: SWRConfigBlock<K, D> = {}
): State<SWRResult<D>> {
    val swr = get<SWR>()
    val scope = get<RecomposeCoroutineScope>().value ?: rememberCoroutineScope()
    return swr.getFlow(key, fetcher, config)
        .flowOn(scope.coroutineContext)
        .collectAsState(initial = swr.getInitialResult(config))
}