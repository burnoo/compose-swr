package dev.burnoo.compose.swr.sample.component

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.burnoo.compose.swr.WithIpSuccessPreview
import dev.burnoo.compose.swr.sample.WithKoin
import dev.burnoo.compose.swr.ui.IpUiState
import dev.burnoo.compose.swr.useIp

@Composable
fun IpApp() {
    when(val ip = useIp()) {
        is IpUiState.Loading -> CircularProgressIndicator()
        is IpUiState.Error -> Text("Error")
        is IpUiState.Loaded -> Text(ip.ip)
    }
}

@Preview
@Composable
fun IpAppSuccessPreview() {
    WithKoin {
        WithIpSuccessPreview {
            IpApp()
        }
    }
}