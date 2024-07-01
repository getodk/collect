package org.odk.collect.androidshared.async

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class TrackableWorkerTest {
    private val scheduler = FakeScheduler()
    private val trackableWorker = TrackableWorker(scheduler)

    @Test
    fun `TrackableWorker counts work in progress`() {
        trackableWorker.immediate {}
        trackableWorker.immediate {}

        scheduler.runFirstBackground()
        scheduler.runFirstForeground()
        assertThat(trackableWorker.isWorking.value, equalTo(true))

        scheduler.runFirstBackground()
        scheduler.runFirstForeground()
        assertThat(trackableWorker.isWorking.value, equalTo(false))
    }
}
