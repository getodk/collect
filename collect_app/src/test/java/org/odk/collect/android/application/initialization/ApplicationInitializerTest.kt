package org.odk.collect.android.application.initialization

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.verify
import org.odk.collect.android.TestSettingsProvider.getMetaSettings
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.projects.ProjectImporter
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.AppStateProvider
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ApplicationInitializerTest {
    val appStateProvider: AppStateProvider = mock(AppStateProvider::class.java)
    val projectImporter: ProjectImporter = mock(ProjectImporter::class.java)
    val currentProjectProvider: CurrentProjectProvider = mock(CurrentProjectProvider::class.java)

    lateinit var applicationInitializer: ApplicationInitializer

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesAppStateProvider(
                context: Context,
                settingsProvider: SettingsProvider
            ): AppStateProvider {
                return appStateProvider
            }

            override fun providesProjectImporter(
                projectsRepository: ProjectsRepository,
                storagePathProvider: StoragePathProvider,
                context: Context,
                settingsProvider: SettingsProvider
            ): ProjectImporter? {
                return projectImporter
            }

            override fun providesCurrentProjectProvider(
                settingsProvider: SettingsProvider?,
                projectsRepository: ProjectsRepository?
            ): CurrentProjectProvider {
                return currentProjectProvider
            }
        })

        applicationInitializer =
            DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Collect>())
                .applicationInitializer()
    }

    @Test
    fun `Should existing project be imported when it's not first launch and it has not been already imported`() {
        `when`(appStateProvider.isFreshInstall()).thenReturn(false)
        getMetaSettings().save(MetaKeys.EXISTING_PROJECT_IMPORTED, false)

        val existing = Project.Saved("123", "Existing", "E", "#ffffff")
        `when`(projectImporter.importExistingProject()).thenReturn(existing)
        applicationInitializer.initialize()
        verify(currentProjectProvider).setCurrentProject(existing.uuid)
    }

    @Test
    fun `Should not existing project be imported when it's first launch`() {
        `when`(appStateProvider.isFreshInstall()).thenReturn(true)
        applicationInitializer.initialize()
        verifyNoInteractions(projectImporter)
    }

    @Test
    fun `Should not existing project be imported when it was already imported once`() {
        `when`(appStateProvider.isFreshInstall()).thenReturn(false)
        getMetaSettings().save(MetaKeys.EXISTING_PROJECT_IMPORTED, true)
        applicationInitializer.initialize()
        verifyNoInteractions(projectImporter)
    }

    @Test
    fun `Initializing the app should set ALREADY_TRIED_TO_IMPORT_EXISTING_PROJECT flag to true`() {
        getMetaSettings().save(MetaKeys.EXISTING_PROJECT_IMPORTED, false)

        val existing = Project.Saved("123", "Existing", "E", "#ffffff")
        `when`(projectImporter.importExistingProject()).thenReturn(existing)

        applicationInitializer.initialize()
        assertThat(getMetaSettings().getBoolean(MetaKeys.EXISTING_PROJECT_IMPORTED), equalTo(true))
    }
}
