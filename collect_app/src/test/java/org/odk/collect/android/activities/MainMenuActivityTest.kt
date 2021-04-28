package org.odk.collect.android.activities

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.odk.collect.android.R
import org.odk.collect.android.activities.viewmodels.MainMenuViewModel
import org.odk.collect.android.formmanagement.InstancesCountRepository
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.support.RobolectricHelpers
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.projects.Project

@RunWith(AndroidJUnit4::class)
class MainMenuActivityTest {

    private lateinit var mainMenuViewModel: MainMenuViewModel
    private lateinit var currentProject: Project
    private lateinit var livedata: LiveData<Int>

    @Before
    fun setup() {
        mainMenuViewModel = mock(MainMenuViewModel::class.java)
        currentProject = mock(Project::class.java)
        livedata = mock(LiveData::class.java) as LiveData<Int>

        `when`(mainMenuViewModel.finalizedFormsCount).thenReturn(livedata)
        `when`(mainMenuViewModel.sentFormsCount).thenReturn(livedata)
        `when`(mainMenuViewModel.unsentFormsCount).thenReturn(livedata)

        RobolectricHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesMainMenuViewModel(versionInformation: VersionInformation, application: Application, settingsProvider: SettingsProvider, instancesCountRepository: InstancesCountRepository): MainMenuViewModel.Factory {
                return object : MainMenuViewModel.Factory(versionInformation, application, settingsProvider, instancesCountRepository) {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return mainMenuViewModel as T
                    }
                }
            }
        })
    }

    @Test
    fun `Project icon for current project should be displayed`() {
        val scenario = ActivityScenario.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val projectIcon = activity.findViewById<TextView>(R.id.project_icon_text)

            assertThat(projectIcon.visibility, `is`(View.VISIBLE))
            assertThat(projectIcon.text, `is`("P"))

            val background = projectIcon.background as GradientDrawable
            assertThat(background.color!!.defaultColor, equalTo(Color.parseColor("#ffffff")))
        }
    }

    @Test
    fun `Fill Blank Form button should have proper text`() {
        val scenario = ActivityScenario.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button: Button = activity.findViewById(R.id.enter_data)
            assertThat(button.text, `is`(activity.getString(R.string.enter_data_button)))
        }
    }

    @Test
    fun `Fill Blank Form button should start list of blank forms`() {
        val scenario = ActivityScenario.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button: Button = activity.findViewById(R.id.enter_data)
            button.performClick()
            assertThat(Intents.getIntents()[0], hasComponent(FillBlankFormActivity::class.java.name))

            Intents.release()
        }
    }

    @Test
    fun `Edit Saved Form button should have proper text`() {
        val scenario = ActivityScenario.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button: Button = activity.findViewById(R.id.review_data)
            assertThat(button.text, `is`(activity.getString(R.string.review_data_button)))
        }
    }

    @Test
    fun `Edit Saved Form button should start list of saved forms`() {
        val scenario = ActivityScenario.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button: Button = activity.findViewById(R.id.review_data)
            button.performClick()
            assertThat(Intents.getIntents()[0], hasComponent(InstanceChooserList::class.java.name))
            assertThat(Intents.getIntents()[0].extras!!.get(ApplicationConstants.BundleKeys.FORM_MODE), `is`(ApplicationConstants.FormModes.EDIT_SAVED))

            Intents.release()
        }
    }

    @Test
    fun `Send Finalized Form button should have proper text`() {
        val scenario = ActivityScenario.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button: Button = activity.findViewById(R.id.send_data)
            assertThat(button.text, `is`(activity.getString(R.string.send_data_button)))
        }
    }

    @Test
    fun `Send Finalized Form button should start list of finalized forms`() {
        val scenario = ActivityScenario.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button: Button = activity.findViewById(R.id.send_data)
            button.performClick()
            assertThat(Intents.getIntents()[0], hasComponent(InstanceUploaderListActivity::class.java.name))

            Intents.release()
        }
    }

    @Test
    fun `View Sent Form button should have proper text`() {
        val scenario = ActivityScenario.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button: Button = activity.findViewById(R.id.view_sent_forms)
            assertThat(button.text, `is`(activity.getString(R.string.view_sent_forms)))
        }
    }

    @Test
    fun `View Sent Form button should start list of sent forms`() {
        val scenario = ActivityScenario.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button: Button = activity.findViewById(R.id.view_sent_forms)
            button.performClick()
            assertThat(Intents.getIntents()[0], hasComponent(InstanceChooserList::class.java.name))
            assertThat(Intents.getIntents()[0].extras!!.get(ApplicationConstants.BundleKeys.FORM_MODE), `is`(ApplicationConstants.FormModes.VIEW_SENT))

            Intents.release()
        }
    }

    @Test
    fun `Get Blank Form button should have proper text`() {
        val scenario = ActivityScenario.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button: Button = activity.findViewById(R.id.get_forms)
            assertThat(button.text, `is`(activity.getString(R.string.get_forms)))
        }
    }

    @Test
    fun `Get Blank Form button should start list of forms to download`() {
        val scenario = ActivityScenario.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button: Button = activity.findViewById(R.id.get_forms)
            button.performClick()
            assertThat(Intents.getIntents()[0], hasComponent(FormDownloadListActivity::class.java.name))

            Intents.release()
        }
    }

    @Test
    fun `Delete Saved Form button should have proper text`() {
        val scenario = ActivityScenario.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button: Button = activity.findViewById(R.id.manage_forms)
            assertThat(button.text, `is`(activity.getString(R.string.manage_files)))
        }
    }

    @Test
    fun `Delete Saved Form button should start list of forms to delete`() {
        val scenario = ActivityScenario.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button: Button = activity.findViewById(R.id.manage_forms)
            button.performClick()
            assertThat(Intents.getIntents()[0], hasComponent(DeleteSavedFormActivity::class.java.name))

            Intents.release()
        }
    }
}
