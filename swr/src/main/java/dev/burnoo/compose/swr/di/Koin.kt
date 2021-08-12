package dev.burnoo.compose.swr.di

import dev.burnoo.compose.swr.domain.Cache
import dev.burnoo.compose.swr.domain.Now
import dev.burnoo.compose.swr.domain.SWR
import dev.burnoo.compose.swr.model.RecomposeCoroutineScope
import kotlinx.datetime.Clock
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.koinApplication
import org.koin.dsl.module

internal object KoinContext {
    private val testableModule = module {
        factory { RecomposeCoroutineScope() }
        single { Now { Clock.System.now() } }
    }

    fun getAppModule() = module {
        single { Cache() }
        factory { SWR(get(), get()) }
    }

    var koinApp = koinApplication {
        modules(testableModule, getAppModule())
    }
}

internal inline fun <reified T> get(qualifier: Qualifier? = null): T =
    KoinContext.koinApp.koin.get(qualifier)