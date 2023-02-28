package org.odk.collect.selfiecamera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.externalapp.ExternalAppUtils
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.selfiecamera.support.RobolectricApplication
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
class CaptureSelfieActivityTest {

    private val application = ApplicationProvider.getApplicationContext<RobolectricApplication>()
    private val permissionsChecker = FakePermissionsChecker()
    private val stillCamera = FakeStillCamera()

    @get:Rule
    val launcher = ActivityScenarioLauncherRule()

    @Before
    fun setup() {
        application.selfieCameraDependencyComponent =
            DaggerSelfieCameraDependencyComponent.builder()
                .selfieCameraDependencyModule(object : SelfieCameraDependencyModule() {
                    override fun providesPermissionChecker(): PermissionsChecker {
                        return permissionsChecker
                    }

                    override fun providesStillCamera(): StillCamera {
                        return stillCamera
                    }
                })
                .build()
    }

    @Test
    fun whenCameraPermissionNotGranted_finishes() {
        permissionsChecker.deny(Manifest.permission.CAMERA)

        val scenario = launcher.launch(CaptureSelfieActivity::class.java)
        assertThat(scenario.state, equalTo(Lifecycle.State.DESTROYED))
    }

    @Test
    fun whenAudioPermissionNotGranted_doesNotFinish() {
        permissionsChecker.deny(Manifest.permission.RECORD_AUDIO)

        val scenario = launcher.launch(CaptureSelfieActivity::class.java)
        assertThat(scenario.state, equalTo(Lifecycle.State.RESUMED))
    }

    @Test
    fun clickingPreview_takesPictureAndSavesToFileInPath() {
        val intent = Intent(application, CaptureSelfieActivity::class.java).also {
            it.putExtra(CaptureSelfieActivity.EXTRA_TMP_PATH, "blah")
        }

        launcher.launch<CaptureSelfieActivity>(intent)
        onView(withId(R.id.preview)).perform(click())
        assertThat(stillCamera.savedPath, equalTo("blah/tmp.jpg"))
    }

    @Test
    fun clickingPreview_finishesWithFilePath() {
        val intent = Intent(application, CaptureSelfieActivity::class.java).also {
            it.putExtra(CaptureSelfieActivity.EXTRA_TMP_PATH, "blah")
        }

        val scenario = launcher.launch<CaptureSelfieActivity>(intent)
        onView(withId(R.id.preview)).perform(click())

        assertThat(scenario.result.resultCode, equalTo(Activity.RESULT_OK))
        val returnedValue = ExternalAppUtils.getReturnedSingleValue(scenario.result.resultData)
        assertThat(returnedValue, equalTo("blah/tmp.jpg"))
    }

    @Test
    fun clickingPreview_whenThereIsAnErrorSavingImage_showsToast() {
        stillCamera.failToSave = true

        val intent = Intent(application, CaptureSelfieActivity::class.java).also {
            it.putExtra(CaptureSelfieActivity.EXTRA_TMP_PATH, "blah")
        }

        launcher.launch<CaptureSelfieActivity>(intent)
        onView(withId(R.id.preview)).perform(click())

        val latestToast = ShadowToast.getTextOfLatestToast()
        assertThat(latestToast, equalTo(application.getString(R.string.camera_error)))
    }

    @Test
    fun whenCameraFailsToInitialize_showsToast() {
        stillCamera.failToInitialize = true

        val intent = Intent(application, CaptureSelfieActivity::class.java).also {
            it.putExtra(CaptureSelfieActivity.EXTRA_TMP_PATH, "blah")
        }

        launcher.launch<CaptureSelfieActivity>(intent)
        val latestToast = ShadowToast.getTextOfLatestToast()
        assertThat(latestToast, equalTo(application.getString(R.string.camera_failed_to_initialize)))
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

private abstract class FakeCamera : Camera {

    var failToInitialize: Boolean = false

    protected val state = MutableNonNullLiveData(Camera.State.UNINITIALIZED)

    override fun initialize(activity: ComponentActivity, previewView: View) {
        if (failToInitialize) {
            state.value = Camera.State.FAILED_TO_INITIALIZE
        } else {
            state.value = Camera.State.INITIALIZED
        }
    }

    override fun state(): NonNullLiveData<Camera.State> {
        return state
    }
}

private class FakeStillCamera : FakeCamera(), StillCamera {

    var failToSave = false
    var savedPath: String? = null

    override fun takePicture(
        imagePath: String,
        onImageSaved: () -> Unit,
        onImageSaveError: () -> Unit,
    ) {
        if (state.value == Camera.State.UNINITIALIZED) {
            throw IllegalStateException()
        }

        savedPath = imagePath

        if (failToSave) {
            onImageSaveError()
        } else {
            onImageSaved()
        }
    }
}
