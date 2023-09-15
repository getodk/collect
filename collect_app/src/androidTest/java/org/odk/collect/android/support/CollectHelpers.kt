package org.odk.collect.android.support

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.injection.config.AppDependencyComponent
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent
import org.odk.collect.projects.Project
import org.odk.collect.settings.keys.ProjectKeys

object CollectHelpers {
    fun overrideAppDependencyModule(appDependencyModule: AppDependencyModule): AppDependencyComponent {
        val application = ApplicationProvider.getApplicationContext<Collect>()
        val testComponent = DaggerAppDependencyComponent.builder()
            .application(application)
            .appDependencyModule(appDependencyModule)
            .build()
        application.component = testComponent
        return testComponent
    }

    fun simulateProcessRestart(appDependencyModule: AppDependencyModule? = null) {
        ApplicationProvider.getApplicationContext<Collect>().getState().clear()

        val newComponent =
            overrideAppDependencyModule(appDependencyModule ?: AppDependencyModule())

        // Reinitialize any application state with new deps/state
        newComponent.applicationInitializer().initialize()
    }

    @JvmStatic
    fun addDemoProject() {
        val component =
            DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>())
        component.projectsRepository().save(Project.DEMO_PROJECT)
        component.currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID)
    }

    @JvmStatic
    fun addGDProject(gdProject: Project.New, accountName: String, testDependencies: TestDependencies) {
        testDependencies.googleAccountPicker.setDeviceAccount(accountName)
        testDependencies.googleApi.setAccount(accountName)

        val project = DaggerUtils
            .getComponent(ApplicationProvider.getApplicationContext<Application>())
            .projectsRepository()
            .save(gdProject)

        DaggerUtils
            .getComponent(ApplicationProvider.getApplicationContext<Application>())
            .settingsProvider().getUnprotectedSettings(project.uuid)
            .also {
                it.save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
                it.save(ProjectKeys.KEY_SELECTED_GOOGLE_ACCOUNT, accountName)
            }
    }
}
