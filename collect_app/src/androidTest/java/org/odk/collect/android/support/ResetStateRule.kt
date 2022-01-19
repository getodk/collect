package org.odk.collect.android.support

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
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
import org.odk.collect.android.utilities.MultiClickGuard
import org.odk.collect.android.views.DecoratedBarcodeView
import org.odk.collect.androidshared.data.getState
import org.odk.collect.androidshared.ui.ToastUtils.recordToasts
import java.io.File
import java.io.IOException

class ResetStateRule @JvmOverloads constructor(private val appDependencyModule: AppDependencyModule? = null) : TestRule {

    private val settingsProvider = getSettingsProvider()

    override fun apply(base: Statement, description: Description): Statement = ResetStateStatement(base)

    private inner class ResetStateStatement(private val base: Statement) : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            val application = ApplicationProvider.getApplicationContext<Application>()
            resetDagger()
            clearPrefs(application)
            clearDisk()
            clearAppState(application)
            setTestState()
            val component = DaggerUtils.getComponent(application.applicationContext)

            // Reinitialize any application state with new deps/state
            component.applicationInitializer().initialize()
            base.evaluate()
        }
    }

    private fun clearAppState(application: Application) {
        application.getState().clear()
    }

    private fun setTestState() {
        MultiClickGuard.test = true
        DecoratedBarcodeView.test = true
        CollectTestRule.projectCreated = false
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

    private fun clearPrefs(context: Context) {
        settingsProvider.clearAll()

        // Delete legacy prefs in case older version of app was run on test device
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply()
        context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }
}