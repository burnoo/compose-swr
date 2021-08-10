package dev.burnoo.compose.swr

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.qualifier
import org.koin.dsl.koinApplication
import org.koin.dsl.module

internal enum class CoroutineQualifiers {
    Refresher, Revalidator
}

data class RecomposeCoroutineScope(val scope: CoroutineScope? = null)

internal object KoinContext {
    private val testableModule = module {
        factory(CoroutineQualifiers.Refresher.qualifier) { CoroutineScope(Dispatchers.Default) }
        factory(CoroutineQualifiers.Revalidator.qualifier) { CoroutineScope(Dispatchers.Default) }
        factory { RecomposeCoroutineScope() }
        factory { Now { Clock.System.now() } }
    }

    fun getAppModule() = module {
        single { Cache(now = get()) }
        single {
            Refresher(
                cache = get(),
                now = get(),
                refresherScope = get(CoroutineQualifiers.Refresher.qualifier),
                revalidatorScope = get(CoroutineQualifiers.Revalidator.qualifier)
            )
        }
    }

    var koinApp = koinApplication {
        modules(testableModule, getAppModule())
    }
}

internal inline fun <reified T> get(qualifier: Qualifier? = null): T =
    KoinContext.koinApp.koin.get(qualifier)