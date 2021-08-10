package dev.burnoo.compose.swr.sample

import android.app.Application
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
    single {
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(json)
            }
        }
    }
}

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SampleApplication)
            modules(appModule)
        }
    }
}