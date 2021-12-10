package dev.burnoo.compose.swr.example.network

import io.ktor.client.*
import io.ktor.client.request.*

internal class Fetcher(internal val client: HttpClient) {

    internal suspend inline operator fun <reified T> invoke(request: Request<T>) : T {
        return client.request(request.url)
    }
}