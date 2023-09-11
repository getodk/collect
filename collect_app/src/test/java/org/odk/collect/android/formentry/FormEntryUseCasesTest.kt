package org.odk.collect.android.formentry

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.entities.InMemEntitiesRepository
import org.odk.collect.android.javarosawrapper.FailedValidationResult
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.formstest.InstanceFixtures
import java.io.File

class FormEntryUseCasesTest {

    @Test
    fun finalizeDraft_whenValidationFails_marksInstanceAsHavingErrors() {
        val instancesRepository = InMemInstancesRepository()
        val instance = instancesRepository.save(InstanceFixtures.instance())

        val formController = mock<FormController> {
            on { validateAnswers(true) } doReturn FailedValidationResult(mock(), 0)
            on { getInstanceFile() } doReturn File(instance.instanceFilePath)
        }

        FormEntryUseCases.finalizeDraft(
            formController,
            instancesRepository,
            InMemEntitiesRepository()
        )

        assertThat(
            instancesRepository.get(instance.dbId)!!.status,
            equalTo(Instance.STATUS_INVALID)
        )
    }
}