package dev.burnoo.compose.swr.utils

import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Suppress("TestFunctionName")
@Composable
internal fun DataErrorLoading(data: String?, error: Throwable?) {
    when {
        error != null -> Text(textFailure)
        data != null -> Text(data)
        else -> Text(textLoading)
    }
}