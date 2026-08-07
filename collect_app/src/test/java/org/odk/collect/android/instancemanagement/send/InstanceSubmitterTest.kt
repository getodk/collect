package org.odk.collect.android.instancemanagement.send

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.odk.collect.android.instancemanagement.CachingProjectDependencyModuleFactory
import org.odk.collect.android.projects.ProjectDependencyModule
import org.odk.collect.android.utilities.ChangeLocks
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.locks.BooleanChangeLock
import org.odk.collect.shared.settings.InMemSettings

@RunWith(AndroidJUnit4::class)
class InstanceSubmitterTest {
    private val form = FormFixtures.form("1")

    private val formsRepository = InMemFormsRepository().apply {
        save(form)
    }

    private val instancesRepository = InMemInstancesRepository()

    private val generalSettings = InMemSettings().apply {
        save(ProjectKeys.KEY_DELETE_AFTER_SEND, true)
    }

    private val projectDependencyModuleFactory = CachingProjectDependencyModuleFactory { projectId ->
        ProjectDependencyModule(
            projectId,
            { generalSettings },
            { formsRepository },
            { instancesRepository },
            mock(),
            { ChangeLocks(BooleanChangeLock(), BooleanChangeLock()) },
            mock(),
            mock(),
            mock(),
            mock(),
            mock()
        )
    }

    @Test
    fun `remaining instances are not submitted when upload is canceled`() {
        val instance1 = saveInstance(1)
        val instance2 = saveInstance(2)
        val instance3 = saveInstance(3)
        val uploaded = mutableListOf<Long>()

        val results = createSubmitter(interruptedAt = instance2.dbId, uploaded = uploaded)
            .submitInstances("projectId", listOf(instance1, instance2, instance3))

        assertThat(uploaded, equalTo(listOf(instance1.dbId)))
        assertThat(results.map { it.instance.dbId }, equalTo(listOf(instance1.dbId)))
    }

    @Test
    fun `instances uploaded before cancellation get deleted`() {
        val instance1 = saveInstance(1)
        val instance2 = saveInstance(2)

        createSubmitter(interruptedAt = instance2.dbId)
            .submitInstances("projectId", listOf(instance1, instance2))

        assertThat(instancesRepository.get(instance1.dbId), nullValue())
        assertThat(instancesRepository.get(instance2.dbId), notNullValue())
    }

    private fun saveInstance(finalizationDate: Long): Instance {
        return instancesRepository.save(
            Instance.Builder()
                .formId(form.formId)
                .formVersion(form.version)
                .status(Instance.STATUS_COMPLETE)
                .finalizationDate(finalizationDate)
                .build()
        )
    }

    private fun createSubmitter(
        interruptedAt: Long,
        uploaded: MutableList<Long> = mutableListOf()
    ): InstanceSubmitter {
        val instanceUploader = object : InstanceUploader {
            override fun uploadOneSubmission(
                projectId: String,
                instance: Instance,
                deviceId: String?,
                overrideURL: String?,
                referrer: String,
                isCancelled: () -> Boolean
            ): String {
                if (instance.dbId == interruptedAt) {
                    throw FormUploadInterruptedException()
                }

                uploaded.add(instance.dbId)
                return "Success"
            }
        }

        return InstanceSubmitter(instanceUploader, projectDependencyModuleFactory, mock())
    }
}
