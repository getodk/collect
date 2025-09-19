package org.odk.collect.draw

import android.app.Activity
import android.content.Intent
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
import org.odk.collect.androidtest.ActivityScenarioExtensions.isFinishing
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.async.Scheduler
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.shared.TempFiles
import org.odk.collect.strings.R
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.Interactions

@RunWith(AndroidJUnit4::class)
internal class DrawActivityTest {

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    private val application: RobolectricApplication by lazy { getApplicationContext() }
    private val scheduler = FakeScheduler()

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
                    return TempFiles.createTempFile().absolutePath
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
        Interactions.clickOn(withText(R.string.discard_changes), isDialog())
        assertThat(scenario.isFinishing, equalTo(true))
        assertThat(scenario.result.resultCode, equalTo(Activity.RESULT_CANCELED))
    }

    @Test
    fun `choosing to keep editing does not close the activity`() {
        val intent = Intent(getApplicationContext(), DrawActivity::class.java)

        intent.putExtra(DrawActivity.SCREEN_ORIENTATION, 0)
        val scenario = launcherRule.launchForResult<DrawActivity>(intent)

        Espresso.pressBack()
        Interactions.clickOn(withText(R.string.keep_editing), isDialog())
        assertThat(scenario.isFinishing, equalTo(false))
    }

    @Test
    fun `saving changes closes the activity with ok result`() {
        val intent = Intent(getApplicationContext(), DrawActivity::class.java)

        intent.putExtra(DrawActivity.SCREEN_ORIENTATION, 0)
        val scenario = launcherRule.launchForResult<DrawActivity>(intent)

        Espresso.pressBack()
        Interactions.clickOn(withText(R.string.keep_changes), isDialog())
        scheduler.flush()
        assertThat(scenario.isFinishing, equalTo(true))
        assertThat(scenario.result.resultCode, equalTo(Activity.RESULT_OK))
    }
}
