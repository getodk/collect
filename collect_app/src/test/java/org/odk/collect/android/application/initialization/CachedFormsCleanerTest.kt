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
import org.odk.collect.projects.Project
import java.io.File

@RunWith(AndroidJUnit4::class)
class CachedFormsCleanerTest {
    private val component = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Context>() as Application)
    private val projectsRepository = component.projectsRepository()
    private val projectDependencyModuleFactory = component.projectDependencyModuleFactory()

    private val cachedFormsCleaner = CachedFormsCleaner(projectsRepository, projectDependencyModuleFactory)

    @Test
    fun `cleaner should be run on every app upgrade`() {
        assertThat(cachedFormsCleaner.key(), equalTo(null))
    }

    @Test
    fun `cached forms should be removed on app upgrade from every project`() {
        projectsRepository.save(Project.Saved("1", "Project 1", "1", "#000000"))
        projectsRepository.save(Project.Saved("2", "Project 2", "2", "#000000"))

        val project1DependencyModule = projectDependencyModuleFactory.create("1").also {
            File(it.cacheDir, "file1.formdef").createNewFile()
            File(it.cacheDir, "file2.formdef").createNewFile()
            File(it.cacheDir, "file3.save").createNewFile()
            File(it.cacheDir, "file4.txt").createNewFile()
        }
        val project2DependencyModule = projectDependencyModuleFactory.create("2").also {
            File(it.cacheDir, "file1.formdef").createNewFile()
            File(it.cacheDir, "file2.formdef").createNewFile()
            File(it.cacheDir, "file3.save").createNewFile()
            File(it.cacheDir, "file4.txt").createNewFile()
        }

        cachedFormsCleaner.run()

        val cachedFilesProject1 = File(project1DependencyModule.cacheDir).listFiles()
        val cachedFilesProject2 = File(project2DependencyModule.cacheDir).listFiles()

        assertThat(cachedFilesProject1.size, equalTo(2))
        assertThat(cachedFilesProject1.map { it.name }.toList(), containsInAnyOrder("file3.save", "file4.txt"))

        assertThat(cachedFilesProject2.size, equalTo(2))
        assertThat(cachedFilesProject2.map { it.name }.toList(), containsInAnyOrder("file3.save", "file4.txt"))
    }
}
