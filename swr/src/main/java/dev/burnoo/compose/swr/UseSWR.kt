package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import dev.burnoo.compose.swr.SWRResult.*

@Composable
fun <K : Any, D> useSWR(
    key: K,
    fetcher: suspend (K) -> D,
): State<SWRResult<D>> {
    val swr = get<SWR>()
    val cachedData = swr.getFromCache<D>(key)
    val initialResult = cachedData?.let { Success(cachedData) } ?: Loading
    return produceState<SWRResult<D>>(initialResult, key) {
        value = try {
            val data = fetcher(key)
            swr.saveToCache(key, data)
            Success(data)
        } catch (e: Exception) {
            Error(e)
        }
    }
}