package org.odk.collect.androidshared.async

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class TrackableScopeTest {

    private val testScope = TestScope()

    @Test
    fun `counts work in progress`() {
        val trackableScope = TrackableScope(testScope)
        assertThat(trackableScope.isWorking.value, equalTo(false))

        trackableScope.launch { delay(1.milliseconds) }
        trackableScope.launch { delay(2.milliseconds) }

        assertThat(trackableScope.isWorking.value, equalTo(true))

        testScope.testScheduler.advanceTimeBy(2)
        assertThat(trackableScope.isWorking.value, equalTo(true))

        testScope.testScheduler.advanceTimeBy(3)
        assertThat(trackableScope.isWorking.value, equalTo(false))
    }
}
