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
    val exception = Exception("Exception while fetching")

    var failCount = 0

    suspend fun fetch(key: String): String {
        delay(100)
        failCount++
        throw exception
    }
}