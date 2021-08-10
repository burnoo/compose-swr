package dev.burnoo.compose.swr.utils

import dev.burnoo.compose.swr.KoinContext
import dev.burnoo.compose.swr.Now
import dev.burnoo.compose.swr.RecomposeCoroutineScope
import dev.burnoo.compose.swr.CoroutineQualifiers
import kotlinx.coroutines.CoroutineScope
import org.koin.core.KoinApplication
import org.koin.core.qualifier.qualifier
import org.koin.dsl.koinApplication
import org.koin.dsl.module

fun testKoinApplication(
    refresherCoroutineScope: CoroutineScope,
    revalidatorCoroutineScope: CoroutineScope,
    recomposeCoroutineScope: CoroutineScope,
    now: Now
): KoinApplication {
    val testModule = module {
        factory(CoroutineQualifiers.Refresher.qualifier) { refresherCoroutineScope }
        factory(CoroutineQualifiers.Revalidator.qualifier) { revalidatorCoroutineScope }
        factory { RecomposeCoroutineScope(recomposeCoroutineScope) }
        single { now }
    }
    return koinApplication { modules(testModule, KoinContext.getAppModule()) }
}