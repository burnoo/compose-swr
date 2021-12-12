---
slug: /
---

# Getting Started

## Installation

TODO publish to maven

## Quick Start

For RESTful APIs with JSON data, first you need to configure HTTP client and JSON parsing, to use it in a `fetcher` function.
You can use `Ktor` client with `kotlinx.serialization` serializer:

```kotlin
val client = HttpClient { install(JsonFeature) }

@Serializable
data class IpResponse(val ip: String)
```

Then you can and start using `useSWR` inside any `@Composable` function:

```kotlin
@Composable
fun App() {
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

Normally, there're 3 possible states of a request: "loading", "ready", or "error". You can use the value of `data` and `error` to
determine the current state of the request, and return the corresponding UI.


## Make It Reusable

When building an app, you might need to reuse the data in many places of the UI. It is incredibly easy to create reusable data hooks
on top of SWR:

```kotlin
sealed class IpUiState {
    object Loading : IpUiState()
    object Error : IpUiState()
    data class Loaded(val ip: String) : IpUiState()
}

@Composable
fun useIp(): IpUiState {
    val (data, error) = useSWR(
        key = "https://api.ipify.org?format=json",
        fetcher = { client.request<IpResponse>(it) }
    )

    return when {
        error != null -> IpUiState.Error
        data != null -> IpUiState.Loaded(data.ip)
        else -> IpUiState.Loading
    }
}
```

And use it in your components:

```kotlin
@Composable
fun IpComponent() {
    when (val ipState = useIp()) {
        is IpUiState.Loading -> CircularProgressIndicator()
        is IpUiState.Error -> ErrorBox()
        is IpUiState.Loaded -> IpBox(ip = ipState.ip)
    }
}
```

By adopting this pattern, you can forget about **fetching** data in the imperative way: start the request, update the loading state, and return the final result.
Instead, your code is more declarative: you just need to specify what data is used by the component.

## Fetcher abstraction and scoping
Storing bare `HttpClient` as the global property is not considered to be a good practice and may lead to memory leaks.
It is suggested to use abstraction to hide http implementation details from composables, e.g:

```kotlin
internal sealed class Request<out D>(val url: String) {
    object Ip : Request<IpResponse>(url = "https://api.ipify.org?format=json")
}

internal class Fetcher(internal val client: HttpClient) {

    internal suspend inline operator fun <reified T> invoke(request: Request<T>) : T {
        return client.request(request.url)
    }
}
```

Then, you should scope `Fetcher` instance to Composable application. It can be done using [cokoin](https://github.com/burnoo/cokoin) - Compose DI library:

```kotlin
val apiModule = module {
    single { HttpClient { install(JsonFeature) } }

    single { Fetcher(client = get()) }
}

@Composable
fun useIp(): IpUiState {
    val fetcher = get<Fetcher>()
    val (data, error) = useSWR(
        key = Request.Ip,
        fetcher = { fetcher(it) },
    )
    return when {
        error != null -> IpUiState.Error
        data != null -> IpUiState.Loaded(data.ip)
        else -> IpUiState.Loading
    }
}

@Composable
fun App() {
    Koin(appDeclaration = { modules(apiModule) }) {
        IpComponent()
    }
}
```