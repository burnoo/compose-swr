package dev.burnoo.compose.swr.sample

import androidx.compose.runtime.Composable
import dev.burnoo.cokoin.Koin

@Composable
fun WithKoin(content: @Composable () -> Unit) {
    Koin(appDeclaration = {
        modules(appModule)
    }) {
        content()
    }
}