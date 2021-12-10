package dev.burnoo.compose.swr.example

import androidx.compose.runtime.Composable
import dev.burnoo.cokoin.get
import dev.burnoo.compose.swr.example.network.Fetcher
import dev.burnoo.compose.swr.example.network.model.IpResponse
import dev.burnoo.compose.swr.example.network.Request
import dev.burnoo.compose.swr.example.ui.IpUiState
import dev.burnoo.compose.swr.preview.SWRPreview
import dev.burnoo.compose.swr.useSWR

@Composable
fun useIp(): IpUiState {
    val fetcher = get<Fetcher>()
    val (data, error) = useSWR(
        key = Request.Ip,
        fetcher = { fetcher(it) },
    )
    return when {
        error != null -> IpUiState.Error
        data != null -> IpUiState.Loaded(data.ip)
        else -> IpUiState.Loading
    }
}

@Composable
fun WithIpSuccessPreview(content: @Composable () -> Unit) {
    SWRPreview(data = IpResponse("1.2.3.4")) {
        content()
    }
}