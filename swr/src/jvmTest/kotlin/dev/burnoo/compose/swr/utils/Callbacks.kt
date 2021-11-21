package dev.burnoo.compose.swr.utils

import dev.burnoo.compose.swr.config.SWRConfig

internal class OnLoadingSlow {
    val invocations = mutableListOf<Args>()

    operator fun invoke(key: String, config: SWRConfig<String, String>) {
        invocations.add(Args(key, config))
    }

    data class Args(val key: String, val config: SWRConfig<String, String>)
}

internal class OnSuccess {
    val invocations = mutableListOf<Args>()

    operator fun invoke(data: String, key: String, config: SWRConfig<String, String>) {
        invocations.add(Args(data, key, config))
    }

    data class Args(val data: String, val key: String, val config: SWRConfig<String, String>)
}

internal class OnError {
    val invocations = mutableListOf<Args>()

    operator fun invoke(error: Throwable, key: String, config: SWRConfig<String, String>) {
        invocations.add(Args(error, key, config))
    }

    data class Args(val error: Throwable, val key: String, val config: SWRConfig<String, String>)
}