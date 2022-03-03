package org.odk.collect.android.backgroundwork.autosend

import android.net.ConnectivityManager
import android.net.NetworkInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.network.NetworkStateProvider
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class GeneralAutoSendCheckerTest {
    private val projectID = "projectID"
    private lateinit var networkInfo: NetworkInfo
    private lateinit var networkStateProvider: NetworkStateProvider
    private lateinit var settingsProvider: SettingsProvider
    private lateinit var generalAutoSendChecker: GeneralAutoSendChecker

    @Before
    fun setup() {
        networkInfo = mock()
        networkStateProvider = mock<NetworkStateProvider>().also {
            whenever(it.networkInfo).thenReturn(networkInfo)
        }
        settingsProvider = InMemSettingsProvider()
        generalAutoSendChecker = GeneralAutoSendChecker(networkStateProvider, settingsProvider)
    }

    @Test
    fun `if networkInfo is null return false`() {
        whenever(networkStateProvider.networkInfo).thenReturn(null)

        assertFalse(generalAutoSendChecker.isAutoSendEnabled(projectID))
    }

    @Test
    fun `if autosend disabled return false`() {
        settingsProvider.getUnprotectedSettings(projectID).save(ProjectKeys.KEY_AUTOSEND, "off")

        assertFalse(generalAutoSendChecker.isAutoSendEnabled(projectID))
    }

    @Test
    fun `if autosend enabled for wifi only but wifi is disabled return false`() {
        whenever(networkInfo.type).thenReturn(ConnectivityManager.TYPE_DUMMY)
        settingsProvider.getUnprotectedSettings(projectID).save(ProjectKeys.KEY_AUTOSEND, "wifi_only")

        assertFalse(generalAutoSendChecker.isAutoSendEnabled(projectID))
    }

    @Test
    fun `if autosend enabled for wifi only and wifi is enabled return true`() {
        whenever(networkInfo.type).thenReturn(ConnectivityManager.TYPE_WIFI)
        settingsProvider.getUnprotectedSettings(projectID).save(ProjectKeys.KEY_AUTOSEND, "wifi_only")

        assertTrue(generalAutoSendChecker.isAutoSendEnabled(projectID))
    }

    @Test
    fun `if autosend enabled for cellular only but cellular is disabled return false`() {
        whenever(networkInfo.type).thenReturn(ConnectivityManager.TYPE_DUMMY)
        settingsProvider.getUnprotectedSettings(projectID).save(ProjectKeys.KEY_AUTOSEND, "cellular_only")

        assertFalse(generalAutoSendChecker.isAutoSendEnabled(projectID))
    }

    @Test
    fun `if autosend enabled for cellular only and cellular is enabled return true`() {
        whenever(networkInfo.type).thenReturn(ConnectivityManager.TYPE_MOBILE)
        settingsProvider.getUnprotectedSettings(projectID).save(ProjectKeys.KEY_AUTOSEND, "cellular_only")

        assertTrue(generalAutoSendChecker.isAutoSendEnabled(projectID))
    }

    @Test
    fun `if autosend enabled for wifi and cellular but both are disabled return false`() {
        whenever(networkInfo.type).thenReturn(ConnectivityManager.TYPE_DUMMY)
        settingsProvider.getUnprotectedSettings(projectID).save(ProjectKeys.KEY_AUTOSEND, "wifi_and_cellular")

        assertFalse(generalAutoSendChecker.isAutoSendEnabled(projectID))
    }

    @Test
    fun `if autosend enabled for wifi and cellular and wifi is enabled return true`() {
        whenever(networkInfo.type).thenReturn(ConnectivityManager.TYPE_WIFI)
        settingsProvider.getUnprotectedSettings(projectID).save(ProjectKeys.KEY_AUTOSEND, "wifi_and_cellular")

        assertTrue(generalAutoSendChecker.isAutoSendEnabled(projectID))
    }

    @Test
    fun `if autosend enabled for wifi and cellular and cellular is enabled return true`() {
        whenever(networkInfo.type).thenReturn(ConnectivityManager.TYPE_MOBILE)
        settingsProvider.getUnprotectedSettings(projectID).save(ProjectKeys.KEY_AUTOSEND, "wifi_and_cellular")

        assertTrue(generalAutoSendChecker.isAutoSendEnabled(projectID))
    }
}
