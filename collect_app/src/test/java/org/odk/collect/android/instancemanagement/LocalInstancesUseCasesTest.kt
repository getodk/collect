package org.odk.collect.android.instancemanagement

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Test
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.formstest.InstanceUtils.buildInstance
import org.odk.collect.shared.TempFiles
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.File

class LocalInstancesUseCasesTest {
    @Test
    fun `creates directory based on definition path and current time in instances directory`() {
        val instancesDirPath = TempFiles.createTempDir().absolutePath

        LocalInstancesUseCases.createInstanceFileBasedOnFormPath(
            "/blah/blah/Cool form name.xml",
            instancesDirPath
        ) { 640915200000 }

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
        ) { 640915200000 }!!

        val instanceDir = instancesDirPath + File.separator + "Cool form name_1990-04-24_00-00-00"
        assertThat(
            instanceFile.absolutePath,
            equalTo(instanceDir + File.separator + "Cool form name_1990-04-24_00-00-00.xml")
        )
    }

    @Test
    fun `#clone makes a proper copy of the instance dir`() {
        val instancesRepository = InMemInstancesRepository()
        val instancesDir = TempFiles.createTempDir()

        val sourceInstance = instancesRepository.save(
            buildInstance("1", "1", instancesDir.absolutePath).build()
        )
        val sourceInstanceFile = File(sourceInstance.instanceFilePath)
        val sourceInstanceFileMd5Hash = sourceInstanceFile.getMd5Hash()
        val sourceInstanceFilePath = sourceInstanceFile.absolutePath

        val sourceInstanceDir = File(sourceInstanceFile.parent!!)

        val sourceMediaFile1 = TempFiles.createTempFile(sourceInstanceDir, "foo", ".jpg").also {
            it.writeText("foo")
        }
        val sourceMediaFile1Md5Hash = sourceMediaFile1.getMd5Hash()
        val sourceMediaFile1Path = sourceMediaFile1.absolutePath

        val sourceMediaFile2 = TempFiles.createTempFile(sourceInstanceDir, "bar", ".mp4").also {
            it.writeText("bar")
        }
        val sourceMediaFile2Md5Hash = sourceMediaFile2.getMd5Hash()
        val sourceMediaFile2Path = sourceMediaFile2.absolutePath

        val clonedInstanceDbId = LocalInstancesUseCases.clone(
            sourceInstanceFile,
            instancesDir.absolutePath,
            instancesRepository
        ) { (100_000_000_0000L..999_999_999_9999L).random() }

        val clonedInstance = instancesRepository.get(clonedInstanceDbId)!!

        val clonedInstanceFile = File(clonedInstance.instanceFilePath)
        val clonedInstanceDir = File(clonedInstanceFile.parent!!)

        val sourceFiles = sourceInstanceDir.listFiles()!!
        val clonedFiles = clonedInstanceDir.listFiles()!!

        assertThat(sourceFiles.size, equalTo(3))
        assertThat(clonedFiles.size, equalTo(3))

        assertThat(sourceInstanceFile.exists(), equalTo(true))
        assertThat(clonedInstanceFile.exists(), equalTo(true))
        assertThat(sourceInstanceFileMd5Hash, equalTo(clonedInstanceFile.getMd5Hash()))
        assertThat(sourceInstanceFilePath, not(clonedInstanceFile))

        val clonedMediaFile1 = clonedFiles.find { it.name == sourceMediaFile1.name }!!
        assertThat(sourceMediaFile1.exists(), equalTo(true))
        assertThat(clonedMediaFile1.exists(), equalTo(true))
        assertThat(sourceMediaFile1Md5Hash, equalTo(clonedMediaFile1.getMd5Hash()))
        assertThat(sourceMediaFile1Path, not(clonedMediaFile1))

        val clonedMediaFile2 = clonedFiles.find { it.name == sourceMediaFile2.name }!!
        assertThat(sourceMediaFile2.exists(), equalTo(true))
        assertThat(clonedMediaFile2.exists(), equalTo(true))
        assertThat(sourceMediaFile2Md5Hash, equalTo(clonedMediaFile2.getMd5Hash()))
        assertThat(sourceMediaFile2Path, not(clonedMediaFile2))
    }

    @Test
    fun `#clone makes a proper copy of the instance row in the database`() {
        val instancesRepository = InMemInstancesRepository()
        val instancesDir = TempFiles.createTempDir()

        val sourceInstance = instancesRepository.save(
            buildInstance("1", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )
        val sourceInstanceFile = File(sourceInstance.instanceFilePath)

        val clonedInstanceDbId = LocalInstancesUseCases.clone(
            sourceInstanceFile,
            instancesDir.absolutePath,
            instancesRepository
        ) { (100_000_000_0000L..999_999_999_9999L).random() }
        val clonedInstance = instancesRepository.get(clonedInstanceDbId)!!

        assertThat(instancesRepository.all.size, equalTo(2))
        assertThat(sourceInstance, equalTo(instancesRepository.get(sourceInstance.dbId)))
        assertThat(sourceInstance, not(clonedInstance))
        assertThat(clonedInstance.status, equalTo(Instance.STATUS_VALID))
        assertThat(sourceInstance.instanceFilePath, not(clonedInstance.instanceFilePath))
    }

    @Test
    fun `#clone can make a copy of the same instance multiple times`() {
        val instancesRepository = InMemInstancesRepository()
        val instancesDir = TempFiles.createTempDir()

        val sourceInstance = instancesRepository.save(
            buildInstance("1", "1", instancesDir.absolutePath).build()
        )
        val sourceInstanceFile = File(sourceInstance.instanceFilePath)
        val sourceInstanceFilePath = sourceInstanceFile.absolutePath

        val clonedInstanceDbId1 = LocalInstancesUseCases.clone(
            sourceInstanceFile,
            instancesDir.absolutePath,
            instancesRepository
        ) { (100_000_000_0000L..999_999_999_9999L).random() }
        val clonedInstance1 = instancesRepository.get(clonedInstanceDbId1)!!
        val clonedInstanceFile1 = File(clonedInstance1.instanceFilePath)

        val clonedInstanceDbId2 = LocalInstancesUseCases.clone(
            sourceInstanceFile,
            instancesDir.absolutePath,
            instancesRepository,
        ) { (100_000_000_0000L..999_999_999_9999L).random() }
        val clonedInstance2 = instancesRepository.get(clonedInstanceDbId2)!!
        val clonedInstanceFile2 = File(clonedInstance2.instanceFilePath)

        assertThat(instancesRepository.all.size, equalTo(3))
        assertThat(sourceInstanceFilePath, not(clonedInstanceFile1.absolutePath))
        assertThat(sourceInstanceFilePath, not(clonedInstanceFile2.absolutePath))
        assertThat(clonedInstanceFile1.absolutePath, not(clonedInstanceFile2.absolutePath))
    }
}
