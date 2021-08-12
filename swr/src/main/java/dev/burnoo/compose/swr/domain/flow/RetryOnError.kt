package dev.burnoo.compose.swr.domain.flow

import dev.burnoo.compose.swr.model.SWRRequest
import dev.burnoo.compose.swr.model.SWRResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

internal fun <K, D> Flow<SWRRequest<K, D>>.retryOnError(
    getResultFlow: Flow<SWRRequest<K, D>>.() -> Flow<SWRResult<D>>
): Flow<SWRResult<D>> {
    return retryRequestFlow(getResultFlow) { request, result, count ->
        val config = request.config
        val shouldRetry = result is SWRResult.Error && config.shouldRetryOnError &&
                (config.errorRetryCount == 0 || count < config.errorRetryCount)
        if (shouldRetry) {
            delay(config.errorRetryInterval)
        }
        shouldRetry
    }
}

internal fun <Request, Result> Flow<Request>.retryRequest(
    getResult: suspend (Request) -> Result,
    predicate: suspend FlowCollector<Result>.(request: Request, result: Result, count: Int) -> Boolean
): Flow<Result> {
    var retryCount = 0

    suspend fun FlowCollector<Result>.resultCollector(
        request: Request,
        result: Result
    ) {
        if (predicate(request, result, retryCount)) {
            retryCount++
            resultCollector(request, getResult(request))
        } else {
            emit(result)
        }
    }

    return flow {
        collect { request ->
            resultCollector(request, getResult(request))
        }
    }
}

internal fun <Request, Result> Flow<Request>.retryRequestFlow(
    mapToResult: Flow<Request>.() -> Flow<Result>,
    predicate: suspend FlowCollector<Result>.(request: Request, result: Result, count: Int) -> Boolean
): Flow<Result> {
    return retryRequest(
        getResult = { request -> flowOf(request).mapToResult().first() },
        predicate = predicate
    )
}
