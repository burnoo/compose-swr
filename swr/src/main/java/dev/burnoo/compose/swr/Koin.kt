package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import org.koin.dsl.koinApplication
import org.koin.dsl.module

internal object SWRKoinContext {
    private val swrModule = module {
        single { SWR() }
    }

    val koinApp = koinApplication {
        modules(swrModule)
    }
}

@Composable
internal inline fun <reified T> get(): T = SWRKoinContext.koinApp.koin.get()