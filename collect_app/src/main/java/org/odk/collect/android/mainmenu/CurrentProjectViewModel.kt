package org.odk.collect.android.mainmenu

import androidx.lifecycle.ViewModel
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.projects.Project

class CurrentProjectViewModel(
    private val currentProjectProvider: CurrentProjectProvider
) : ViewModel() {

    private val _currentProject by lazy { MutableNonNullLiveData(currentProjectProvider.getCurrentProject()) }
    val currentProject: NonNullLiveData<Project.Saved> by lazy { _currentProject }

    fun setCurrentProject(project: Project.Saved) {
        Analytics.log(AnalyticsEvents.SWITCH_PROJECT)
        currentProjectProvider.setCurrentProject(project.uuid)
        refresh()
    }

    fun refresh() {
        if (currentProject.value != currentProjectProvider.getCurrentProject()) {
            _currentProject.postValue(currentProjectProvider.getCurrentProject())
        }
    }

    fun hasCurrentProject(): Boolean {
        return try {
            currentProjectProvider.getCurrentProject()
            true
        } catch (e: IllegalStateException) {
            false
        }
    }
}
