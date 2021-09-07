package dev.burnoo.compose.swr.state

import androidx.compose.runtime.Composable
import dev.burnoo.compose.swr.internal.SWR
import dev.burnoo.compose.swr.cache.SWRCache
import dev.burnoo.compose.swr.config.SWRConfig

class SWRConfigState<K, D> @PublishedApi internal constructor(
    _config: SWRConfig<K, D>,
    _cache: SWRCache
) {

    // Needed to suppress destructing warning
    val config = _config
    val cache = _cache

    suspend fun mutate(key: K, data: D? = null, shouldRevalidate: Boolean = true) {
        SWR(key, config, cache).mutate(data, shouldRevalidate)
    }

    @Composable
    operator fun component1() = cache

    @Composable
    operator fun component2(): suspend (key: K, data: D?, shouldRevalidate: Boolean) -> Unit =
        ::mutate

    @Composable
    operator fun component3(): SWRConfig<K, D> = config
}