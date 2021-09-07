package dev.burnoo.compose.swr.model.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import dev.burnoo.compose.swr.domain.SWR
import dev.burnoo.compose.swr.domain.SWRCache
import dev.burnoo.compose.swr.model.config.SWRConfig
import dev.burnoo.compose.swr.model.internal.InternalState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map

internal class SWRStateImpl<D> internal constructor(
    private val stateFlow: StateFlow<InternalState<Any, D>>,
    private val config: SWRConfig<Any, D>,
    cache: SWRCache
) : SWRState<D> {

    private val swr = SWR(stateFlow.value.key, config, cache)

    override val data: D?
        @Composable
        get() = stateFlow
            .map { it.data }
            .run { if (config.getFallback(stateFlow.value.key) != null) drop(1) else this }
            .collectAsState(
                initial = config.getFallback(stateFlow.value.key) ?: stateFlow.value.data
            )
            .value

    override val error: Throwable?
        @Composable
        get() = stateFlow
            .map { it.error }
            .collectAsState(initial = stateFlow.value.error)
            .value

    override val isValidating: Boolean
        @Composable
        get() = stateFlow
            .map { it.isValidating }
            .drop(1)
            .collectAsState(initial = config.revalidateOnMount != false)
            .value

    override suspend fun mutate(data: D?, shouldRevalidate: Boolean) {
        swr.mutate(data, shouldRevalidate)
    }
}