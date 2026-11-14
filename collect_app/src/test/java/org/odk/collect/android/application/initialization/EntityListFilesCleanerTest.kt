package org.odk.collect.android.application.initialization

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.ProjectDependencyModule
import org.odk.collect.android.utilities.FormUtils.getMediaFiles
import org.odk.collect.forms.Form
import org.odk.collect.formstest.FormUtils
import org.odk.collect.projects.Project
import org.odk.collect.settings.keys.MetaKeys
import java.io.File

@RunWith(AndroidJUnit4::class)
class EntityListFilesCleanerTest {
    private val component = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Context>() as Application)
    private val projectsRepository = component.projectsRepository()
    private val projectDependencyModuleFactory = component.projectDependencyModuleFactory()

    private val entityListFilesCleaner = EntityListFilesCleaner(projectsRepository, projectDependencyModuleFactory)

    @Test
    fun `cleaner should be run only once`() {
        assertThat(entityListFilesCleaner.key(), equalTo(MetaKeys.ENTITY_LIST_FILES_CLEANED))
    }

    @Test
    fun `entity list files should be removed from forms that use entities in every project`() {
        projectsRepository.save(Project.Saved("1", "Project 1", "1", "#000000"))
        projectsRepository.save(Project.Saved("2", "Project 2", "2", "#000000"))

        val project1 = projectDependencyModuleFactory.create("1")
        project1.entitiesRepository.addList("people")
        val form1 = createForm(project1, usesEntities = true, "people.csv", "other.csv", "image.png")

        val project2 = projectDependencyModuleFactory.create("2")
        project2.entitiesRepository.addList("trees")
        val form2 = createForm(project2, usesEntities = true, "trees.csv", "people.csv")

        entityListFilesCleaner.run()

        assertThat(getMediaFiles(form1).map { it.name }, containsInAnyOrder("other.csv", "image.png"))
        assertThat(getMediaFiles(form2).map { it.name }, containsInAnyOrder("people.csv"))
    }

    @Test
    fun `files should not be removed from forms that do not use entities`() {
        projectsRepository.save(Project.Saved("1", "Project 1", "1", "#000000"))

        val project = projectDependencyModuleFactory.create("1")
        project.entitiesRepository.addList("people")
        val form = createForm(project, usesEntities = false, "people.csv")

        entityListFilesCleaner.run()

        assertThat(getMediaFiles(form).map { it.name }, containsInAnyOrder("people.csv"))
    }

    private fun createForm(
        projectDependencyModule: ProjectDependencyModule,
        usesEntities: Boolean,
        vararg mediaFiles: String
    ): Form {
        val form = projectDependencyModule.formsRepository.save(
            FormUtils.buildForm("formId", "1", projectDependencyModule.formsDir, usesEntities = usesEntities).build()
        )

        File(form.formMediaPath).mkdirs()
        mediaFiles.forEach { File(form.formMediaPath, it).createNewFile() }
        return form
    }
}
