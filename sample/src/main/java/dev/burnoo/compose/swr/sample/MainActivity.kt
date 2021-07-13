package dev.burnoo.compose.swr.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import dev.burnoo.compose.swr.SWRResult
import dev.burnoo.compose.swr.sample.ui.theme.AppTheme
import dev.burnoo.compose.swr.useSWR

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
    val result by useSWR(key = "example.com/api", fetcher = { url -> NetworkClient.getData(url) })

    // Ported from React SWR
    val (data, exception) = result
    when {
        exception != null -> Text(text = "Failed to load")
        data != null -> Text(text = data)
        else -> Text(text = "Loading")
    }
    // OR more Kotlin-styled
    when {
        result is SWRResult.Success -> Text(text = "Failed to load")
        result is SWRResult.Loading -> Text("Loading")
        else -> Text(text = "Failed to load")
    }
}