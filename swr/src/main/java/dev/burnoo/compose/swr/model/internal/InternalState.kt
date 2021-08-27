package dev.burnoo.compose.swr.model.internal

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

    internal operator fun plus(event: Event<D>): InternalState<K, D> {
        return when (event) {
            is Event.StartValidating -> copy(isValidating = true, revalidationTime = now())
            is Event.Success -> copy(
                data = event.value,
                isValidating = false,
                mutationTime = now()
            )
            is Event.Error -> copy(
                error = event.cause,
                isValidating = false,
                mutationTime = now()
            )
            is Event.Local -> copy(
                data = event.value,
                isValidating = false,
                revalidationTime = now(),
                mutationTime = now()
            )
        }
    }
}