package dev.burnoo.compose.swr.sample

import kotlinx.coroutines.delay

object NetworkClient {

    var counter = 1

    suspend fun getData(url: String): String {
        delay(1000)
        return "GET $url | ${counter++}"
    }
}