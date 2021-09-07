package dev.burnoo.compose.swr.internal.model

import dev.burnoo.compose.swr.internal.testable.now
import kotlinx.datetime.Instant

internal data class InternalState<K, D> internal constructor(
    val key: K,
    val data: D?,
    val error: Throwable?,
    val isValidating: Boolean,
    val revalidationTime: Instant,
    val mutationTime: Instant,
) {

    operator fun plus(event: Event<D>): InternalState<K, D> {
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

    companion object {
        fun <K, D> initial(key: K) = InternalState<K, D>(
            key = key,
            data = null,
            error = null,
            isValidating = false,
            revalidationTime = Instant.DISTANT_PAST,
            mutationTime = Instant.DISTANT_PAST
        )
    }
}