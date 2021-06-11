package org.odk.collect.android.utilities

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.startsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.formstest.InstanceUtils
import org.odk.collect.shared.TempFiles

@RunWith(AndroidJUnit4::class)
class InstancesRepositoryProviderTest {

    private val dbDir = TempFiles.createTempDir()
    private val instancesDir = TempFiles.createTempDir()

    @Test
    fun `returned repository uses project directory when passed`() {
        val context = ApplicationProvider.getApplicationContext<Application>()

        val projectId = "projectId"
        val storagePathProvider = mock<StoragePathProvider> {
            on {
                getOdkDirPath(
                    StorageSubdirectory.METADATA,
                    projectId
                )
            } doReturn dbDir.absolutePath
            on {
                getOdkDirPath(
                    StorageSubdirectory.INSTANCES,
                    projectId
                )
            } doReturn instancesDir.absolutePath
        }

        val instancesRepositoryProvider = InstancesRepositoryProvider(context, storagePathProvider)
        val repository = instancesRepositoryProvider.get(projectId)

        val instance = repository.save(
            InstanceUtils.buildInstance(
                "formId",
                "formVersion",
                instancesDir.absolutePath
            ).build()
        )

        assertThat(instance.instanceFilePath, startsWith(instancesDir.absolutePath))
    }
}
