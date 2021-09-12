package dev.burnoo.compose.swr.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.koin.android.ext.koin.androidContext

@Composable
fun WithKoin(content: @Composable () -> Unit) {
    val context = LocalContext.current
    Koin(appDeclaration = {
        androidContext(context)
        modules(appModule)
    }) {
        content()
    }
}