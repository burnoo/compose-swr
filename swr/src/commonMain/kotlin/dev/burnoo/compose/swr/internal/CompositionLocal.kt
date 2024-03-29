package dev.burnoo.compose.swr.internal

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import dev.burnoo.compose.swr.cache.DefaultCache
import dev.burnoo.compose.swr.cache.SWRCache
import dev.burnoo.compose.swr.config.SWRLocalConfigBlock
import dev.burnoo.compose.swr.state.StaticSWRState
import kotlin.reflect.KClass

@PublishedApi
internal var LocalCache = compositionLocalOf<SWRCache> { DefaultCache() }

@PublishedApi
internal val LocalConfigBlocks =
    mutableMapOf<KClass<Any>, ProvidableCompositionLocal<SWRLocalConfigBlock<Any>>>()

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal inline fun <reified K> getLocalConfigBlock(): ProvidableCompositionLocal<SWRLocalConfigBlock<K>> {
    val key = K::class as KClass<Any>
    return if (LocalConfigBlocks[key] == null) {
        val localConfigBlock = compositionLocalOf<SWRLocalConfigBlock<K>> { { } }
        LocalConfigBlocks[key] =
            localConfigBlock as ProvidableCompositionLocal<SWRLocalConfigBlock<Any>>
        localConfigBlock
    } else {
        LocalConfigBlocks[key] as ProvidableCompositionLocal<SWRLocalConfigBlock<K>>
    }
}

@PublishedApi
internal val LocalPreviewStates =
    mutableMapOf<KClass<Any>, ProvidableCompositionLocal<StaticSWRState<Any>?>>()

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal inline fun <reified D> getLocalPreviewState(): ProvidableCompositionLocal<StaticSWRState<D>?> {
    val key = D::class as KClass<Any>
    return if (LocalPreviewStates[key] == null) {
        val swrPreviewState = compositionLocalOf<StaticSWRState<D>?> { null }
        LocalPreviewStates[key] =
            swrPreviewState as ProvidableCompositionLocal<StaticSWRState<Any>?>
        swrPreviewState
    } else {
        LocalPreviewStates[key] as ProvidableCompositionLocal<StaticSWRState<D>?>
    }
}