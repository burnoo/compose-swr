package dev.burnoo.compose.swr.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import dev.burnoo.compose.swr.model.internal.InternalState
import dev.burnoo.compose.swr.mutate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map

class SWRState<D> internal constructor(
    private val stateFlow: StateFlow<InternalState<*, D>>,
    private val config: SWRConfig<*, D>,
) {

    @Composable
    operator fun component1() = data

    @Composable
    operator fun component2() = error

    @Composable
    operator fun component3() = isValidating

    operator fun component4(): suspend (data: Any?, shouldRevalidate: Boolean) -> Unit =
        this::mutate

    val data: D?
        @Composable
        get() = stateFlow
            .map { it.data }
            .run { if (config.initialData != null) drop(1) else this }
            .collectAsState(initial = config.initialData ?: stateFlow.value.data)
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

    suspend fun mutate(data: Any? = null, shouldRevalidate: Boolean = true) {
        mutate(stateFlow.value.key, data, shouldRevalidate)
    }
}