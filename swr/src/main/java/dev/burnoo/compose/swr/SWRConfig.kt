package dev.burnoo.compose.swr

class SWRConfig<R> {

    var initialData: R? = null
    var refreshInterval = 0L
    var shouldRetryOnError = true
    var errorRetryInterval = 5000L
}