package org.odk.collect.android.instancemanagement

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.shared.TempFiles
import java.io.File

class LocalInstancesUseCasesTest {
    @Test
    fun `creates directory based on definition path and current time in instances directory`() {
        val instancesDirPath = TempFiles.createTempDir().absolutePath

        LocalInstancesUseCases.createInstanceFileBasedOnFormPath(
            "/blah/blah/Cool form name.xml",
            instancesDirPath
        ) { 640908000000 }

        val instanceDir = File(instancesDirPath + File.separator + "Cool form name_1990-04-24_00-00-00")
        assertThat(instanceDir.exists(), equalTo(true))
        assertThat(instanceDir.isDirectory, equalTo(true))
    }

    @Test
    fun `returns instance file in instance directory`() {
        val instancesDirPath = TempFiles.createTempDir().absolutePath

        val instanceFile = LocalInstancesUseCases.createInstanceFileBasedOnFormPath(
            "/blah/blah/Cool form name.xml",
            instancesDirPath
        ) { 640908000000 }!!

        val instanceDir = instancesDirPath + File.separator + "Cool form name_1990-04-24_00-00-00"
        assertThat(
            instanceFile.absolutePath,
            equalTo(instanceDir + File.separator + "Cool form name_1990-04-24_00-00-00.xml")
        )
    }
}
