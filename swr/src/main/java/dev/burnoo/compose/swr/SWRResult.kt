package dev.burnoo.compose.swr

sealed class SWRResult<out T> {
    object Loading : SWRResult<Nothing>()
    class Success<T>(val data: T) : SWRResult<T>()
    class Error(val exception: Exception) : SWRResult<Nothing>()

    operator fun component1(): T? = if (this is Success) data else null

    operator fun component2() = if (this is Error) exception else null
}