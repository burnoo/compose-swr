package dev.burnoo.compose.swr.utils

import dev.burnoo.compose.swr.KoinContext
import dev.burnoo.compose.swr.Now
import dev.burnoo.compose.swr.RecomposeCoroutineScope
import kotlinx.coroutines.CoroutineScope
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module

fun testKoinApplication(
    testCoroutineScope: CoroutineScope,
    testNow: Now
): KoinApplication {
    val testModule = module {
        factory { testCoroutineScope }
        factory { RecomposeCoroutineScope(get()) }
        single { testNow }
    }
    return koinApplication { modules(testModule, KoinContext.getAppModule()) }
}