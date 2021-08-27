package dev.burnoo.compose.swr.di

import androidx.annotation.VisibleForTesting
import dev.burnoo.compose.swr.domain.Cache
import dev.burnoo.compose.swr.domain.SWR
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.koinApplication
import org.koin.dsl.module

internal object KoinContext {

    var koinApp = createKoinApp()

    private fun createKoinApp() = koinApplication {
        modules(getAppModule())
    }

    private fun getAppModule() = module {
        single { Cache() }
        factory { SWR(get()) }
    }

    @VisibleForTesting
    fun restart() {
        koinApp = createKoinApp()
    }
}

internal inline fun <reified T> get(qualifier: Qualifier? = null): T =
    KoinContext.koinApp.koin.get(qualifier)