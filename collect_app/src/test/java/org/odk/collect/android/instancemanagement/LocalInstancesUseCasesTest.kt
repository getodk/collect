package org.odk.collect.android.instancemanagement

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Test
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.formstest.InstanceFixtures
import org.odk.collect.shared.TempFiles
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.File
import java.util.TimeZone
import kotlin.random.Random

class LocalInstancesUseCasesTest {
    @Test
    fun `#createInstanceFile creates directory based on sanitized form name and current time in instances directory`() {
        val instancesDirPath = TempFiles.createTempDir().absolutePath

        LocalInstancesUseCases.createInstanceFile(
            "Cool form  name:",
            instancesDirPath,
            TimeZone.getTimeZone("UTC")
        ) { 640915200000 }

        val instanceDir = File(instancesDirPath + File.separator + "Cool form name_1990-04-24_00-00-00")
        assertThat(instanceDir.exists(), equalTo(true))
        assertThat(instanceDir.isDirectory, equalTo(true))
    }

    @Test
    fun `#createInstanceFile returns instance file in instance directory`() {
        val instancesDirPath = TempFiles.createTempDir().absolutePath

        val instanceFile = LocalInstancesUseCases.createInstanceFile(
            "Cool form name",
            instancesDirPath,
            TimeZone.getTimeZone("UTC")
        ) { 640915200000 }!!

        val instanceDir = instancesDirPath + File.separator + "Cool form name_1990-04-24_00-00-00"
        assertThat(
            instanceFile.absolutePath,
            equalTo(instanceDir + File.separator + "Cool form name_1990-04-24_00-00-00.xml")
        )
    }

    @Test
    fun `#editInstance makes a proper copy of the instance dir`() {
        val formsRepository = InMemFormsRepository()
        val instancesRepository = InMemInstancesRepository()
        val instancesDir = TempFiles.createTempDir()

        val form = formsRepository.save(FormFixtures.form())
        val sourceInstance = instancesRepository.save(
            InstanceFixtures.instance(
                instancesDir = instancesDir,
                formId = form.formId,
                formVersion = form.version!!
            )
        )
        val sourceInstanceFile = File(sourceInstance.instanceFilePath)
        val sourceInstanceFileMd5Hash = sourceInstanceFile.getMd5Hash()

        val sourceInstanceDir = File(sourceInstanceFile.parent!!)

        val sourceMediaFile1 = TempFiles.createTempFile(sourceInstanceDir, "foo", ".jpg").also {
            it.writeText("foo")
        }
        val sourceMediaFile1Md5Hash = sourceMediaFile1.getMd5Hash()

        val sourceMediaFile2 = TempFiles.createTempFile(sourceInstanceDir, "bar", ".mp4").also {
            it.writeText("bar")
        }
        val sourceMediaFile2Md5Hash = sourceMediaFile2.getMd5Hash()

        val editedInstance = LocalInstancesUseCases.editInstance(
            sourceInstanceFile.absolutePath,
            instancesDir.absolutePath,
            instancesRepository,
            formsRepository
        ) { Random.nextLong() }.instance

        val editedInstanceFile = File(editedInstance.instanceFilePath)
        val editedInstanceDir = File(editedInstanceFile.parent!!)

        val sourceFiles = sourceInstanceDir.listFiles()!!
        val editedFiles = editedInstanceDir.listFiles()!!

        assertThat(sourceFiles.size, equalTo(3))
        assertThat(editedFiles.size, equalTo(3))
        assertThat(sourceInstanceDir.absolutePath, not(editedInstanceDir.absolutePath))
        assertThat(sourceInstanceFile.name, not(editedInstanceFile.name))

        assertThat(sourceInstanceFile.exists(), equalTo(true))
        assertThat(editedInstanceFile.exists(), equalTo(true))
        assertThat(sourceInstanceFileMd5Hash, equalTo(editedInstanceFile.getMd5Hash()))

        val editedMediaFile1 = editedFiles.find { it.name == sourceMediaFile1.name }!!
        assertThat(sourceMediaFile1.exists(), equalTo(true))
        assertThat(editedMediaFile1.exists(), equalTo(true))
        assertThat(sourceMediaFile1Md5Hash, equalTo(editedMediaFile1.getMd5Hash()))

        val editedMediaFile2 = editedFiles.find { it.name == sourceMediaFile2.name }!!
        assertThat(sourceMediaFile2.exists(), equalTo(true))
        assertThat(editedMediaFile2.exists(), equalTo(true))
        assertThat(sourceMediaFile2Md5Hash, equalTo(editedMediaFile2.getMd5Hash()))
    }

    @Test
    fun `#editInstance makes a proper copy of the instance row in the database`() {
        val formsRepository = InMemFormsRepository()
        val instancesRepository = InMemInstancesRepository()
        val instancesDir = TempFiles.createTempDir()

        val form = formsRepository.save(FormFixtures.form())
        val sourceInstance = instancesRepository.save(
            InstanceFixtures.instance(
                instancesDir = instancesDir,
                status = Instance.STATUS_SUBMITTED,
                formId = form.formId,
                formVersion = form.version!!
            )
        )

        // The first edit
        val firstEditedInstance = LocalInstancesUseCases.editInstance(
            sourceInstance.instanceFilePath,
            instancesDir.absolutePath,
            instancesRepository,
            formsRepository
        ) { Random.nextLong() }.instance

        assertThat(instancesRepository.all.size, equalTo(2))
        assertThat(sourceInstance, equalTo(instancesRepository.get(sourceInstance.dbId)))
        assertThat(sourceInstance, not(firstEditedInstance))
        assertThat(firstEditedInstance.status, equalTo(Instance.STATUS_NEW_EDIT))
        assertThat(sourceInstance.instanceFilePath, not(firstEditedInstance.instanceFilePath))
        assertThat(firstEditedInstance.editOf, equalTo(sourceInstance.dbId))
        assertThat(firstEditedInstance.editNumber, equalTo(1))

        // The second edit
        val secondEditedInstance = LocalInstancesUseCases.editInstance(
            firstEditedInstance.instanceFilePath,
            instancesDir.absolutePath,
            instancesRepository,
            formsRepository
        ) { Random.nextLong() }.instance

        assertThat(instancesRepository.all.size, equalTo(3))
        assertThat(secondEditedInstance.editOf, equalTo(sourceInstance.dbId))
        assertThat(secondEditedInstance.editNumber, equalTo(2))
    }

    @Test
    fun `#editInstance returns the newest edit instance when user attempts to edit an outdated one`() {
        val formsRepository = InMemFormsRepository()
        val instancesRepository = InMemInstancesRepository()
        val instancesDir = TempFiles.createTempDir()

        val form = formsRepository.save(FormFixtures.form())
        val sourceInstance = instancesRepository.save(
            InstanceFixtures.instance(
                instancesDir = instancesDir,
                formId = form.formId,
                formVersion = form.version!!
            )
        )
        val sourceInstanceFile = File(sourceInstance.instanceFilePath)
        val editedInstance1 = instancesRepository.save(
            InstanceFixtures.instance(
                instancesDir = instancesDir,
                formId = form.formId,
                formVersion = form.version!!,
                editOf = sourceInstance.dbId,
                editNumber = 1
            )
        )
        val editedInstance1File = File(editedInstance1.instanceFilePath)
        val editedInstance2 = instancesRepository.save(
            InstanceFixtures.instance(
                instancesDir = instancesDir,
                formId = form.formId,
                formVersion = form.version!!,
                editOf = sourceInstance.dbId,
                editNumber = 2
            )
        )

        var editResult = LocalInstancesUseCases.editInstance(
            sourceInstanceFile.absolutePath,
            instancesDir.absolutePath,
            instancesRepository,
            formsRepository
        ) { Random.nextLong() }
        assertThat(editResult.instance.dbId, equalTo(editedInstance2.dbId))

        editResult = LocalInstancesUseCases.editInstance(
            editedInstance1File.absolutePath,
            instancesDir.absolutePath,
            instancesRepository,
            formsRepository
        ) { Random.nextLong() }
        assertThat(editResult.instance.dbId, equalTo(editedInstance2.dbId))
    }
}
