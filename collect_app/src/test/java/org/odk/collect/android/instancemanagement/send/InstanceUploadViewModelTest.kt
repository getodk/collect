package org.odk.collect.android.instancemanagement.send

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.StandardTestDispatcher
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.odk.collect.android.instancemanagement.InstanceDeleter
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.metadata.PropertyManager
import org.odk.collect.settings.SettingsProvider

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

        val instance3 = Instance.Builder()
            .dbId(3)
            .formId(form.formId)
            .formVersion(form.version)
            .status(Instance.STATUS_COMPLETE)
            .finalizationDate(3)
            .build()

        val instancesRepository = InMemInstancesRepository().apply {
            save(instance1)
            save(instance2)
            save(instance3)
        }

        val submittedInstances = mutableListOf<Long>()

        val instanceUploader = object : InstanceUploader {
            override fun uploadOneSubmission(
                instance: Instance,
                deviceId: String?,
                overrideURL: String?
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

        val instanceDeleter = mock<InstanceDeleter>()
        val webCredentialsUtils = mock<WebCredentialsUtils>()
        val propertyManager = mock<PropertyManager>()
        val settingsProvider = mock<SettingsProvider>()
        val instancesDataService = mock<InstancesDataService>()

        val dispatcher = StandardTestDispatcher()
        viewModel = InstanceUploadViewModel(
            dispatcher,
            instanceUploader,
            instanceDeleter,
            webCredentialsUtils,
            propertyManager,
            instancesRepository,
            formsRepository,
            settingsProvider,
            instancesDataService,
            "projectId",
            "Success"
        )
        viewModel.setDeleteInstanceAfterSubmission(true)
        viewModel.upload(listOf(instance1.dbId, instance2.dbId, instance3.dbId))
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(submittedInstances.containsAll(listOf(instance1.dbId)), equalTo(true))
        verify(instanceDeleter).delete(eq(arrayOf(instance1.dbId)))
    }
}
