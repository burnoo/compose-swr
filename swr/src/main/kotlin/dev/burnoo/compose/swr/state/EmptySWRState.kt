package dev.burnoo.compose.swr.state

import androidx.compose.runtime.Composable

@PublishedApi
internal class EmptySWRState<D> : SWRState<D> {

    override val data: D?
        @Composable
        get() = null

    override val error: Throwable?
        @Composable
        get() = null

    override val isValidating: Boolean
        @Composable
        get() = false

    override suspend fun mutate(data: D?, shouldRevalidate: Boolean) = Unit
}