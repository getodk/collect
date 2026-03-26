package org.odk.collect.android.instancemanagement.send

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.StandardTestDispatcher
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.odk.collect.android.instancemanagement.CachingProjectDependencyModuleFactory
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.android.projects.ProjectDependencyModule
import org.odk.collect.android.utilities.ChangeLocks
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.locks.BooleanChangeLock
import org.odk.collect.shared.settings.InMemSettings

@RunWith(AndroidJUnit4::class)
class InstanceUploadViewModelTest {
    private lateinit var viewModel: InstanceUploadViewModel

    @Test
    fun `submitted instance is deleted even when upload is cancelled`() {
        val form = FormFixtures.form("1")
        val formsRepository = InMemFormsRepository().apply {
            save(form)
        }

        val instance1 = Instance.Builder()
            .dbId(1)
            .formId(form.formId)
            .formVersion(form.version)
            .status(Instance.STATUS_COMPLETE)
            .finalizationDate(1)
            .build()

        val instance2 = Instance.Builder()
            .dbId(2)
            .formId(form.formId)
            .formVersion(form.version)
            .status(Instance.STATUS_COMPLETE)
            .finalizationDate(2)
            .build()

        val instancesRepository = InMemInstancesRepository().apply {
            save(instance1)
            save(instance2)
        }

        val projectsDependencyModuleFactory = CachingProjectDependencyModuleFactory { projectId ->
            ProjectDependencyModule(
                projectId,
                {
                    InMemSettings().also {
                        it.save(ProjectKeys.KEY_DELETE_AFTER_SEND, true)
                    }
                },
                { formsRepository },
                { instancesRepository },
                mock(),
                { ChangeLocks(BooleanChangeLock(), BooleanChangeLock()) },
                mock(),
                mock(),
                mock(),
                mock()
            )
        }

        val submittedInstances = mutableListOf<Long>()

        val instanceUploader = object : InstanceUploader {
            override fun uploadOneSubmission(
                projectId: String,
                instance: Instance,
                deviceId: String?,
                overrideURL: String?,
                referrer: String
            ): String {
                submittedInstances.add(instance.dbId)
                instancesRepository.save(
                    Instance.Builder(instance)
                        .status(Instance.STATUS_SUBMITTED)
                        .build()
                )
                viewModel.cancel()
                return "Success"
            }
        }

        val instancesSubmitter = InstanceSubmitter(
            instanceUploader,
            projectsDependencyModuleFactory,
            mock()
        )
        val instancesDataService = InstancesDataService(
            AppState(),
            mock(),
            projectsDependencyModuleFactory,
            mock(),
            instancesSubmitter
        ) {}

        val dispatcher = StandardTestDispatcher()
        viewModel = InstanceUploadViewModel(
            dispatcher,
            mock(),
            instancesRepository,
            instancesDataService,
            "projectId",
            "",
            null,
            null,
            null,
            null,
            "Success",
            "Waiting"
        )
        viewModel.upload(listOf(instance1.dbId, instance2.dbId))
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(submittedInstances.containsAll(listOf(instance1.dbId)), equalTo(true))
        assertThat(instancesRepository.get(instance1.dbId)!!.deletedDate, notNullValue())
        assertThat(instancesRepository.get(instance2.dbId)!!.deletedDate, equalTo(null))
    }
}
