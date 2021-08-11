package dev.burnoo.compose.swr.model

typealias SWRConfigBlock<K, D> = SWRConfig<K, D>.() -> Unit

class SWRConfig<K, D>(block: SWRConfigBlock<K, D> = {}) {

    var initialData: D? = null

    var refreshInterval = 0L
    var shouldRetryOnError = true
    var errorRetryInterval = 5000L
    var errorRetryCount = 3 // Change to 0 after exponential error

    var dedupingInterval = 2000L

    var loadingTimeout = 3000L
    var onLoadingSlow: ((key: K, config: SWRConfig<K, D>) -> Unit)? = null

    var onSuccess: ((data: D, key: K, config: SWRConfig<K, D>) -> Unit)? = null
    var onError: ((error: Exception, key: K, config: SWRConfig<K, D>) -> Unit)? = null

    init {
        apply(block)
    }
}