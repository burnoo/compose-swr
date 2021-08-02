package dev.burnoo.compose.swr.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.burnoo.compose.swr.SWRResult
import dev.burnoo.compose.swr.sample.ui.theme.AppTheme
import dev.burnoo.compose.swr.useSWR
import dev.burnoo.swr.ktor.swrKtorJsonClient
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    KtorApp()
                }
            }
        }
    }
}

@Composable
fun App() {
    val resultState = useSWR(
        key = "example.com/api",
        fetcher = { url -> NetworkClient.getData(url) }
    )
    val result = resultState.value

    // ported from React SWR
    val (data, exception) = result
    when {
        exception != null -> Text(text = "Failed to load")
        data != null -> Text(text = data)
        else -> Text(text = "Loading")
    }

    // or more Kotlin-styled
    when (result) {
        is SWRResult.Success -> Text(text = result.data)
        is SWRResult.Loading -> Text("Loading")
        is SWRResult.Error -> Text(text = "Failed to load")
    }
}

@Serializable
data class IpResponse(val ip: String)

@Composable
fun KtorApp() {
    val client = swrKtorJsonClient()
    val resultState = useSWR<String, IpResponse>(
        key = "https://api.ipify.org?format=json",
        fetcher = { client.request(it) }
    )

    when (val result = resultState.value) {
        is SWRResult.Success -> Text(text = result.data.ip)
        is SWRResult.Loading -> Text("Loading")
        is SWRResult.Error -> Text(text = "Failed to load")
    }
}