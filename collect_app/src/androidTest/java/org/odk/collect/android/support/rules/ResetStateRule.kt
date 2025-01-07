package org.odk.collect.android.support.rules

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.injection.config.AppDependencyComponent
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.views.DecoratedBarcodeView
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import org.odk.collect.db.sqlite.DatabaseConnection
import org.odk.collect.material.BottomSheetBehavior
import java.io.IOException

private class ResetStateStatement(
    private val base: Statement,
    private val appDependencyModule: AppDependencyModule? = null
) : Statement() {

    override fun evaluate() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val oldComponent = DaggerUtils.getComponent(application)

        clearPrefs(oldComponent)
        clearDisk()
        setTestState()
        CollectHelpers.simulateProcessRestart(appDependencyModule)
        base.evaluate()
    }

    private fun setTestState() {
        MultiClickGuard.test = true
        DecoratedBarcodeView.test = true
        ToastUtils.recordToasts = true
        BottomSheetBehavior.DRAGGING_ENABLED = false
    }

    private fun clearDisk() {
        try {
            val internalFilesDir = ApplicationProvider.getApplicationContext<Application>().filesDir
            internalFilesDir.deleteRecursively()

            val externalFilesDir =
                ApplicationProvider.getApplicationContext<Application>().getExternalFilesDir(null)!!
            externalFilesDir.deleteRecursively()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        DatabaseConnection.closeAll()
    }

    private fun clearPrefs(component: AppDependencyComponent) {
        val projectIds = component.projectsRepository().getAll().map { it.uuid }
        component.settingsProvider().clearAll(projectIds)
    }
}

class ResetStateRule @JvmOverloads constructor(
    private val appDependencyModule: AppDependencyModule? = null
) : TestRule {

    override fun apply(base: Statement, description: Description): Statement =
        ResetStateStatement(base, appDependencyModule)
}
