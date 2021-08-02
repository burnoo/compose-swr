package dev.burnoo.swr.ktor

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.*
import io.ktor.client.features.json.*

@Composable
fun swrKtorJsonClient() : HttpClient {
    val swrKtor = viewModel<SWRKtor>()
    return swrKtor.client
}

internal class SWRKtor : ViewModel() {
    val client by lazy {
        HttpClient { install(JsonFeature) }
    }
}