package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import dev.burnoo.compose.swr.di.get
import dev.burnoo.compose.swr.domain.SWR
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <K> swrIsValidating(key: K): State<Boolean> {
    return swrIsValidatingFlow(key).collectAsState()
}

fun <K> swrIsValidatingFlow(key: K): StateFlow<Boolean> {
    val swr = get<SWR>()
    return swr.isValidating(key)
}

