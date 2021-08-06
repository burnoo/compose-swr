package dev.burnoo.compose.swr

class SWRConfig<K, D> {

    var initialData: D? = null

    var refreshInterval = 0L
    var shouldRetryOnError = true
    var errorRetryInterval = 5000L
    var errorRetryCount = 0

    var loadingTimeout = 3000L
    var onLoadingSlow: ((key: K, config: SWRConfig<K, D>) -> Unit)? = null

    var onSuccess: ((data: D, key: K, config: SWRConfig<K, D>) -> Unit)? = null
    var onError: ((error: Exception, key : K, config: SWRConfig<K, D>) -> Unit)? = null
}