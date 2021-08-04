package dev.burnoo.swr.ktor

import androidx.compose.runtime.Composable
import io.ktor.client.*
import io.ktor.client.features.json.*
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@PublishedApi
internal object KoinContext {
    private val module = module {
        single { HttpClient { install(JsonFeature) } }
    }

    val koinApp = koinApplication {
        modules(module)
    }
}

@PublishedApi
@Composable
internal inline fun <reified T> get(): T = KoinContext.koinApp.koin.get()