package org.odk.collect.android.instancemanagement

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.odk.collect.forms.instances.Instance.STATUS_COMPLETE
import org.odk.collect.forms.instances.Instance.STATUS_INCOMPLETE
import org.odk.collect.forms.instances.Instance.STATUS_INVALID
import org.odk.collect.forms.instances.Instance.STATUS_SUBMISSION_FAILED
import org.odk.collect.forms.instances.Instance.STATUS_SUBMITTED
import org.odk.collect.forms.instances.Instance.STATUS_VALID
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.formstest.InstanceFixtures

class LocalInstanceUseCasesTest {

    @Test
    fun `#reset does not reset instances that can't be deleted before sending`() {
        val form = FormFixtures.form()

        val instancesRepository = InMemInstancesRepository()
        instancesRepository.save(InstanceFixtures.instance(form = form, canDeleteBeforeSend = false, status = STATUS_INCOMPLETE))
        instancesRepository.save(InstanceFixtures.instance(form = form, canDeleteBeforeSend = false, status = STATUS_COMPLETE))
        instancesRepository.save(InstanceFixtures.instance(form = form, canDeleteBeforeSend = false, status = STATUS_INVALID))
        instancesRepository.save(InstanceFixtures.instance(form = form, canDeleteBeforeSend = false, status = STATUS_VALID))
        instancesRepository.save(InstanceFixtures.instance(form = form, canDeleteBeforeSend = false, status = STATUS_SUBMITTED))
        instancesRepository.save(InstanceFixtures.instance(form = form, canDeleteBeforeSend = false, status = STATUS_SUBMISSION_FAILED))

        LocalInstanceUseCases.reset(instancesRepository)
        val remainingInstances = instancesRepository.all
        assertThat(remainingInstances.size, equalTo(2))
        assertThat(remainingInstances.any { it.status == STATUS_COMPLETE }, equalTo(true))
        assertThat(remainingInstances.any { it.status == STATUS_SUBMISSION_FAILED }, equalTo(true))
    }
}
