package dev.burnoo.compose.swr.example.di

import dev.burnoo.compose.swr.example.network.Fetcher
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import org.koin.dsl.module

val apiModule = module {
    single {
        HttpClient { install(JsonFeature) }
    }

    single { Fetcher(get()) }
}