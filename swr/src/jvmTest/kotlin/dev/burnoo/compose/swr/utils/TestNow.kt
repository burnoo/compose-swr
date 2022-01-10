package dev.burnoo.compose.swr.utils

import dev.burnoo.compose.swr.internal.testable.Now
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

class TestNow : Now {
    private var currentInstant: Instant = Clock.System.now()

    override fun invoke() = currentInstant

    @OptIn(ExperimentalTime::class)
    fun advanceTimeBy(timeMillis: Long) {
        currentInstant += timeMillis.milliseconds
    }
}