package org.odk.collect.android.formmanagement

import android.app.Application
import android.database.ContentObserver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.provider.FormsProviderAPI.CONTENT_URI
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.BooleanChangeLock
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.forms.FormSource

@RunWith(AndroidJUnit4::class)
class FormUpdateCheckerTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var updateChecker: FormUpdateChecker

    @Before
    fun setup() {
        val storagePathProvider = StoragePathProvider()

        val formSource = mock<FormSource> {
            on { fetchFormList() } doReturn emptyList()
        }

        val formSourceProvider = mock<FormSourceProvider> { on { get() } doReturn formSource }

        updateChecker = FormUpdateChecker(
            context = application,
            notifier = mock(),
            analytics = mock(),
            changeLock = BooleanChangeLock(),
            storagePathProvider = storagePathProvider,
            settingsProvider = SettingsProvider(application),
            formsRepositoryProvider = FormsRepositoryProvider(application, storagePathProvider),
            formSourceProvider = formSourceProvider
        )
    }

    @Test
    fun `checkForUpdates() notifies Forms content resolver`() {
        val contentObserver = mock<ContentObserver>()
        application.contentResolver.registerContentObserver(CONTENT_URI, false, contentObserver)

        val projectId = CollectHelpers.setupDemoProject()
        updateChecker.checkForUpdates(projectId)

        verify(contentObserver).dispatchChange(false, CONTENT_URI)
    }
}
