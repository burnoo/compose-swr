# compose-swr
[React SWR](https://swr.vercel.app/) ported for Compose (Jetpack + Multiplatform)

## Quick Start:
```kotlin
@Serializable
data class IpResponse(val ip: String)

@Composable
fun App() {
    val client = get<HttpClient>() // Using ktor and cokoin
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
In this example, the `@Composeble useSWR` accepts a `key` and a `fetcher` function.
The `key` is a unique identifier of the request,
and the `fetcher` accepts `key` as its parameter and returns the data asynchronously.

`useSWR` also returns 2 values: `data` and `error`. When the request (fetcher) is not yet finished,
`data` will be `undefined`. And when we get a response, it sets `data` and `error` based on the result
of `fetcher` and recompose the component.

Note that `fetcher` can be any suspend function, you can use your favourite data-fetching
library to handle that part (e.g. Ktor Client).

## About

*Based on the original library*:

SWR is a Compose library for data fetching.

The name “**SWR**” is derived from `stale-while-revalidate`, a cache invalidation strategy popularized by [HTTP RFC 5861](https://tools.ietf.org/html/rfc5861).
**SWR** first returns the data from cache (stale), then sends the request (revalidate), and finally comes with the up-to-date data again.

With just one function usage, you can significantly simplify the data fetching logic in your project. And it also covered in all aspects of speed, correctness, and stability to help you build better experiences:

- **Fast**, **lightweight** and **reusable** data fetching
- Transport and protocol agnostic
- Built-in **cache** and request deduplication
- **Real-time** experience
- Polling
- Local mutation (Optimistic UI)
- Built-in smart error retry

...and a lot more.

With SWR, components will get **a stream of data updates constantly and automatically**. Thus, the UI will be always **fast** and **reactive**.