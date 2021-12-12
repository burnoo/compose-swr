package dev.burnoo.compose.swr.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import dev.burnoo.compose.swr.example.ui.IpUiState

fun main() = application {
    WithKoin {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Compose SWR example",
            state = WindowState(
                position = WindowPosition.Aligned(Alignment.Center),
            ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                IpApp()
            }
        }
    }
}

@Composable
private fun IpApp() {
    when (val ip = useIp()) {
        is IpUiState.Loading -> CircularProgressIndicator()
        is IpUiState.Error -> Text("Error")
        is IpUiState.Loaded -> Text(ip.ip)
    }
}