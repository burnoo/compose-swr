package dev.burnoo.compose.swr.utils

import kotlinx.coroutines.delay

class StringFetcher(private val delay: Long = 100L) {
    private var counter = 1

    suspend fun fetch(key: String): String {
        delay(delay);
        return "$key${counter++}"
    }
}

class FailingFetcher {
    var failCount = 0

    suspend fun fetch(key: String) : String {
        delay(100)
        failCount++
        throw Exception("Exception while fetching $key")
    }
}