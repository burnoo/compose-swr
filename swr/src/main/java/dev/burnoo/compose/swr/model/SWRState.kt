package dev.burnoo.compose.swr.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import dev.burnoo.compose.swr.domain.SWR
import dev.burnoo.compose.swr.domain.SWRCache
import dev.burnoo.compose.swr.model.internal.InternalState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map

@Suppress("UNCHECKED_CAST", "FunctionName")
internal fun <K, D> SWRState(
    stateFlow: StateFlow<InternalState<K, D>>,
    config: SWRConfig<K, D>,
    cache: SWRCache
): SWRState<D> {
    return SWRState(
        stateFlow as StateFlow<InternalState<Any, D>>,
        config as SWRConfig<Any, D>,
        cache
    )
}

class SWRState<D> internal constructor(
    private val stateFlow: StateFlow<InternalState<Any, D>>,
    private val config: SWRConfig<Any, D>,
    cache: SWRCache
) {

    private val swr = SWR(stateFlow.value.key, config, cache)

    @Composable
    operator fun component1() = data

    @Composable
    operator fun component2() = error

    @Composable
    operator fun component3() = isValidating

    operator fun component4(): suspend (data: D?, shouldRevalidate: Boolean) -> Unit =
        this::mutate

    val data: D?
        @Composable
        get() = stateFlow
            .map { it.data }
            .run { if (config.fallbackData != null) drop(1) else this }
            .collectAsState(initial = config.fallbackData ?: stateFlow.value.data)
            .value

    val error: Throwable?
        @Composable
        get() = stateFlow
            .map { it.error }
            .collectAsState(initial = stateFlow.value.error)
            .value

    val isValidating: Boolean
        @Composable
        get() = stateFlow
            .map { it.isValidating }
            .drop(1)
            .collectAsState(initial = config.revalidateOnMount != false)
            .value

    suspend fun mutate(data: D? = null, shouldRevalidate: Boolean = true) {
        swr.mutate(data, shouldRevalidate)
    }
}