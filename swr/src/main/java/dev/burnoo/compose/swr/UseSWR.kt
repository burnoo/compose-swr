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
    val swr: SWR = get()
    val cachedData = swr.getFromCache<D>(key)
    val initialResult = cachedData?.let { Success(cachedData) } ?: Loading
    return produceState<SWRResult<D>>(initialResult, key) {
        value = try {
            Success(fetcher(key))
        } catch (e: Exception) {
            Error(e)
        }
    }
}