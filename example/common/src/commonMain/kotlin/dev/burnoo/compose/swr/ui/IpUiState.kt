package dev.burnoo.compose.swr.ui

sealed class IpUiState {
    object Loading : IpUiState()
    object Error : IpUiState()
    data class Loaded(val ip: String) : IpUiState()

    fun asString() = when (this) {
        is Loading -> "Loading"
        is Error -> "Error"
        is Loaded -> ip
    }
}