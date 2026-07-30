package org.odk.collect.draw

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.bitmap.ImageFileUtils
import org.odk.collect.androidtest.ActivityScenarioExtensions.isFinishing
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.async.Scheduler
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.shared.TempFiles
import org.odk.collect.strings.R
import org.odk.collect.testshared.EspressoInteractions
import org.odk.collect.testshared.FakeScheduler
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
internal class DrawActivityTest {

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    private val application: RobolectricApplication by lazy { getApplicationContext() }
    private val scheduler = FakeScheduler()
    private val imagePath = TempFiles.createTempFile().absolutePath

    @Before
    fun setup() {
        application.setupDependencies(
            object : DrawDependencyModule() {
                override fun providesScheduler(): Scheduler {
                    return scheduler
                }

                override fun providesSettingsProvider(): SettingsProvider {
                    return InMemSettingsProvider()
                }

                override fun providesImagePath(): String {
                    return imagePath
                }
            }
        )
    }

    @Test
    fun `discarding changes closes the activity with canceled result`() {
        val intent = Intent(getApplicationContext(), DrawActivity::class.java)

        intent.putExtra(DrawActivity.SCREEN_ORIENTATION, 0)
        val scenario = launcherRule.launchForResult<DrawActivity>(intent)

        Espresso.pressBack()
        EspressoInteractions.clickOn(withText(R.string.discard_changes), isDialog())
        assertThat(scenario.isFinishing, equalTo(true))
        assertThat(scenario.result.resultCode, equalTo(Activity.RESULT_CANCELED))
    }

    @Test
    fun `choosing to keep editing does not close the activity`() {
        val intent = Intent(getApplicationContext(), DrawActivity::class.java)

        intent.putExtra(DrawActivity.SCREEN_ORIENTATION, 0)
        val scenario = launcherRule.launchForResult<DrawActivity>(intent)

        Espresso.pressBack()
        EspressoInteractions.clickOn(withText(R.string.keep_editing), isDialog())
        assertThat(scenario.isFinishing, equalTo(false))
    }

    @Test
    fun `saving changes closes the activity with ok result`() {
        val intent = Intent(getApplicationContext(), DrawActivity::class.java)

        intent.putExtra(DrawActivity.SCREEN_ORIENTATION, 0)
        val scenario = launcherRule.launchForResult<DrawActivity>(intent)

        Espresso.pressBack()
        EspressoInteractions.clickOn(withText(R.string.keep_changes), isDialog())
        scheduler.flush()
        assertThat(scenario.isFinishing, equalTo(true))
        assertThat(scenario.result.resultCode, equalTo(Activity.RESULT_OK))
    }

    @Test
    @Config(qualifiers = "w400dp-h700dp-mdpi")
    fun `saved image keeps the original image resolution when annotating`() {
        val originalImage = Bitmap.createBitmap(800, 1400, Bitmap.Config.ARGB_8888)
        val originalImageFile = TempFiles.createTempFile(".png")
        ImageFileUtils.saveBitmapToFile(originalImage, originalImageFile.absolutePath)

        val intent = Intent(getApplicationContext(), DrawActivity::class.java).apply {
            putExtra(DrawActivity.SCREEN_ORIENTATION, 1)
            putExtra(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE)
            putExtra(DrawActivity.REF_IMAGE, Uri.fromFile(originalImageFile))
        }
        launcherRule.launch<DrawActivity>(intent)

        Espresso.pressBack()
        EspressoInteractions.clickOn(withText(R.string.keep_changes), isDialog())
        scheduler.flush()

        val savedImage = BitmapFactory.decodeFile(imagePath)
        assertThat(savedImage.width, equalTo(originalImage.width))
        assertThat(savedImage.height, equalTo(originalImage.height))
    }

    @Test
    @Config(qualifiers = "w400dp-h700dp-mdpi")
    fun `saved image is downscaled when annotating an image that is too big to keep in memory`() {
        val originalImage = Bitmap.createBitmap(100, 6000, Bitmap.Config.ARGB_8888)
        val originalImageFile = TempFiles.createTempFile(".png")
        ImageFileUtils.saveBitmapToFile(originalImage, originalImageFile.absolutePath)

        val intent = Intent(getApplicationContext(), DrawActivity::class.java).apply {
            putExtra(DrawActivity.SCREEN_ORIENTATION, 1)
            putExtra(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE)
            putExtra(DrawActivity.REF_IMAGE, Uri.fromFile(originalImageFile))
        }
        launcherRule.launch<DrawActivity>(intent)

        Espresso.pressBack()
        EspressoInteractions.clickOn(withText(R.string.keep_changes), isDialog())
        scheduler.flush()

        val savedImage = BitmapFactory.decodeFile(imagePath)
        assertThat(savedImage.width, equalTo(50))
        assertThat(savedImage.height, equalTo(3000))
    }
}
