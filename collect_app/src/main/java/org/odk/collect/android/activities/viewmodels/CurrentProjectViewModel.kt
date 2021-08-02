package org.odk.collect.android.activities.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.application.initialization.AnalyticsInitializer
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import java.io.File

class CurrentProjectViewModel(
    private val currentProjectProvider: CurrentProjectProvider,
    private val analyticsInitializer: AnalyticsInitializer,
    private val storagePathProvider: StoragePathProvider,
    private val projectsRepository: ProjectsRepository
) : ViewModel() {

    private val _currentProject = MutableNonNullLiveData(currentProjectProvider.getCurrentProject())
    val currentProject: NonNullLiveData<Project.Saved> = _currentProject

    fun setCurrentProject(project: Project.Saved) {
        currentProjectProvider.setCurrentProject(project.uuid)
        Analytics.log(AnalyticsEvents.SWITCH_PROJECT)
        analyticsInitializer.initialize()
        updateCurrentProjectIfNeeded()
    }

    @Throws(NoCurrentProjectException::class)
    fun refresh() {
        updateCurrentProjectIfNeeded()

        if (File(storagePathProvider.odkRootDirPath).listFiles().isEmpty()) {
            currentProjectProvider.clear()
            projectsRepository.deleteAll()
            throw NoCurrentProjectException()
        }
    }

    private fun updateCurrentProjectIfNeeded() {
        if (currentProject.value != currentProjectProvider.getCurrentProject()) {
            _currentProject.postValue(currentProjectProvider.getCurrentProject())
        }
    }

    open class Factory(
        private val currentProjectProvider: CurrentProjectProvider,
        private val analyticsInitializer: AnalyticsInitializer,
        private val storagePathProvider: StoragePathProvider,
        private val projectsRepository: ProjectsRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CurrentProjectViewModel(
                currentProjectProvider,
                analyticsInitializer,
                storagePathProvider,
                projectsRepository
            ) as T
        }
    }

    class NoCurrentProjectException : Exception()
}
