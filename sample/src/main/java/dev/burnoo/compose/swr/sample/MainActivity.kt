package dev.burnoo.compose.swr.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import dev.burnoo.compose.swr.SWRResult
import dev.burnoo.compose.swr.sample.ui.theme.AppTheme
import dev.burnoo.compose.swr.useSWR
import dev.burnoo.swr.ktor.useSWRKtor
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.get

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

@Serializable
data class IpResponse(val ip: String)

@Composable
fun App() {
    val client = get<HttpClient>() // Using Koin for Jetpack Compose
    val resultState = useSWR<String, IpResponse>(
        key = "https://api.ipify.org?format=json",
        fetcher = { client.request(it) }
    )
    val result = resultState.value

    // ported from React SWR
    val (data, exception) = result
    when {
        exception != null -> Text(text = "Failed to load")
        data != null -> Text(text = data.ip)
        else -> Text(text = "Loading")
    }

    // or more Kotlin-styled
    when (result) {
        is SWRResult.Success -> Text(text = result.data.ip)
        is SWRResult.Loading -> Text("Loading")
        is SWRResult.Error -> Text(text = "Failed to load")
    }
}

@Serializable
data class RandomUserResponse(
    val results: List<Person>
) {
    @Serializable
    data class Person(val email: String)

    val firstEmail = results[0].email
}

@Composable
fun KtorApp() {
    val resultState =  useSWRKtor<RandomUserResponse>(url = "https://randomuser.me/api/") {
        refreshInterval = 5000L
    }
    when(val result = resultState.value) {
        is SWRResult.Success -> Text(text = result.data.firstEmail)
        is SWRResult.Loading -> Text("Loading")
        is SWRResult.Error -> {
            result.exception.printStackTrace()
            Text(text = "Failed to load")
        }
    }
}