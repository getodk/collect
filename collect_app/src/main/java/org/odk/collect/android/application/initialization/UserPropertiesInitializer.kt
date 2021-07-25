package org.odk.collect.android.application.initialization

import org.odk.collect.analytics.Analytics
import org.odk.collect.projects.ProjectsRepository

class UserPropertiesInitializer(private val analytics: Analytics, private val projectsRepository: ProjectsRepository) {

    fun initialize() {
        val projectsCount = projectsRepository.getAll().size
        analytics.setUserProperty("ProjectsCount", projectsCount.toString())
    }
}
