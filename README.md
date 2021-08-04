# compose-swr
React SWR ported for Compose (for now only android, but I will try multiplatform)

# Coding in progress... 👨🏽‍💻
Still working, but you can take a sneak peak:
- [Libarary API (useSWR)](https://github.com/burnoo/compose-swr/blob/main/swr/src/main/java/dev/burnoo/compose/swr/UseSWR.kt)
- [Usage](https://github.com/burnoo/compose-swr/blob/main/sample/src/main/java/dev/burnoo/compose/swr/sample/MainActivity.kt)

## TODO
- configurarion (polling on interval, smart error retry etc.)
- global conifg
- publish to MavenCentral
- add multiplatform support

## Already implemented:
```kotlin
@Serializable
data class IpResponse(val ip: String)

@Composable
fun App() {
    val client = get<HttpClient>() // Using Koin for Jetpack Compose
    val resultState = useSWR<String, IpResponse>(
        key = "https://api.ipify.org?format=json",
        fetcher = { client.request(it) }
    )
    val result = resultState.value

    // ported from React SWR
    val (data, exception) = result
    when {
        exception != null -> Text(text = "Failed to load")
        data != null -> Text(text = data.ip)
        else -> Text(text = "Loading")
    }

    // or more Kotlin-styled
    when (result) {
        is SWRResult.Success -> Text(text = result.data.ip)
        is SWRResult.Loading -> Text("Loading")
        is SWRResult.Error -> Text(text = "Failed to load")
    }
}
```
Or even simpler with `swr-ktor`
```kotlin
@Composable
fun KtorApp() {
    val result by useSWRKtor<IpResponse>(url = "https://api.ipify.org?format=json")
    when(result) {
        is SWRResult.Success -> Text(text = result.requireData().ip)
        is SWRResult.Loading -> Text("Loading")
        is SWRResult.Error -> Text(text = "Failed to load")
    }
}
```
