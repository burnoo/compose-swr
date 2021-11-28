package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import dev.burnoo.cokoin.get
import dev.burnoo.compose.swr.network.Fetcher
import dev.burnoo.compose.swr.network.model.IpResponse
import dev.burnoo.compose.swr.network.Request
import dev.burnoo.compose.swr.ui.IpUiState
import dev.burnoo.compose.swr.preview.SWRPreview

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