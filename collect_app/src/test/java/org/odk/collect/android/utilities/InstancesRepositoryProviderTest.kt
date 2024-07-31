package org.odk.collect.android.utilities

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.startsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.storage.StoragePaths
import org.odk.collect.formstest.InstanceUtils
import org.odk.collect.shared.TempFiles

@RunWith(AndroidJUnit4::class)
class InstancesRepositoryProviderTest {

    private val dbDir = TempFiles.createTempDir()
    private val instancesDir = TempFiles.createTempDir()

    @Test
    fun `returned repository uses project directory when passed`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val instancesRepositoryProvider = InstancesRepositoryProvider(context) {
            StoragePaths(
                "",
                "",
                instancesDir.absolutePath,
                "",
                dbDir.absolutePath,
                "",
                ""
            )
        }

        val repository = instancesRepositoryProvider.create("projectId")
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
