package dev.burnoo.compose.swr.di

import dev.burnoo.compose.swr.domain.*
import dev.burnoo.compose.swr.model.RecomposeCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.koinApplication
import org.koin.dsl.module

internal object KoinContext {
    private val testableModule = module {
        factory { CoroutineScope(Dispatchers.Default) }
        factory { RecomposeCoroutineScope() }
        single { Now { Clock.System.now() } }
    }

    fun getAppModule() = module {
        single { Cache() }
        single {
            Refresher(
                cache = get(),
                now = get(),
                scope = get(),
            )
        }
        single {
            SWR(
                cache = get(),
                now = get(),
                refresher = get(),
                recomposeCoroutineScope = get()
            )
        }
    }

    var koinApp = koinApplication {
        modules(testableModule, getAppModule())
    }
}

internal inline fun <reified T> get(qualifier: Qualifier? = null): T =
    KoinContext.koinApp.koin.get(qualifier)