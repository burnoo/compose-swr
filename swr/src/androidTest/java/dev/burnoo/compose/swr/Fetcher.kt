package dev.burnoo.compose.swr

import kotlinx.coroutines.delay

class Fetcher {
    private var counter = 1

    suspend fun fetch(key: String): String {
        delay(100);
        return "$key${counter++}"
    }
}