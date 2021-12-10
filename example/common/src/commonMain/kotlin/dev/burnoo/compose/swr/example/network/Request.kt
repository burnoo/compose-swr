package dev.burnoo.compose.swr.example.network

import dev.burnoo.compose.swr.example.network.model.IpResponse

internal sealed class Request<out D>(val url: String) {
    object Ip : Request<IpResponse>(url = "https://api.ipify.org?format=json")
}