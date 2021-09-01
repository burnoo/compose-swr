package dev.burnoo.compose.swr

import androidx.compose.runtime.Composable
import dev.burnoo.compose.swr.domain.getConfigBlockComposition
import dev.burnoo.compose.swr.model.SWRConfig

@Composable
inline fun <reified K, reified D> useSWRConfig(): SWRConfig<K, D> {
    val localConfigBlock = getConfigBlockComposition<K, D>()
    return SWRConfig(localConfigBlock.current)
}