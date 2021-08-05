package dev.burnoo.swr.ktor

import androidx.compose.runtime.Composable
import kotlinx.serialization.json.Json
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@PublishedApi
internal object KoinContext {
    private val module = module {
        single {
            val json = Json { ignoreUnknownKeys = true }
            HttpClient {
                install(JsonFeature) {
                    serializer = KotlinxSerializer(json)
                }
            }
        }
    }

    val koinApp = koinApplication {
        modules(module)
    }
}

@PublishedApi
@Composable
internal inline fun <reified T> get(): T = KoinContext.koinApp.koin.get()