package org.odk.collect.android.activities

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ActivityScenario
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
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.support.RobolectricHelpers
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

        `when`(mainMenuViewModel.currentProject).thenReturn(currentProject)
        `when`(mainMenuViewModel.finalizedFormsCount).thenReturn(livedata)
        `when`(mainMenuViewModel.sentFormsCount).thenReturn(livedata)
        `when`(mainMenuViewModel.unsentFormsCount).thenReturn(livedata)

        RobolectricHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesMainMenuViewModel(versionInformation: VersionInformation, application: Application, settingsProvider: SettingsProvider, instancesCountRepository: InstancesCountRepository, currentProjectProvider: CurrentProjectProvider): MainMenuViewModel.Factory {
                return object : MainMenuViewModel.Factory(versionInformation, application, settingsProvider, instancesCountRepository, currentProjectProvider) {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return mainMenuViewModel as T
                    }
                }
            }
        })
    }

    @Test
    fun `Project icon for current project should be displayed`() {
        `when`(mainMenuViewModel.currentProject).thenReturn(Project("Project 1", "P", "#ffffff"))

        val scenario = ActivityScenario.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val projectIcon = activity.findViewById<TextView>(R.id.project_icon)

            assertThat(projectIcon.visibility, `is`(View.VISIBLE))
            assertThat(projectIcon.text, `is`("P"))

            val background = projectIcon.background as GradientDrawable
            assertThat(background.color!!.defaultColor, equalTo(Color.parseColor("#ffffff")))
        }
    }
}
