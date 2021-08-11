package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import dev.burnoo.compose.swr.di.get
import dev.burnoo.compose.swr.domain.SWR
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRConfigBlock
import dev.burnoo.compose.swr.model.SWRResult

@Composable
fun <K, D> useSWR(
    key: K,
    fetcher: suspend (K) -> D,
    config: SWRConfigBlock<K, D> = {}
): State<SWRResult<D>> {
    val swr = get<SWR>()
    val swrConfig = SWRConfig(block = config)
    swr.init(key, fetcher, swrConfig)
    LaunchedEffect(key) {
        swr.launch(key, swrConfig.refreshInterval, this)
    }
    return swr.getStateFlow<K, D>(key).collectAsState()
}

