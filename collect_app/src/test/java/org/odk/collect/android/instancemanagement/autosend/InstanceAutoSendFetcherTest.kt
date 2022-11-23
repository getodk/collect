package org.odk.collect.android.instancemanagement.autosend

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.FormUtils.buildForm
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.formstest.InstanceUtils.buildInstance
import org.odk.collect.projects.Project
import org.odk.collect.shared.TempFiles.createTempDir

class InstanceAutoSendFetcherTest {
    private val autoSendSettingsProvider: AutoSendSettingsProvider = mock()
    private val instanceAutoSendFetcher = InstanceAutoSendFetcher(autoSendSettingsProvider)
    private val projectId = Project.DEMO_PROJECT_NAME
    private val instancesRepository = InMemInstancesRepository()
    private val formsRepository = InMemFormsRepository()

    private val formWithEnabledAutoSend = buildForm("1", "1", createTempDir().absolutePath, autosend = "true").build()

    private val instanceOfFormWithEnabledAutoSendIncomplete = buildInstance("1", "1", "instance 1", Instance.STATUS_INCOMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithEnabledAutoSendComplete = buildInstance("1", "1", "instance 2", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithEnabledAutoSendSubmissionFailed = buildInstance("1", "1", "instance 3", Instance.STATUS_SUBMISSION_FAILED, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithEnabledAutoSendSubmitted = buildInstance("1", "1", "instance 4", Instance.STATUS_SUBMITTED, null, createTempDir().absolutePath).build()

    private val formWithoutSpecifiedAutoSend = buildForm("2", "1", createTempDir().absolutePath).build()

    private val instanceOfFormWithoutSpecifiedAutoSendIncomplete = buildInstance("2", "1", "instance 1", Instance.STATUS_INCOMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithoutSpecifiedAutoSendComplete = buildInstance("2", "1", "instance 2", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithoutSpecifiedAutoSendSubmissionFailed = buildInstance("2", "1", "instance 3", Instance.STATUS_SUBMISSION_FAILED, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithoutSpecifiedAutoSendSubmitted = buildInstance("2", "1", "instance 4", Instance.STATUS_SUBMITTED, null, createTempDir().absolutePath).build()

    private val formWithDisabledAutoSend = buildForm("3", "1", createTempDir().absolutePath, autosend = "false").build()

    private val instanceOfFormWithDisabledAutoSendIncomplete = buildInstance("3", "1", "instance 1", Instance.STATUS_INCOMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithDisabledAutoSendComplete = buildInstance("3", "1", "instance 2", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithDisabledAutoSendSubmissionFailed = buildInstance("3", "1", "instance 3", Instance.STATUS_SUBMISSION_FAILED, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithDisabledAutoSendSubmitted = buildInstance("3", "1", "instance 4", Instance.STATUS_SUBMITTED, null, createTempDir().absolutePath).build()

    private val formWithCustomAutoSend = buildForm("4", "1", createTempDir().absolutePath, autosend = "anything").build()

    private val instanceOfFormWithCustomAutoSendIncomplete = buildInstance("4", "1", "instance 1", Instance.STATUS_INCOMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithCustomAutoSendComplete = buildInstance("4", "1", "instance 2", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithCustomAutoSendSubmissionFailed = buildInstance("4", "1", "instance 3", Instance.STATUS_SUBMISSION_FAILED, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithCustomAutoSendSubmitted = buildInstance("4", "1", "instance 4", Instance.STATUS_SUBMITTED, null, createTempDir().absolutePath).build()

    @Test
    fun `when auto-send enabled in settings return all finalized instances of forms that do not have auto send disabled on a form level`() {
        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId)).thenReturn(true)

        formsRepository.save(formWithEnabledAutoSend)
        formsRepository.save(formWithoutSpecifiedAutoSend)
        formsRepository.save(formWithDisabledAutoSend)
        formsRepository.save(formWithCustomAutoSend)

        instancesRepository.apply {
            save(instanceOfFormWithEnabledAutoSendIncomplete)
            save(instanceOfFormWithEnabledAutoSendComplete)
            save(instanceOfFormWithEnabledAutoSendSubmissionFailed)
            save(instanceOfFormWithEnabledAutoSendSubmitted)

            save(instanceOfFormWithoutSpecifiedAutoSendIncomplete)
            save(instanceOfFormWithoutSpecifiedAutoSendComplete)
            save(instanceOfFormWithoutSpecifiedAutoSendSubmissionFailed)
            save(instanceOfFormWithoutSpecifiedAutoSendSubmitted)

            save(instanceOfFormWithDisabledAutoSendIncomplete)
            save(instanceOfFormWithDisabledAutoSendComplete)
            save(instanceOfFormWithDisabledAutoSendSubmissionFailed)
            save(instanceOfFormWithDisabledAutoSendSubmitted)

            save(instanceOfFormWithCustomAutoSendIncomplete)
            save(instanceOfFormWithCustomAutoSendComplete)
            save(instanceOfFormWithCustomAutoSendSubmissionFailed)
            save(instanceOfFormWithCustomAutoSendSubmitted)
        }

        val instancesToSend = instanceAutoSendFetcher.getInstancesToAutoSend(projectId, instancesRepository, formsRepository)

        assertThat(instancesToSend.size, `is`(6))

        assertTrue(instancesToSend.contains(instanceOfFormWithEnabledAutoSendComplete))
        assertTrue(instancesToSend.contains(instanceOfFormWithEnabledAutoSendSubmissionFailed))

        assertTrue(instancesToSend.contains(instanceOfFormWithoutSpecifiedAutoSendComplete))
        assertTrue(instancesToSend.contains(instanceOfFormWithoutSpecifiedAutoSendSubmissionFailed))

        assertTrue(instancesToSend.contains(instanceOfFormWithCustomAutoSendComplete))
        assertTrue(instancesToSend.contains(instanceOfFormWithCustomAutoSendSubmissionFailed))
    }

    @Test
    fun `when auto-send disabled in settings return only those instances with auto-send enabled on a form level`() {
        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId)).thenReturn(false)

        formsRepository.save(formWithEnabledAutoSend)
        formsRepository.save(formWithoutSpecifiedAutoSend)
        formsRepository.save(formWithDisabledAutoSend)
        formsRepository.save(formWithCustomAutoSend)

        instancesRepository.apply {
            save(instanceOfFormWithEnabledAutoSendIncomplete)
            save(instanceOfFormWithEnabledAutoSendComplete)
            save(instanceOfFormWithEnabledAutoSendSubmissionFailed)
            save(instanceOfFormWithEnabledAutoSendSubmitted)

            save(instanceOfFormWithoutSpecifiedAutoSendIncomplete)
            save(instanceOfFormWithoutSpecifiedAutoSendComplete)
            save(instanceOfFormWithoutSpecifiedAutoSendSubmissionFailed)
            save(instanceOfFormWithoutSpecifiedAutoSendSubmitted)

            save(instanceOfFormWithDisabledAutoSendIncomplete)
            save(instanceOfFormWithDisabledAutoSendComplete)
            save(instanceOfFormWithDisabledAutoSendSubmissionFailed)
            save(instanceOfFormWithDisabledAutoSendSubmitted)

            save(instanceOfFormWithCustomAutoSendIncomplete)
            save(instanceOfFormWithCustomAutoSendComplete)
            save(instanceOfFormWithCustomAutoSendSubmissionFailed)
            save(instanceOfFormWithCustomAutoSendSubmitted)
        }

        val instancesToSend = instanceAutoSendFetcher.getInstancesToAutoSend(projectId, instancesRepository, formsRepository)

        assertThat(instancesToSend.size, `is`(2))
        assertTrue(instancesToSend.contains(instanceOfFormWithEnabledAutoSendComplete))
        assertTrue(instancesToSend.contains(instanceOfFormWithEnabledAutoSendSubmissionFailed))
    }

    @Test
    fun `enabling auto-send is not case sensitive`() {
        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId)).thenReturn(false)

        val formWithEnabledAutoSend = buildForm("1", "1", createTempDir().absolutePath, autosend = " TrUe ").build()
        val instanceOfFormWithEnabledAutoSendComplete = buildInstance("1", "1", "instance 1", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()

        formsRepository.save(formWithEnabledAutoSend)
        instancesRepository.save(instanceOfFormWithEnabledAutoSendComplete)

        val instancesToSend = instanceAutoSendFetcher.getInstancesToAutoSend(projectId, instancesRepository, formsRepository)

        assertThat(instancesToSend.size, `is`(1))
        assertTrue(instancesToSend.contains(instanceOfFormWithEnabledAutoSendComplete))
    }

    @Test
    fun `disabling auto-send is not case sensitive`() {
        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings(projectId)).thenReturn(true)

        val formWithEnabledAutoSend = buildForm("1", "1", createTempDir().absolutePath, autosend = " FaLsE ").build()
        val instanceOfFormWithEnabledAutoSendComplete = buildInstance("1", "1", "instance 1", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()

        formsRepository.save(formWithEnabledAutoSend)
        instancesRepository.save(instanceOfFormWithEnabledAutoSendComplete)

        val instancesToSend = instanceAutoSendFetcher.getInstancesToAutoSend(projectId, instancesRepository, formsRepository)

        assertTrue(instancesToSend.isEmpty())
    }

    @Test
    fun `if there are multiple versions of one form and only one has auto-send enabled take only instances of that form`() {
        val formWithEnabledAutoSendV1 = buildForm("1", "1", createTempDir().absolutePath, autosend = "false").build()
        val instanceOfFormWithEnabledAutoSendCompleteV1 = buildInstance("1", "1", "instance 2", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()

        val formWithEnabledAutoSendV2 = buildForm("1", "2", createTempDir().absolutePath, autosend = "true").build()
        val instanceOfFormWithEnabledAutoSendCompleteV2 = buildInstance("1", "2", "instance 2", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()

        formsRepository.save(formWithEnabledAutoSendV1)
        formsRepository.save(formWithEnabledAutoSendV2)

        instancesRepository.apply {
            save(instanceOfFormWithEnabledAutoSendCompleteV1)
            save(instanceOfFormWithEnabledAutoSendCompleteV2)
        }

        val instancesToSend = instanceAutoSendFetcher.getInstancesToAutoSend(projectId, instancesRepository, formsRepository)

        assertThat(instancesToSend.size, `is`(1))
        assertTrue(instancesToSend.contains(instanceOfFormWithEnabledAutoSendCompleteV2))
    }
}
