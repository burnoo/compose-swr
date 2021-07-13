# compose-swr
React SWR ported for Compose (for now only android, but I will try multiplatform)

# Coding in progress... 👨🏽‍💻
Still working, but you can take a sneak peak:
- [Libarary API (useSWR)](https://github.com/burnoo/compose-swr/blob/main/swr/src/main/java/dev/burnoo/compose/swr/UseSWR.kt)
- [Usage](https://github.com/burnoo/compose-swr/blob/main/sample/src/main/java/dev/burnoo/compose/swr/sample/MainActivity.kt)

## TODO
- configurarion (polling on interval, smart error retry etc.)
- add Ktor fetcher module
- global conifg
- publish to MavenCentral
- add multiplatform support

## Already implemented in [4f4e37cb](https://github.com/burnoo/compose-swr/commit/4f4e37cb9fff9da1c811fda340da27873b1e4ff2):
```kotlin
@Composable
fun App() {
    val resultState = useSWR(
        key = "example.com/api",
        fetcher = { url -> NetworkClient.getData(url) }
    )
    val result = resultState.value

    // ported from React SWR
    val (data, exception) = result
    when {
        exception != null -> Text(text = "Failed to load")
        data != null -> Text(text = data)
        else -> Text(text = "Loading")
    }
    
    // or more Kotlin-styled
    when (result) {
        is SWRResult.Success -> Text(text = result.data)
        is SWRResult.Loading -> Text("Loading")
        else -> Text(text = "Failed to load")
    }
}
```
