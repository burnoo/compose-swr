package dev.burnoo.compose.swr.model

import dev.burnoo.compose.swr.mutate

private typealias Mutate<T> = suspend (data: T?, shouldRevalidate: Boolean) -> Unit

sealed class SWRState<T>(private val mutate: Mutate<T>) {

    class Loading<T> internal constructor(mutate: Mutate<T>) : SWRState<T>(mutate)

    class Error<T> internal constructor(
        val exception: Throwable,
        mutate: Mutate<T>
    ) : SWRState<T>(mutate)

    class Success<T> internal constructor(
        val data: T,
        mutate: Mutate<T>
    ) : SWRState<T>(mutate) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Success<*>
            if (data != other.data) return false
            return true
        }

        override fun hashCode(): Int {
            return data?.hashCode() ?: 0
        }
    }

    operator fun component1(): T? = if (this is Success) data else null

    operator fun component2() = if (this is Error) exception else null

    operator fun component3(): Mutate<T> = mutate

    fun requireData() = (this as Success<T>).data

    fun requireException() = (this as Error).exception

    suspend fun mutate(data: T? = null, shouldRevalidate: Boolean = true) =
        mutate.invoke(data, shouldRevalidate)

    companion object {
        private fun <K, D> getMutate(key: K): Mutate<D> = { newData, shouldRevalidate ->
            mutate(key, newData, shouldRevalidate)
        }

        fun <K, D> fromData(key: K, data: D?): SWRState<D> {
            return if (data == null) Loading(getMutate(key)) else Success(data, getMutate(key))
        }

        fun <K, D> fromError(key: K, error: Throwable) =
            Error<D>(error, getMutate(key))
    }
}