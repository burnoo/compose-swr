package dev.burnoo.compose.swr.example.component

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.burnoo.compose.swr.example.WithIpSuccessPreview
import dev.burnoo.compose.swr.example.WithKoin
import dev.burnoo.compose.swr.example.ui.IpUiState
import dev.burnoo.compose.swr.example.useIp

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