package org.odk.collect.android.utilities

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.startsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory.CACHE
import org.odk.collect.android.storage.StorageSubdirectory.FORMS
import org.odk.collect.android.storage.StorageSubdirectory.METADATA
import org.odk.collect.formstest.FormUtils.buildForm
import org.odk.collect.shared.TempFiles.createTempDir

@RunWith(AndroidJUnit4::class)
class FormsRepositoryProviderTest {

    private val dbDir = createTempDir()
    private val formsDir = createTempDir()
    private val cacheDir = createTempDir()

    @Test
    fun `returned repository uses project directory when passed`() {
        val context = ApplicationProvider.getApplicationContext<Application>()

        val projectId = "projectId"
        val storagePathProvider = mock<StoragePathProvider> {
            on { getOdkDirPath(METADATA, projectId) } doReturn dbDir.absolutePath
            on { getOdkDirPath(FORMS, projectId) } doReturn formsDir.absolutePath
            on { getOdkDirPath(CACHE, projectId) } doReturn cacheDir.absolutePath
        }

        val formsRepositoryProvider = FormsRepositoryProvider(context, storagePathProvider)
        val repository = formsRepositoryProvider.get(projectId)

        val form = repository.save(buildForm("id", "version", formsDir.absolutePath).build())
        assertThat(form.formFilePath, startsWith(formsDir.absolutePath))
        assertThat(form.jrCacheFilePath, startsWith(cacheDir.absolutePath))
    }
}
