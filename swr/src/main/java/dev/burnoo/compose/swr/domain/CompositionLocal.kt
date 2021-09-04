package dev.burnoo.compose.swr.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import dev.burnoo.compose.swr.model.config.SWRConfigBlock
import kotlin.reflect.KClass

@PublishedApi
internal val configBlockCompositions =
    mutableMapOf<KClass<Any>, ProvidableCompositionLocal<SWRConfigBlock<Any, Any>>>()

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal inline fun <reified K> getLocalConfigBlock(): ProvidableCompositionLocal<SWRConfigBlock<K, Any>> {
    val key = K::class as KClass<Any>
    return if (configBlockCompositions[key] == null) {
        val localConfigBlock = compositionLocalOf<SWRConfigBlock<K, Any>> { { } }
        configBlockCompositions[key] =
            localConfigBlock as ProvidableCompositionLocal<SWRConfigBlock<Any, Any>>
        localConfigBlock
    } else {
        configBlockCompositions[key] as ProvidableCompositionLocal<SWRConfigBlock<K, Any>>
    }
}

@Suppress("UNCHECKED_CAST")
@PublishedApi
@Composable
internal inline fun <reified K, reified D> getCurrentConfigBlock() =
    getLocalConfigBlock<K>().current as SWRConfigBlock<K, D>

@PublishedApi
internal var LocalCache = compositionLocalOf<SWRCache> { DefaultCache() }