package dev.burnoo.compose.swr

import org.koin.dsl.koinApplication
import org.koin.dsl.module

internal object KoinContext {
    private val module = module {
        single { Cache() }
        single { ReactiveCache() }
    }

    val koinApp = koinApplication {
        modules(module)
    }
}

internal inline fun <reified T> get(): T = KoinContext.koinApp.koin.get()