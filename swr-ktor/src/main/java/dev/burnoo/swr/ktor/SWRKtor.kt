package dev.burnoo.swr.ktor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import dev.burnoo.compose.swr.model.SWRConfig
import dev.burnoo.compose.swr.model.SWRState
import dev.burnoo.compose.swr.useSWR
import io.ktor.client.*
import io.ktor.client.request.*

@Composable
inline fun <reified D> useSWRKtor(
    url: String,
    noinline config: SWRConfig<String, D>.() -> Unit = {}
): State<SWRState<D>> {
    val client = get<HttpClient>()
    return useSWR(key = url, fetcher = { client.request(it) }, config = config)
}