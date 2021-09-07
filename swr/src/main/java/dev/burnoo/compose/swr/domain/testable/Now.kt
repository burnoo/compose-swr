package dev.burnoo.compose.swr.domain.testable

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal fun interface Now {
    operator fun invoke(): Instant
}

internal var now: Now = Now { Clock.System.now() }