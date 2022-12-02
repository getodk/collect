package org.odk.collect.selfiecamera

import android.Manifest
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.selfiecamera.support.RobolectricApplication

@RunWith(AndroidJUnit4::class)
class CaptureSelfieActivityTest {

    private val application = ApplicationProvider.getApplicationContext<RobolectricApplication>()
    private val permissionsChecker = FakePermissionsChecker()

    @get:Rule
    val launcher = ActivityScenarioLauncherRule()

    @Before
    fun setup() {
        application.testObjectProvider.also {
            it.addSupplier(PermissionsChecker::class.java) { permissionsChecker }
        }
    }

    @Test
    fun whenCameraPermissionNotGranted_finishes() {
        permissionsChecker.deny(Manifest.permission.CAMERA)

        val scenario = launcher.launch(CaptureSelfieActivity::class.java)
        assertThat(scenario.state, equalTo(Lifecycle.State.DESTROYED))
    }

    @Test
    fun whenTakingVideo_andAudioPermissionNotGranted_finishes() {
        permissionsChecker.deny(Manifest.permission.RECORD_AUDIO)

        val intent = Intent(application, CaptureSelfieActivity::class.java).also {
            it.putExtra(CaptureSelfieActivity.EXTRA_VIDEO, true)
        }

        val scenario = launcher.launch<CaptureSelfieActivity>(intent)
        assertThat(scenario.state, equalTo(Lifecycle.State.DESTROYED))
    }
}

private class FakePermissionsChecker : PermissionsChecker {

    private val denied = mutableListOf<String>()

    fun deny(permission: String) {
        denied.add(permission)
    }

    override fun isPermissionGranted(vararg permissions: String): Boolean {
        return permissions.none { denied.contains(it) }
    }
}
