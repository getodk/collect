package org.odk.collect.android.entities

import android.app.Application
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.entities.EntitiesRepository
import java.io.File

class EntitiesRepositoryProvider(private val application: Application, private val projectsDataService: ProjectsDataService) {

    fun get(projectId: String = projectsDataService.getCurrentProject().uuid): EntitiesRepository {
        val projectDir = File(application.filesDir, projectId)
        return JsonFileEntitiesRepository(projectDir)
    }
}
