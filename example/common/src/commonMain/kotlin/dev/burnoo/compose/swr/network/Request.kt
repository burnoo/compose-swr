package dev.burnoo.compose.swr.network

import dev.burnoo.compose.swr.network.model.IpResponse

internal sealed class Request<out D>(val url: String) {
    object Ip : Request<IpResponse>(url = "https://api.ipify.org?format=json")
}