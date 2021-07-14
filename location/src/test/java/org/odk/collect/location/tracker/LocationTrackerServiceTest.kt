package org.odk.collect.location.tracker

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.ui.ReturnToAppActivity
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class LocationTrackerServiceTest {

    @Test
    fun onStartCommand_startsServiceInForeground() {
        val service = RobolectricHelpers.startService(LocationTrackerService::class.java, null)
        assertThat(service.foregroundNotification, notNullValue())
    }

    @Test
    fun clickingForegroundNotification_navigatesBackToApp() {
        val service = RobolectricHelpers.startService(LocationTrackerService::class.java, null)
        val intent = service.foregroundNotification.contentIntent
        assertThat(intent.component?.className, equalTo(ReturnToAppActivity::class.qualifiedName))
    }
}
