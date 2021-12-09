package org.odk.collect.android.application.initialization.upgrade

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class BeforeProjectsInstallDetectorTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `returns false if no legacy installation`() {
        val detector = BeforeProjectsInstallDetector(context)
        assertThat(detector.installDetected(), equalTo(false))
    }

    /**
     * Account for installs of versions before `LAST_LAUNCHED` was added
     */
    @Test
    fun `returns true if legacy general prefs is not empty`() {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString("anything", "anything")
            .apply()

        val detector = BeforeProjectsInstallDetector(context)
        assertThat(detector.installDetected(), equalTo(true))
    }

    /**
     * Account for installs of versions before `LAST_LAUNCHED` was added
     */
    @Test
    fun `returns true if legacy admin prefs is not empty`() {
        context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE).edit()
            .putString("anything", "anything")
            .apply()

        val detector = BeforeProjectsInstallDetector(context)
        assertThat(detector.installDetected(), equalTo(true))
    }

    /**
     * Account for installs of versions before `LAST_LAUNCHED` was added
     */
    @Test
    fun `returns true if legacy metadata dir is not empty`() {
        val metadataDir = File(context.getExternalFilesDir(null), "metadata").also { it.mkdir() }
        File(metadataDir, "something").createNewFile()

        val detector = BeforeProjectsInstallDetector(context)
        assertThat(detector.installDetected(), equalTo(true))
    }
}
