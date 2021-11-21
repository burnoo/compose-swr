package dev.burnoo.compose.swr.sample

import androidx.compose.runtime.Composable
import dev.burnoo.cokoin.Koin
import dev.burnoo.compose.swr.sample.model.IpResponse
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
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

    single { Repository(get()) }
}

internal class Repository(private val client: HttpClient) {

     suspend fun fetchIpResponse(): IpResponse {
        return client.request("https://api.ipify.org?format=json")
    }
}

@Composable
fun WithKoin(content: @Composable () -> Unit) {
    Koin(appDeclaration = {
        modules(appModule)
    }) {
        content()
    }
}