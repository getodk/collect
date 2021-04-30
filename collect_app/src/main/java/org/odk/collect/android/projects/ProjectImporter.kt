package org.odk.collect.android.projects

import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.Settings

class ProjectImporter(
    private val projectsRepository: ProjectsRepository,
    private val metaSettings: Settings
) {
    fun importDemoProject() {
        val project = Project("Demo project", "D", "#3e9fcc", DEMO_PROJECT_ID)
        projectsRepository.save(project)
        metaSettings.save(MetaKeys.CURRENT_PROJECT_ID, DEMO_PROJECT_ID)
    }

    // Now it does the same like importDemoProject() but it should be changed later
    fun importExistingProject() {
        val project = Project("Demo project", "D", "#3e9fcc", DEMO_PROJECT_ID)
        projectsRepository.save(project)
        metaSettings.save(MetaKeys.CURRENT_PROJECT_ID, DEMO_PROJECT_ID)
    }

    companion object {
        /*
        Should be empty to easily access existed settings (general and admin) that had be saved in versions
        prior to the one that implemented "Projects" and treat them as those which belong to
        the imported existed project.
         */
        const val DEMO_PROJECT_ID = ""
    }
}
