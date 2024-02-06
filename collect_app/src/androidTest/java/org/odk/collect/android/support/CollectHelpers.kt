package org.odk.collect.android.support

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.injection.config.AppDependencyComponent
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent
import org.odk.collect.projects.Project

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

    fun killApp() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressRecentApps()
        device
            .findObject(UiSelector().descriptionContains("Collect"))
            .swipeUp(10).also {
                simulateProcessRestart() // the process is not restarted automatically (probably to keep the test running) so we have simulate it
            }
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
}
