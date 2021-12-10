package dev.burnoo.compose.swr.example

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import dev.burnoo.compose.swr.useSWR
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

var counter = 0

@Composable
fun CounterApp() {
    val state = useSWR(
        key = Unit,
        fetcher = {
            delay(100)
            counter++.toString()
        }
    ) { refreshInterval = 1000L }
    val scope = rememberCoroutineScope()

    val (data, error, isValidating) = state
    when {
        error != null -> Text(text = "Failed to load")
        data != null -> Text(text = "$data $isValidating")
        else -> Text(text = "Loading")
    }
    Button(onClick = {
        scope.launch {
            state.mutate("7", false)
        }
    }) {
        Text("Mutate")
    }
}