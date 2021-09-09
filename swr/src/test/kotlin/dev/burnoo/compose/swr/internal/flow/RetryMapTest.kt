package dev.burnoo.compose.swr.internal.flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Test

private data class TestRequest(val shouldFail: Boolean = false, val shouldRetry: Boolean = true)

private sealed class TestResult {
    object Success : TestResult()
    object Failure : TestResult()
    open class Loading : TestResult() {
        data class Retry(val count: Long) : Loading()

        override fun equals(other: Any?) = javaClass == other?.javaClass
        override fun hashCode() = javaClass.hashCode()
    }
}

private suspend fun getResult(request: TestRequest): TestResult {
    delay(500L)
    return if (!request.shouldFail) {
        TestResult.Success
    } else {
        TestResult.Failure
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RetryMapTest {

    private val testCoroutineScope = TestCoroutineScope()

    @Test
    fun `do not retry on success`() {
        val testStateFlow = MutableStateFlow<TestResult?>(null)
        flowOf(TestRequest(shouldFail = false))
            .retryMap(::getResult) { _, result, _ ->
                result is TestResult.Failure
            }
            .onStart { emit(TestResult.Loading()) }
            .onEach { testStateFlow.value = it }
            .launchIn(testCoroutineScope)

        assertEquals(TestResult.Loading(), testStateFlow.value)
        testCoroutineScope.advanceTimeBy(500L)
        assertEquals(TestResult.Success, testStateFlow.value)
    }

    @Test
    fun `retry 3 times`() {
        val testStateFlow = MutableStateFlow<TestResult?>(null)
        flowOf(TestRequest(shouldFail = true))
            .retryMap(::getResult) { _, _, attempt ->
                attempt <= 3
            }
            .onStart { emit(TestResult.Loading()) }
            .onEach { testStateFlow.value = it }
            .launchIn(testCoroutineScope)

        assertEquals(TestResult.Loading(), testStateFlow.value)
        testCoroutineScope.advanceTimeBy(500L)
        assertEquals(TestResult.Loading(), testStateFlow.value)
        testCoroutineScope.advanceTimeBy(500L)
        assertEquals(TestResult.Loading(), testStateFlow.value)
        testCoroutineScope.advanceTimeBy(500L)
        assertEquals(TestResult.Loading(), testStateFlow.value)
        testCoroutineScope.advanceTimeBy(500L)
        assertEquals(TestResult.Failure, testStateFlow.value)
    }

    @Test
    fun `do not retry based on request value`() {
        val testStateFlow = MutableStateFlow<TestResult?>(null)
        flowOf(TestRequest(shouldFail = true, shouldRetry = false))
            .retryMap(::getResult) { request, result, _ ->
                result is TestResult.Failure && request.shouldRetry
            }
            .onStart { emit(TestResult.Loading()) }
            .onEach { testStateFlow.value = it }
            .launchIn(testCoroutineScope)

        assertEquals(TestResult.Loading(), testStateFlow.value)
        testCoroutineScope.advanceTimeBy(500L)
        assertEquals(TestResult.Failure, testStateFlow.value)
    }

    @Test
    fun `emit on retry downstream`() {
        val testStateFlow = MutableStateFlow<TestResult?>(null)
        flowOf(TestRequest(shouldFail = true))
            .retryMap(::getResult) { _, result, attempt ->
                val shouldRetry = result is TestResult.Failure
                if (shouldRetry) {
                    emit(TestResult.Loading.Retry(attempt))
                }
                shouldRetry
            }
            .onStart { emit(TestResult.Loading()) }
            .onEach { testStateFlow.value = it }
            .launchIn(testCoroutineScope)

        assertEquals(TestResult.Loading(), testStateFlow.value)
        testCoroutineScope.advanceTimeBy(500L)
        assertEquals(TestResult.Loading.Retry(1), testStateFlow.value)
        testCoroutineScope.advanceTimeBy(500L)
        assertEquals(TestResult.Loading.Retry(2), testStateFlow.value)
    }

    @Test
    fun `reset attempt counter for the next item`() {
        val testStateFlow = MutableStateFlow<TestResult?>(null)
        flow {
            emit(TestRequest(shouldFail = true))
            delay(5000L)
            emit(TestRequest(shouldFail = true))
        }
            .retryMap(::getResult) { _, _, attempt ->
                val shouldRetry = attempt <= 1
                if (shouldRetry) {
                    emit(TestResult.Loading.Retry(attempt))
                }
                shouldRetry
            }
            .onStart { emit(TestResult.Loading()) }
            .onEach {
                println(it)
                testStateFlow.value = it
            }
            .launchIn(testCoroutineScope)
        assertEquals(TestResult.Loading(), testStateFlow.value)
        testCoroutineScope.advanceTimeBy(500L)
        assertEquals(TestResult.Loading.Retry(count = 1), testStateFlow.value)
        testCoroutineScope.advanceTimeBy(500L)
        assertEquals(TestResult.Failure, testStateFlow.value)
        testCoroutineScope.advanceTimeBy(5500L)
        assertEquals(TestResult.Loading.Retry(count = 1), testStateFlow.value)
        testCoroutineScope.advanceTimeBy(500L)
        assertEquals(TestResult.Failure, testStateFlow.value)
    }
}