# compose-swr
React SWR ported for Compose (Jetpack + Multiplatform)

# Coding in progress... 👨🏽‍💻
Still working, but you can take a sneak peak:
- [Libarary API (useSWR)](https://github.com/burnoo/compose-swr/blob/main/swr/src/commonMain/kotlin/dev/burnoo/compose/swr/UseSWR.kt)
- [Usage](https://github.com/burnoo/compose-swr/blob/main/sample/src/main/java/dev/burnoo/compose/swr/sample/MainActivity.kt)

## TODO
- docs
- publish to MavenCentral

## Already implemented:
```kotlin
@Serializable
data class IpResponse(val ip: String)

@Composable
fun App() {
    val client = get<HttpClient>() // Using Koin for Jetpack Compose
    val (data, error) = useSWR(
        key = "https://api.ipify.org?format=json",
        fetcher = { client.request<IpResponse>(it) }
    )

    when {
        error != null -> Text(text = "Failed to load")
        data != null -> Text(text = data.ip)
        else -> Text(text = "Loading")
    }
}
```