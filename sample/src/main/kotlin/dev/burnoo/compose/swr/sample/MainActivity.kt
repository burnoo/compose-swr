package dev.burnoo.compose.swr.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import dev.burnoo.compose.swr.preview.SWRPreview
import dev.burnoo.compose.swr.sample.ui.theme.AppTheme
import dev.burnoo.compose.swr.useSWR
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WithKoin {
                AppTheme {
                    Surface(color = MaterialTheme.colors.background) {
                        App()
                    }
                }
            }
        }
    }
}

var counter = 0

@Composable
fun App() {
    val state = useSWR(
        key = Unit,
        fetcher = {
            delay(100)
            counter++.toString()
        }
    ) { refreshInterval = 1000L }
    val scope = rememberCoroutineScope()

    val (data, error, isValidating) = state
    Column {
        when {
            error != null -> Text(text = "Failed to load")
            data != null -> Text(text = "$data $isValidating")
            else -> Text(text = "Loading")
        }
        Button(onClick = {
            scope.launch {
                state.mutate("7", false)
            }
        }) {
            Text("Mutate")
        }
        IpApp()
    }
}

@Serializable
data class IpResponse(val ip: String)

@Composable
fun IpApp() {
    val client = get<HttpClient>() // Using Koin for Jetpack Compose
    val (data, error) = useSWR(
        key = "https://api.ipify.org?format=json",
        fetcher = { client.request<IpResponse>(it) }
    )

    when {
        error != null -> Text(text = "Failed to load")
        data != null -> Text(text = data.ip)
        else -> Text(text = "Loading")
    }
}

@Preview
@Composable
fun IpAppSuccessPreview() {
    WithKoin {
        SWRPreview(data = IpResponse("1.2.3.4")) {
            IpApp()
        }
    }
}