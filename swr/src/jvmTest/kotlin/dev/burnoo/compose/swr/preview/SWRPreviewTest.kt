package dev.burnoo.compose.swr.preview

import dev.burnoo.compose.swr.useSWR
import dev.burnoo.compose.swr.utils.ComposeBaseTest
import dev.burnoo.compose.swr.utils.DataErrorLoading
import dev.burnoo.compose.swr.utils.key
import org.junit.Test

class SWRPreviewTest : ComposeBaseTest() {

    @Test
    fun showDataInPreview() {
        val previewText = "preview"
        composeTestRule.setContent {
            SWRPreview(data = previewText) {
                val (data, error) = useSWR(
                    key = key,
                    fetcher = stringFetcher::fetch
                )
                DataErrorLoading(data, error)
            }
        }
        assertText(previewText)
    }

    @Test
    fun showFailureInPreview() {
        composeTestRule.setContent {
            SWRPreview<String>(error = Exception("TestException")) {
                val (data, error) = useSWR(
                    key = key,
                    fetcher = stringFetcher::fetch
                )
                DataErrorLoading(data, error)
            }
        }
        assertTextFailure()
    }
}