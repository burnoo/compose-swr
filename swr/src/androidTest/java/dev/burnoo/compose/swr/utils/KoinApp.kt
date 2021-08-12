package dev.burnoo.compose.swr.utils

import dev.burnoo.compose.swr.di.KoinContext
import dev.burnoo.compose.swr.domain.Now
import dev.burnoo.compose.swr.model.RecomposeCoroutineScope
import kotlinx.coroutines.CoroutineScope
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module

fun testKoinApplication(
    testCoroutineScope: CoroutineScope,
    testNow: Now
): KoinApplication {
    val testModule = module {
        factory { RecomposeCoroutineScope(testCoroutineScope) }
        single { testNow }
    }
    return koinApplication { modules(testModule, KoinContext.getAppModule()) }
}