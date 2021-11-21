package dev.burnoo.compose.swr.state

import androidx.compose.runtime.Composable

@PublishedApi
internal class StaticSWRState<D>(
    private val _data: D?,
    private val _error: Throwable?
) : SWRState<D> {

    constructor() : this(null, null)

    override val data: D?
        @Composable
        get() = _data

    override val error: Throwable?
        @Composable
        get() = _error

    override val isValidating: Boolean
        @Composable
        get() = false

    override suspend fun mutate(data: D?, shouldRevalidate: Boolean) = Unit
}