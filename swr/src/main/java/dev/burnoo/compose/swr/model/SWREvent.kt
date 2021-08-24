package dev.burnoo.compose.swr.model

internal sealed class SWREvent<out D> {
    object StartValidating : SWREvent<Nothing>()
    data class Success<D>(val value: D) : SWREvent<D>()
    data class Local<D>(val value: D) : SWREvent<D>()
    data class Error(val cause: Throwable) : SWREvent<Nothing>()
}