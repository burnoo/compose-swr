package dev.burnoo.compose.swr

import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.burnoo.compose.swr.domain.DefaultCache
import dev.burnoo.compose.swr.model.config.SWRLocalConfigBlock
import dev.burnoo.compose.swr.utils.BaseTest
import dev.burnoo.compose.swr.utils.FailingFetcher
import dev.burnoo.compose.swr.utils.OnSuccess
import dev.burnoo.compose.swr.utils.key
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UseSWRTest : BaseTest() {


}