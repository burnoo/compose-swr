package dev.burnoo.compose.swr

import kotlinx.datetime.Instant

fun interface Now {
    operator fun invoke(): Instant
}