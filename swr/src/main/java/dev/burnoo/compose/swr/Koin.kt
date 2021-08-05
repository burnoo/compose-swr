package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import org.koin.dsl.koinApplication
import org.koin.dsl.module

internal object KoinContext {
    private val module = module {
        single { Cache() }
    }

    val koinApp = koinApplication {
        modules(module)
    }
}

@Composable
internal inline fun <reified T> get(): T = KoinContext.koinApp.koin.get()