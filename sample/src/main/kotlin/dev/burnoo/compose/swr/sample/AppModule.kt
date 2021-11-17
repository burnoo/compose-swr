package dev.burnoo.compose.swr.sample

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient {
            install(JsonFeature) {
                val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                serializer = KotlinxSerializer(json)
            }
        }
    }
}