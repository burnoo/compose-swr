package dev.burnoo.compose.swr.di

import dev.burnoo.compose.swr.network.Fetcher
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import org.koin.dsl.module

val apiModule = module {
    single {
        HttpClient {
            install(JsonFeature) {
                val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                serializer = KotlinxSerializer(json)
            }
        }
    }

    single { Fetcher(get()) }
}