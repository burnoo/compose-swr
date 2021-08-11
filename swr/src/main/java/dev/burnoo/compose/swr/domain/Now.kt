package dev.burnoo.compose.swr.domain

import kotlinx.datetime.Instant

fun interface Now {
    operator fun invoke(): Instant
}