package dev.burnoo.compose.swr.utils

import dev.burnoo.compose.swr.model.SWRConfig

class OnLoadingSlow {
    val invocations = mutableListOf<Args>()

    operator fun invoke(key: String, config: SWRConfig<String, String>) {
        invocations.add(Args(key, config))
    }

    data class Args(val key: String, val config: SWRConfig<String, String>)
}

class OnSuccess {
    val invocations = mutableListOf<Args>()

    operator fun invoke(data: String, key: String, config: SWRConfig<String, String>) {
        invocations.add(Args(data, key, config))
    }

    data class Args(val data: String, val key: String, val config: SWRConfig<String, String>)
}

class OnError {
    val invocations = mutableListOf<Args>()

    operator fun invoke(error: Throwable, key: String, config: SWRConfig<String, String>) {
        invocations.add(Args(error, key, config))
    }

    data class Args(val error: Throwable, val key: String, val config: SWRConfig<String, String>)
}