package org.odk.collect.mobiledevicemanagement

import android.app.Application
import android.content.Intent
import android.content.RestrictionsManager
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.testshared.FakeBroadcastReceiverRegister
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class MDMConfigObserverTest {
    private val scheduler = FakeScheduler()
    private val mdmConfigHandler = mock<MDMConfigHandler>()
    private val broadcastReceiverRegister = FakeBroadcastReceiverRegister()
    private val managedConfig = Bundle()
    private val restrictionsManager = mock<RestrictionsManager>()
        .apply { whenever(applicationRestrictions).thenReturn(managedConfig) }
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val lifecycleOwner = mock<LifecycleOwner>()
    private val mdmConfigObserver = MDMConfigObserver(
        scheduler,
        mdmConfigHandler,
        broadcastReceiverRegister,
        restrictionsManager
    )

    @Test
    fun `broadcast receiver is registered in #onResume`() {
        assertThat(broadcastReceiverRegister.registeredReceivers.isEmpty(), equalTo(true))

        mdmConfigObserver.onResume(lifecycleOwner)

        assertThat(broadcastReceiverRegister.registeredReceivers.isEmpty(), equalTo(false))
    }

    @Test
    fun `broadcast receiver is unregistered in #onPause`() {
        mdmConfigObserver.onResume(lifecycleOwner)
        mdmConfigObserver.onPause(lifecycleOwner)

        assertThat(broadcastReceiverRegister.registeredReceivers.isEmpty(), equalTo(true))
    }

    @Test
    fun `#onResume triggers ManagedConfigSaver#applyConfig`() {
        mdmConfigObserver.onResume(lifecycleOwner)
        scheduler.runBackground()

        verify(mdmConfigHandler, times(1)).applyConfig(managedConfig)
    }

    @Test
    fun `broadcast receiver triggers ManagedConfigSaver#applyConfig`() {
        mdmConfigObserver.onResume(lifecycleOwner)

        broadcastReceiverRegister.registeredReceivers.first().onReceive(context, Intent(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED))
        scheduler.runBackground()

        verify(mdmConfigHandler, times(2)).applyConfig(managedConfig)
    }
}
