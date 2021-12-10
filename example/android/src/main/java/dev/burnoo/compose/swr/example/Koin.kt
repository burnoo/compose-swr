package dev.burnoo.compose.swr.example

import androidx.compose.runtime.Composable
import dev.burnoo.cokoin.Koin
import dev.burnoo.compose.swr.example.di.apiModule

@Composable
fun WithKoin(content: @Composable () -> Unit) {
    Koin(appDeclaration = {
        modules(apiModule)
    }) {
        content()
    }
}