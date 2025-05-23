package org.odk.collect.formstest

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.formstest.InstanceUtils.buildInstance
import java.io.File
import java.util.function.Supplier

abstract class InstancesRepositoryTest {
    abstract fun buildSubject(): InstancesRepository

    abstract fun buildSubject(clock: Supplier<Long>): InstancesRepository

    abstract val instancesDir: File

    @Test
    fun allNotDeleted_returnsUndeletedInstances() {
        val instancesRepository = buildSubject()

        instancesRepository.save(
            buildInstance("deleted", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_COMPLETE)
                .deletedDate(System.currentTimeMillis())
                .build()
        )
        instancesRepository.save(
            buildInstance("undeleted", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        val allNotDeleted = instancesRepository.allNotDeleted
        assertThat(allNotDeleted.size, equalTo(1))
        assertThat(allNotDeleted[0].formId, equalTo("undeleted"))
    }

    @Test
    fun allByStatus_withOneStatus_returnsMatchingInstances() {
        val instancesRepository = buildSubject()

        instancesRepository.save(
            buildInstance("incomplete", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_INCOMPLETE)
                .build()
        )
        instancesRepository.save(
            buildInstance("incomplete", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_INCOMPLETE)
                .build()
        )

        instancesRepository.save(
            buildInstance("complete", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_COMPLETE)
                .build()
        )
        instancesRepository.save(
            buildInstance("complete", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        val incomplete = instancesRepository.getAllByStatus(Instance.STATUS_INCOMPLETE)
        assertThat(incomplete.size, equalTo(2))
        assertThat(incomplete[0].formId, equalTo("incomplete"))
        assertThat(incomplete[1].status, equalTo("incomplete"))

        // Check corresponding count method is also correct
        assertThat(
            instancesRepository.getCountByStatus(Instance.STATUS_INCOMPLETE),
            equalTo(2)
        )
    }

    @Test
    fun allByStatus_withMultipleStatus_returnsMatchingInstances() {
        val instancesRepository = buildSubject()

        instancesRepository.save(
            buildInstance("incomplete", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_INCOMPLETE)
                .build()
        )
        instancesRepository.save(
            buildInstance("incomplete", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_INCOMPLETE)
                .build()
        )

        instancesRepository.save(
            buildInstance("complete", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_COMPLETE)
                .build()
        )
        instancesRepository.save(
            buildInstance("complete", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        instancesRepository.save(
            buildInstance("submitted", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )
        instancesRepository.save(
            buildInstance("submitted", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )

        val incomplete =
            instancesRepository.getAllByStatus(
                Instance.STATUS_INCOMPLETE,
                Instance.STATUS_SUBMITTED
            )
        assertThat(incomplete.size, equalTo(4))
        assertThat(incomplete[0].formId, not("complete"))
        assertThat(incomplete[1].formId, not("complete"))
        assertThat(incomplete[2].formId, not("complete"))
        assertThat(incomplete[3].status, not("complete"))

        // Check corresponding count method is also correct
        assertThat(
            instancesRepository.getCountByStatus(Instance.STATUS_INCOMPLETE, Instance.STATUS_SUBMITTED),
            equalTo(4)
        )
    }

    @Test
    fun allByFormId_includesAllVersionsForFormId() {
        val instancesRepository = buildSubject()

        instancesRepository.save(
            buildInstance("formid", "1", instancesDir.absolutePath).build()
        )
        instancesRepository.save(
            buildInstance(
                "formid",
                "2",
                "display",
                Instance.STATUS_COMPLETE,
                null,
                instancesDir.absolutePath
            ).build()
        )
        instancesRepository.save(
            buildInstance("formid", "3", instancesDir.absolutePath).build()
        )
        instancesRepository.save(
            buildInstance(
                "formid",
                "4",
                "display",
                Instance.STATUS_COMPLETE,
                System.currentTimeMillis(),
                instancesDir.absolutePath
            ).build()
        )
        instancesRepository.save(
            buildInstance(
                "formid2",
                "1",
                "display",
                Instance.STATUS_COMPLETE,
                null,
                instancesDir.absolutePath
            ).build()
        )

        val instances = instancesRepository.getAllByFormId("formid")
        assertThat(instances.size, equalTo(4))
    }

    @Test
    fun allByFormIdAndVersionNotDeleted_excludesDeleted() {
        val instancesRepository = buildSubject()

        instancesRepository.save(
            buildInstance("formid", "1", instancesDir.absolutePath).build()
        )
        instancesRepository.save(
            buildInstance(
                "formid",
                "1",
                "display",
                Instance.STATUS_COMPLETE,
                null,
                instancesDir.absolutePath
            )
                .build()
        )
        instancesRepository.save(
            buildInstance("formid", "1", instancesDir.absolutePath).build()
        )
        instancesRepository.save(
            buildInstance(
                "formid",
                "1",
                "display",
                Instance.STATUS_COMPLETE,
                System.currentTimeMillis(),
                instancesDir.absolutePath
            )
                .build()
        )
        instancesRepository.save(
            buildInstance(
                "formid2",
                "1",
                "display",
                Instance.STATUS_COMPLETE,
                null,
                instancesDir.absolutePath
            )
                .build()
        )

        val instances = instancesRepository.getAllNotDeletedByFormIdAndVersion("formid", "1")
        assertThat(instances.size, equalTo(3))
    }

    @Test
    fun deleteAll_deletesAllInstances() {
        val instancesRepository = buildSubject()

        instancesRepository.save(buildInstance("formid", "1", instancesDir.absolutePath).build())
        instancesRepository.save(buildInstance("formid", "1", instancesDir.absolutePath).build())

        instancesRepository.deleteAll()
        assertThat(instancesRepository.all.size, equalTo(0))
    }

    @Test
    fun deleteAll_deletesInstanceFiles() {
        val instancesRepository = buildSubject()

        val instance1 = instancesRepository.save(
            buildInstance(
                "formid", "1",
                instancesDir.absolutePath
            ).build()
        )
        val instance2 = instancesRepository.save(
            buildInstance(
                "formid", "1",
                instancesDir.absolutePath
            ).build()
        )

        instancesRepository.deleteAll()
        assertThat(File(instance1.instanceFilePath).exists(), equalTo(false))
        assertThat(File(instance2.instanceFilePath).exists(), equalTo(false))
    }

    @Test
    fun save_addsUniqueId() {
        val instancesRepository = buildSubject()

        instancesRepository.save(buildInstance("formid", "1", instancesDir.absolutePath).build())
        instancesRepository.save(buildInstance("formid", "1", instancesDir.absolutePath).build())

        val id1 = instancesRepository.all[0].dbId
        val id2 = instancesRepository.all[1].dbId
        assertThat(id1, notNullValue())
        assertThat(id2, notNullValue())
        assertThat(id1, not(equalTo(id2)))
    }

    @Test
    fun save_returnsInstanceWithId() {
        val instancesRepository = buildSubject()

        val instance = instancesRepository.save(
            buildInstance(
                "formid", "1",
                instancesDir.absolutePath
            ).build()
        )
        assertThat(instancesRepository[instance.dbId], equalTo(instance))
    }

    @Test
    fun save_whenInstanceHasId_updatesExisting() {
        val instancesRepository = buildSubject()

        val originalInstance = instancesRepository.save(
            buildInstance(
                "formid", "1",
                instancesDir.absolutePath
            )
                .displayName("Blah")
                .build()
        )

        instancesRepository.save(
            Instance.Builder(originalInstance)
                .displayName("A different blah")
                .build()
        )

        assertThat(
            instancesRepository[originalInstance.dbId]!!.displayName,
            equalTo("A different blah")
        )
    }

    @Test
    fun save_whenInstanceHasId_updatesLastStatusChangeDate() {
        val clock = mock<Supplier<Long>>()
        whenever(clock.get()).thenReturn(123L)

        val instancesRepository = buildSubject(clock)

        val instance = instancesRepository.save(
            buildInstance(
                "formid", "1",
                instancesDir.absolutePath
            ).build()
        )

        instancesRepository.save(instance)
        assertThat(
            instancesRepository[instance.dbId]!!.lastStatusChangeDate, equalTo(123L)
        )
    }

    @Test
    fun save_whenStatusIsFinalized_populatesFinalizationDate() {
        val clock = mock<Supplier<Long>>()
        whenever(clock.get()).thenReturn(123L)

        val instancesRepository = buildSubject(clock)

        val instance = instancesRepository.save(
            buildInstance("formid", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_COMPLETE)
                .build()
        )
        assertThat(
            instancesRepository[instance.dbId]!!.finalizationDate,
            equalTo(123L)
        )
    }

    @Test
    fun save_whenStatusIsFinalizedAndFinalizationDateIsAlreadyPopulated_doesNotUpdateFinalizationDate() {
        val clock = mock<Supplier<Long>>()
        whenever(clock.get()).thenReturn(123L)

        val instancesRepository = buildSubject(clock)

        val instance = instancesRepository.save(
            buildInstance("formid", "1", instancesDir.absolutePath)
                .status(Instance.STATUS_COMPLETE)
                .build()
        )
        whenever(clock.get()).thenReturn(456L)
        instancesRepository.save(instance)
        assertThat(
            instancesRepository[instance.dbId]!!.finalizationDate,
            equalTo(123L)
        )
    }

    @Test
    fun save_whenStatusIsNotFinalized_doesNotPopulateFinalizationDate() {
        val clock = mock<Supplier<Long>>()
        whenever(clock.get()).thenReturn(123L)

        val instancesRepository = buildSubject(clock)

        val nonFinalizedStatuses = listOf(
            Instance.STATUS_VALID,
            Instance.STATUS_INVALID,
            Instance.STATUS_NEW_EDIT,
            Instance.STATUS_SUBMISSION_FAILED,
            Instance.STATUS_SUBMITTED
        )

        for (status in nonFinalizedStatuses) {
            val instance = instancesRepository.save(
                buildInstance("formid", "1", instancesDir.absolutePath)
                    .status(status)
                    .build()
            )

            assertThat(
                instancesRepository[instance.dbId]!!.finalizationDate,
                equalTo(null)
            )
        }
    }

    @Test
    fun save_whenStatusIsNull_usesIncomplete() {
        val instancesRepository = buildSubject()

        val instance = instancesRepository.save(
            buildInstance(
                "formid", "1",
                instancesDir.absolutePath
            )
                .status(null)
                .build()
        )
        assertThat(
            instancesRepository[instance.dbId]!!.status, equalTo(Instance.STATUS_INCOMPLETE)
        )
    }

    @Test
    fun save_whenLastStatusChangeDateIsNull_setsIt() {
        val instancesRepository = buildSubject()

        val instance = instancesRepository.save(
            buildInstance(
                "formid", "1",
                instancesDir.absolutePath
            )
                .lastStatusChangeDate(null)
                .build()
        )
        assertThat(
            instancesRepository[instance.dbId]!!.lastStatusChangeDate,
            notNullValue()
        )
    }

    @Test
    fun save_whenInstanceHasDeletedDate_doesNotUpdateLastChangesStatusDate() {
        val clock = mock<Supplier<Long>>()
        whenever(clock.get()).thenReturn(123L)

        val instancesRepository = buildSubject(clock)

        val originalInstance = instancesRepository.save(
            buildInstance(
                "formid", "1",
                instancesDir.absolutePath
            ).build()
        )
        val originalInstanceDbId = originalInstance.dbId

        whenever(clock.get()).thenReturn(456L)
        instancesRepository.deleteWithLogging(originalInstanceDbId)
        instancesRepository.save(instancesRepository[originalInstanceDbId])

        assertThat(
            instancesRepository[originalInstanceDbId]!!.lastStatusChangeDate, equalTo(123L)
        )
    }

    @Test
    fun deleteWithLogging_setsDeletedDate() {
        val instancesRepository = buildSubject()
        val instance = instancesRepository.save(
            buildInstance(
                "formid", "1",
                instancesDir.absolutePath
            ).build()
        )

        instancesRepository.deleteWithLogging(instance.dbId)
        assertThat(
            instancesRepository[instance.dbId]!!.deletedDate, notNullValue()
        )
    }

    @Test
    fun deleteWithLogging_deletesInstanceDir() {
        val instancesRepository = buildSubject()
        val instance = instancesRepository.save(
            buildInstance(
                "formid", "1",
                instancesDir.absolutePath
            ).build()
        )

        val instanceDir = File(instance.instanceFilePath).parentFile
        assertThat(instanceDir.exists(), equalTo(true))
        assertThat(instanceDir.isDirectory, equalTo(true))

        instancesRepository.deleteWithLogging(instance.dbId)
        assertThat(instanceDir.exists(), equalTo(false))
    }

    @Test
    fun deleteWithLogging_clearsGeometryData() {
        val instancesRepository = buildSubject()
        val instance = instancesRepository.save(
            buildInstance(
                "formid", "1",
                instancesDir.absolutePath
            )
                .geometry("blah")
                .geometryType("blah")
                .build()
        )

        instancesRepository.deleteWithLogging(instance.dbId)
        assertThat(
            instancesRepository[instance.dbId]!!.geometry, equalTo(null)
        )
        assertThat(
            instancesRepository[instance.dbId]!!.geometryType, equalTo(null)
        )
    }

    @Test
    fun delete_deletesInstance() {
        val instancesRepository = buildSubject()
        val instance = instancesRepository.save(
            buildInstance(
                "formid", "1",
                instancesDir.absolutePath
            ).build()
        )

        instancesRepository.delete(instance.dbId)
        assertThat(instancesRepository.all.size, equalTo(0))
    }

    @Test
    fun delete_deletesInstanceDir() {
        val instancesRepository = buildSubject()
        val instance = instancesRepository.save(
            buildInstance(
                "formid", "1",
                instancesDir.absolutePath
            ).build()
        )

        // The repo assumes the parent of the file also contains other instance files
        val instanceDir = File(instance.instanceFilePath).parentFile
        assertThat(instanceDir.exists(), equalTo(true))
        assertThat(instanceDir.isDirectory, equalTo(true))

        instancesRepository.delete(instance.dbId)
        assertThat(instanceDir.exists(), equalTo(false))
    }

    @Test
    fun delete_failsWhenDeletingInstanceWithEdits() {
        val instancesRepository = buildSubject()

        val originalInstance = InstanceFixtures.instance(displayName = "Form1", instancesDir = instancesDir)
        val originalInstanceDbId = instancesRepository.save(originalInstance).dbId

        val editedInstance = InstanceFixtures.instance(displayName = "Form1", instancesDir = instancesDir, editOf = originalInstanceDbId, editNumber = 1)
        instancesRepository.save(editedInstance)

        assertThrows(InstancesRepository.IntegrityException::class.java) {
            instancesRepository.delete(originalInstanceDbId)
        }
    }

    @Test
    fun save_failsWhenEditOfPointsAtItsOwnDbId() {
        val instancesRepository = buildSubject()

        val originalInstance = InstanceFixtures.instance(displayName = "Form1", instancesDir = instancesDir)
        val originalInstanceDbId = instancesRepository.save(originalInstance).dbId

        val editedInstance = InstanceFixtures.instance(displayName = "Form1", instancesDir = instancesDir, editOf = originalInstanceDbId + 1, editNumber = 1)

        assertThrows(InstancesRepository.IntegrityException::class.java) {
            instancesRepository.save(editedInstance)
        }
    }

    @Test
    fun save_failsWhenEditOfPointsAtNonExistingDbId() {
        val instancesRepository = buildSubject()

        val originalInstance = InstanceFixtures.instance(displayName = "Form1", instancesDir = instancesDir)
        val originalInstanceDbId = instancesRepository.save(originalInstance).dbId

        val editedInstance = InstanceFixtures.instance(displayName = "Form1", instancesDir = instancesDir, editOf = originalInstanceDbId + 100, editNumber = 1)

        assertThrows(InstancesRepository.IntegrityException::class.java) {
            instancesRepository.save(editedInstance)
        }
    }

    @Test
    fun save_failsWhenEditOfIsNullButEditNumberIsNotNull() {
        val instancesRepository = buildSubject()

        val originalInstance = InstanceFixtures.instance(displayName = "Form1", instancesDir = instancesDir)
        instancesRepository.save(originalInstance)

        val editedInstance = InstanceFixtures.instance(displayName = "Form1", instancesDir = instancesDir, editNumber = 1)

        assertThrows(InstancesRepository.IntegrityException::class.java) {
            instancesRepository.save(editedInstance)
        }
    }

    @Test
    fun save_failsWhenEditOfIsNotNullButEditNumberIsNull() {
        val instancesRepository = buildSubject()

        val originalInstance = InstanceFixtures.instance(displayName = "Form1", instancesDir = instancesDir)
        val originalInstanceDbId = instancesRepository.save(originalInstance).dbId

        val editedInstance = InstanceFixtures.instance(displayName = "Form1", instancesDir = instancesDir, editOf = originalInstanceDbId)

        assertThrows(InstancesRepository.IntegrityException::class.java) {
            instancesRepository.save(editedInstance)
        }
    }
}
