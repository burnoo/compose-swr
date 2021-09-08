package dev.burnoo.compose.swr.utils

import androidx.annotation.CallSuper
import dev.burnoo.compose.swr.domain.random
import dev.burnoo.compose.swr.internal.testable.now
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseTest {

    private val testNow = TestNow()
    protected val testCoroutineScope = TestCoroutineScope()

    @CallSuper
    @Before
    open fun setUp() {
        now = testNow
        restartRandom()
    }

    protected fun advanceTimeBy(durationMillis: Long) {
        testNow.advanceTimeBy(durationMillis)
        testCoroutineScope.advanceTimeBy(durationMillis)
    }

    protected fun restartRandom() {
        random = Random(0)
    }
}