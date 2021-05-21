package org.odk.collect.android.activities.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.projects.Project

class CurrentProjectViewModel(private val currentProjectProvider: CurrentProjectProvider) :
    ViewModel() {

    private val _currentProject = MutableNonNullLiveData(currentProjectProvider.getCurrentProject())
    val currentProject: NonNullLiveData<Project.Saved> = _currentProject

    fun setCurrentProject(project: Project.Saved) {
        currentProjectProvider.setCurrentProject(project.uuid)
        refresh()
    }

    fun refresh() {
        if (currentProject.value != currentProjectProvider.getCurrentProject()) {
            _currentProject.postValue(currentProjectProvider.getCurrentProject())
        }
    }

    open class Factory constructor(private val currentProjectProvider: CurrentProjectProvider) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CurrentProjectViewModel(currentProjectProvider) as T
        }
    }
}
