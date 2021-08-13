package dev.burnoo.compose.swr.model

import dev.burnoo.compose.swr.mutate

sealed class SWRResult<out T>(private val mutate: suspend () -> Unit) {

    class Loading internal constructor(mutate: suspend () -> Unit) : SWRResult<Nothing>(mutate)

    class Error internal constructor(
        val exception: Throwable,
        mutate: suspend () -> Unit
    ) : SWRResult<Nothing>(mutate)

    class Success<T> internal constructor(
        val data: T,
        mutate: suspend () -> Unit
    ) : SWRResult<T>(mutate) {

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

    operator fun component3() = mutate

    fun requireData() = (this as Success<T>).data

    fun requireException() = (this as Error).exception

    suspend fun mutate() = mutate.invoke()

    companion object {
        fun <K, D> fromData(key: K, data: D?): SWRResult<D> {
            val mutateFunction: suspend () -> Unit = { mutate(key) }
            return if (data == null) Loading(mutateFunction) else Success(data, mutateFunction)
        }

        fun <K> fromError(key: K, error: Throwable) = Error(error) { mutate(key) }
    }
}