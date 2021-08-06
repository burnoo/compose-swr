package dev.burnoo.compose.swr

class SWRConfig<K, D> {

    var initialData: D? = null

    var refreshInterval = 0L
    var shouldRetryOnError = true
    var errorRetryInterval = 5000L

    var loadingTimeout = 3000L
    var onSlowLoading: ((K, SWRConfig<K, D>) -> Unit)? = null
}