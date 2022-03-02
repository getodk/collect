package org.odk.collect.android.support.rules

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.apache.commons.io.FileUtils
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.odk.collect.android.TestSettingsProvider.getSettingsProvider
import org.odk.collect.android.database.DatabaseConnection.Companion.closeAll
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.views.DecoratedBarcodeView
import org.odk.collect.androidshared.data.getState
import org.odk.collect.androidshared.ui.ToastUtils.recordToasts
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import java.io.File
import java.io.IOException

private class ResetStateStatement(
    private val base: Statement,
    private val appDependencyModule: AppDependencyModule? = null
) : Statement() {

    private val settingsProvider = getSettingsProvider()

    override fun evaluate() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        resetDagger()
        clearPrefs()
        clearDisk()
        clearAppState(application)
        setTestState()
        val component = DaggerUtils.getComponent(application.applicationContext)

        // Reinitialize any application state with new deps/state
        component.applicationInitializer().initialize()
        base.evaluate()
    }

    private fun clearAppState(application: Application) {
        application.getState().clear()
    }

    private fun setTestState() {
        MultiClickGuard.test = true
        DecoratedBarcodeView.test = true
        recordToasts = true
    }

    private fun clearDisk() {
        try {
            val storagePathProvider = StoragePathProvider()
            FileUtils.deleteDirectory(File(storagePathProvider.odkRootDirPath))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        closeAll()
    }

    private fun resetDagger() {
        CollectHelpers.overrideAppDependencyModule(appDependencyModule ?: AppDependencyModule())
    }

    private fun clearPrefs() {
        settingsProvider.clearAll()
    }
}

class ResetStateRule @JvmOverloads constructor(
    private val appDependencyModule: AppDependencyModule? = null
) : TestRule {

    override fun apply(base: Statement, description: Description): Statement =
        ResetStateStatement(base, appDependencyModule)
}
