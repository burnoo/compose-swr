package dev.burnoo.compose.swr

import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.core.KoinApplication
import org.koin.core.qualifier.qualifier
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class TestNow : Now {
    private var currentInstant: Instant = Clock.System.now()

    override fun invoke() = currentInstant

    @OptIn(ExperimentalTime::class)
    fun advanceTimeBy(timeMillis: Long) {
        currentInstant += Duration.milliseconds(timeMillis)
    }
}

fun testKoinApplication(
    refresherCoroutineScope: CoroutineScope,
    revalidatorCoroutineScope: CoroutineScope,
    recomposeCoroutineScope: CoroutineScope,
    now: Now
): KoinApplication {
    val testModule = module {
        factory(qualifier(ScopeQualifiers.Refresher)) { refresherCoroutineScope }
        factory(qualifier(ScopeQualifiers.Revalidator)) { revalidatorCoroutineScope }
        factory { RecomposeCoroutineScope(recomposeCoroutineScope) }
        single { now }
    }
    return koinApplication { modules(testModule, KoinContext.getAppModule()) }
}