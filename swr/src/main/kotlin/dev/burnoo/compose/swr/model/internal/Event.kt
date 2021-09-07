package dev.burnoo.compose.swr.model.internal

internal sealed class Event<out D> {
    object StartValidating : Event<Nothing>()
    data class Success<D>(val value: D) : Event<D>()
    data class Local<D>(val value: D) : Event<D>()
    data class Error(val cause: Throwable) : Event<Nothing>()
}