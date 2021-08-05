package dev.burnoo.swr.ktor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import dev.burnoo.compose.swr.SWRConfig
import dev.burnoo.compose.swr.SWRResult
import dev.burnoo.compose.swr.useSWR
import io.ktor.client.*
import io.ktor.client.request.*

@Composable
inline fun <reified D> useSWRKtor(
    url: String,
    noinline configBlock: SWRConfig<D>.() -> Unit = {}
): State<SWRResult<D>> {
    val client = get<HttpClient>()
    return useSWR(key = url, fetcher = { client.request(it) }, configBlock = configBlock)
}