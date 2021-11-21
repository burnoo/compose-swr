package dev.burnoo.compose.swr.state

import androidx.compose.runtime.Composable
import dev.burnoo.compose.swr.cache.SWRCache
import dev.burnoo.compose.swr.config.SWRConfig
import dev.burnoo.compose.swr.internal.model.InternalState
import kotlinx.coroutines.flow.StateFlow

@Suppress("UNCHECKED_CAST", "FunctionName")
internal fun <K, D> SWRState(
    stateFlow: StateFlow<InternalState<K, D>>,
    config: SWRConfig<K, D>,
    cache: SWRCache
): SWRState<D> {
    return SWRStateImpl(
        stateFlow as StateFlow<InternalState<Any, D>>,
        config as SWRConfig<Any, D>,
        cache
    )
}

interface SWRState<D> {
    @Composable
    operator fun component1(): D? = data

    @Composable
    operator fun component2(): Throwable? = error

    @Composable
    operator fun component3() = isValidating

    operator fun component4(): suspend (data: D?, shouldRevalidate: Boolean) -> Unit =
        this::mutate

    val data: D?
        @Composable
        get

    val error: Throwable?
        @Composable
        get

    val isValidating: Boolean
        @Composable
        get

    suspend fun mutate(data: D? = null, shouldRevalidate: Boolean = true)
}