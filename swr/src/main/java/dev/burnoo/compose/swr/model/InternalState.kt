package dev.burnoo.compose.swr.model

import dev.burnoo.compose.swr.domain.now
import kotlinx.datetime.Instant

internal data class InternalState<K, D> internal constructor(
    val key: K,
    val data: D?,
    val error: Throwable?,
    val isValidating: Boolean,
    val revalidationTime: Instant,
    val mutationTime: Instant,
) {

    internal operator fun plus(event: SWREvent<D>): InternalState<K, D> {
        return when (event) {
            is SWREvent.StartValidating -> copy(isValidating = true, revalidationTime = now())
            is SWREvent.Success -> copy(
                data = event.value,
                isValidating = false,
                mutationTime = now()
            )
            is SWREvent.Error -> copy(
                error = event.cause,
                isValidating = false,
                mutationTime = now()
            )
            is SWREvent.Local -> copy(
                data = event.value,
                isValidating = false,
                revalidationTime = now(),
                mutationTime = now()
            )
        }
    }
}