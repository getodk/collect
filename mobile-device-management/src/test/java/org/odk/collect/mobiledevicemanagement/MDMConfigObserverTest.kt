package org.odk.collect.mobiledevicemanagement

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.RestrictionsManager
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.testshared.FakeScheduler
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class MDMConfigObserverTest {
    private lateinit var scheduler: FakeScheduler
    private lateinit var mdmConfigHandler: MDMConfigHandler
    private lateinit var restrictionsManager: RestrictionsManager
    private lateinit var context: Context
    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var mdmConfigObserver: MDMConfigObserver
    private lateinit var managedConfig: Bundle

    @Before
    fun setup() {
        scheduler = FakeScheduler()
        mdmConfigHandler = mock<MDMConfigHandler>()
        restrictionsManager = mock<RestrictionsManager>()
        context = ApplicationProvider.getApplicationContext()
        lifecycleOwner = mock<LifecycleOwner>()
        managedConfig = Bundle()

        mdmConfigObserver = MDMConfigObserver(scheduler, mdmConfigHandler, restrictionsManager, context)

        whenever(restrictionsManager.applicationRestrictions).thenReturn(managedConfig)
    }

    @Test
    fun `broadcast receiver is registered in #onResume`() {
        val shadowApp = shadowOf(context as Application)

        assert(shadowApp.registeredReceivers.none {
            it.intentFilter.hasAction(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED)
        })

        mdmConfigObserver.onResume(lifecycleOwner)

        assert(shadowApp.registeredReceivers.any {
            it.intentFilter.hasAction(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED)
        })
    }

    @Test
    fun `broadcast receiver is unregistered in #onPause`() {
        mdmConfigObserver.onResume(lifecycleOwner)
        mdmConfigObserver.onPause(lifecycleOwner)

        val shadowApp = shadowOf(context as Application)

        assert(shadowApp.registeredReceivers.none {
            it.intentFilter.hasAction(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED)
        })
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

        val intent = Intent(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED)
        context.sendBroadcast(intent)

        shadowOf(context.mainLooper).idle()
        scheduler.runBackground()

        verify(mdmConfigHandler, times(2)).applyConfig(managedConfig)
    }
}
