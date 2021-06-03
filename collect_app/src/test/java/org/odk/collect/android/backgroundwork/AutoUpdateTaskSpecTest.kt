package org.odk.collect.android.backgroundwork

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.backgroundwork.AutoUpdateTaskSpec.DATA_PROJECT_ID
import org.odk.collect.android.formmanagement.FormSourceProvider
import org.odk.collect.android.formmanagement.FormUpdateChecker
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.FormsRepositoryProvider

@RunWith(AndroidJUnit4::class)
class AutoUpdateTaskSpecTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val formUpdateChecker = mock<FormUpdateChecker>()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesFormUpdateChecker(
                context: Context,
                notifier: Notifier,
                analytics: Analytics,
                changeLock: ChangeLock,
                storagePathProvider: StoragePathProvider,
                settingsProvider: SettingsProvider,
                formsRepositoryProvider: FormsRepositoryProvider,
                formSourceProvider: FormSourceProvider
            ): FormUpdateChecker {
                return formUpdateChecker
            }
        })
    }

    @Test
    fun `calls checkForUpdates with project from tag`() {
        val autoUpdateTaskSpec = AutoUpdateTaskSpec()
        val task = autoUpdateTaskSpec.getTask(context, mapOf(DATA_PROJECT_ID to "projectId"))

        task.get()
        verify(formUpdateChecker).downloadUpdates("projectId")
    }
}
