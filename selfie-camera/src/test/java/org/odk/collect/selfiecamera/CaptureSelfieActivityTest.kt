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
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.externalapp.ExternalAppUtils
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.selfiecamera.support.RobolectricApplication
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
class CaptureSelfieActivityTest {

    private val application = ApplicationProvider.getApplicationContext<RobolectricApplication>()
    private val permissionsChecker = FakePermissionsChecker()
    private val camera = FakeCamera()

    @get:Rule
    val launcher = ActivityScenarioLauncherRule()

    @Before
    fun setup() {
        application.testObjectProvider.also {
            it.addSupplier(PermissionsChecker::class.java) { permissionsChecker }
            it.addSupplier(Camera::class.java) { camera }
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

    @Test
    fun clickingPreview_takesPictureAndSavesToFileInPath() {
        val intent = Intent(application, CaptureSelfieActivity::class.java).also {
            it.putExtra(CaptureSelfieActivity.EXTRA_TMP_PATH, "blah")
        }

        launcher.launch<CaptureSelfieActivity>(intent)
        onView(withId(R.id.preview)).perform(click())
        assertThat(camera.savedPath, equalTo("blah/tmp.jpg"))
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
        camera.failToSave = true

        val intent = Intent(application, CaptureSelfieActivity::class.java).also {
            it.putExtra(CaptureSelfieActivity.EXTRA_TMP_PATH, "blah")
        }

        launcher.launch<CaptureSelfieActivity>(intent)
        onView(withId(R.id.preview)).perform(click())

        val latestToast = ShadowToast.getTextOfLatestToast()
        assertThat(latestToast, equalTo(application.getString(R.string.camera_error)))
    }

    @Test
    fun whenTakingVideo_clickingPreview_startsRecordingToFileInPath() {
        val intent = Intent(application, CaptureSelfieActivity::class.java).also {
            it.putExtra(CaptureSelfieActivity.EXTRA_TMP_PATH, "blah")
            it.putExtra(CaptureSelfieActivity.EXTRA_VIDEO, true)
        }

        launcher.launch<CaptureSelfieActivity>(intent)
        onView(withId(R.id.preview)).perform(click())

        assertThat(camera.isRecording(), equalTo(true))
        assertThat(camera.savedPath, equalTo("blah/tmp.mp4"))
    }

    @Test
    fun whenTakingVideo_whenVideoIsSaved_finishesWithPath() {
        val intent = Intent(application, CaptureSelfieActivity::class.java).also {
            it.putExtra(CaptureSelfieActivity.EXTRA_TMP_PATH, "blah")
            it.putExtra(CaptureSelfieActivity.EXTRA_VIDEO, true)
        }

        val scenario = launcher.launch<CaptureSelfieActivity>(intent)
        onView(withId(R.id.preview)).perform(click())
        onView(withId(R.id.preview)).perform(click())
        camera.finalizeVideo()

        assertThat(scenario.result.resultCode, equalTo(Activity.RESULT_OK))
        val returnedValue = ExternalAppUtils.getReturnedSingleValue(scenario.result.resultData)
        assertThat(returnedValue, equalTo("blah/tmp.mp4"))
    }

    @Test
    fun whenTakingVideo_whenErrorOccursSavingVideo_showsToast() {
        camera.failToSave = true

        val intent = Intent(application, CaptureSelfieActivity::class.java).also {
            it.putExtra(CaptureSelfieActivity.EXTRA_TMP_PATH, "blah")
            it.putExtra(CaptureSelfieActivity.EXTRA_VIDEO, true)
        }

        launcher.launch<CaptureSelfieActivity>(intent)
        onView(withId(R.id.preview)).perform(click())
        onView(withId(R.id.preview)).perform(click())
        camera.finalizeVideo()

        val latestToast = ShadowToast.getTextOfLatestToast()
        assertThat(latestToast, equalTo(application.getString(R.string.camera_error)))
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

private class FakeCamera : Camera {

    var failToSave = false
    var savedPath: String? = null
    var recording = false

    private var initialized = false
    private var onVideoSaved: (() -> Unit)? = null
    private var onVideoSaveError: (() -> Unit)? = null

    override fun initialize(activity: ComponentActivity, previewView: View) {
        initialized = true
    }

    override fun takePicture(
        imagePath: String,
        onImageSaved: () -> Unit,
        onImageSaveError: () -> Unit,
    ) {
        if (!initialized) {
            throw IllegalStateException()
        }

        savedPath = imagePath

        if (failToSave) {
            onImageSaveError()
        } else {
            onImageSaved()
        }
    }

    override fun startVideo(
        videoPath: String,
        onVideoSaved: () -> Unit,
        onVideoSaveError: () -> Unit,
    ) {
        recording = true
        savedPath = videoPath
        this.onVideoSaved = onVideoSaved
        this.onVideoSaveError = onVideoSaveError
    }

    override fun stopVideo() {
        recording = false
    }

    override fun isRecording(): Boolean {
        return recording
    }

    fun finalizeVideo() {
        if (recording) {
            throw IllegalStateException()
        }

        if (failToSave) {
            onVideoSaveError?.invoke()
        } else {
            onVideoSaved?.invoke()
        }
    }
}
