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
import dev.burnoo.compose.swr.sample.ui.theme.AppTheme
import dev.burnoo.compose.swr.useSWR
import dev.burnoo.swr.ktor.useSWRKtor
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.get

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    MutationApp()
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

var counter = 0

@Composable
fun MutationApp() {
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
    val state = useSWRKtor<RandomUserResponse>(url = "https://randomuser.me/api/") {
        refreshInterval = 5000L
    }
    val (data, error, isValidating) = state
    when {
        error != null -> Text(text = "Failed to load")
        data != null -> Text(text = "${data.firstEmail} $isValidating")
        else -> Text(text = "Loading")
    }
}