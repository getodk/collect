package org.odk.collect.android.mainmenu

import androidx.lifecycle.ViewModel
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.projects.Project

class CurrentProjectViewModel(
    private val projectsDataService: ProjectsDataService
) : ViewModel() {

    private val _currentProject by lazy { MutableNonNullLiveData(projectsDataService.getCurrentProject()) }
    val currentProject: NonNullLiveData<Project.Saved> by lazy { _currentProject }

    fun setCurrentProject(project: Project.Saved) {
        Analytics.log(AnalyticsEvents.SWITCH_PROJECT)
        projectsDataService.setCurrentProject(project.uuid)
        refresh()
    }

    fun refresh() {
        if (currentProject.value != projectsDataService.getCurrentProject()) {
            _currentProject.postValue(projectsDataService.getCurrentProject())
        }
    }

    fun hasCurrentProject(): Boolean {
        return try {
            projectsDataService.getCurrentProject()
            true
        } catch (e: IllegalStateException) {
            false
        }
    }
}
