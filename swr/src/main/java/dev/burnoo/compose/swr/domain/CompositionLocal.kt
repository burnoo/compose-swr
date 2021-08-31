package dev.burnoo.compose.swr.domain

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import dev.burnoo.compose.swr.model.SWRConfigBlock
import kotlin.reflect.KClass

@PublishedApi
internal val configBlockCompositions =
    mutableMapOf<Pair<KClass<Any>, KClass<Any>>,
            ProvidableCompositionLocal<SWRConfigBlock<Any, Any>>>()

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal inline fun <reified K, reified D> getConfigBlockComposition(): ProvidableCompositionLocal<SWRConfigBlock<K, D>> {
    val p = Pair(K::class, D::class) as Pair<KClass<Any>, KClass<Any>>
    return if (configBlockCompositions[p] == null) {
        val localConfigBlock = compositionLocalOf<SWRConfigBlock<K, D>> { { } }
        configBlockCompositions[p] = localConfigBlock as ProvidableCompositionLocal<SWRConfigBlock<Any, Any>>
        localConfigBlock
    } else {
        configBlockCompositions[p] as ProvidableCompositionLocal<SWRConfigBlock<K, D>>
    }
}