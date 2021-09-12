package dev.burnoo.compose.swr.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.koinApplication

@PublishedApi
internal val LocalKoin = compositionLocalOf { GlobalContext.get() }

@Composable
fun Koin(
    appDeclaration: KoinAppDeclaration? = null,
    content: @Composable () -> Unit
) {
    val koinApplication = koinApplication(appDeclaration)
    CompositionLocalProvider(LocalKoin provides koinApplication.koin) {
        content()
    }
}

@Composable
inline fun <reified T> get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T {
    val koin = getKoin()
    return remember(qualifier, parameters) {
        koin.get(qualifier, parameters)
    }
}

@Composable
fun getKoin(): Koin = LocalKoin.current
