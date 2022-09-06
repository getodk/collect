package org.odk.collect.android.instancemanagement.autosend

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertTrue
import org.junit.Before
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
    private lateinit var instanceAutoSendFetcher: InstanceAutoSendFetcher
    private val autoSendSettingChecker: AutoSendSettingChecker = mock()
    private val projectId = Project.DEMO_PROJECT_NAME
    private val instancesRepository = InMemInstancesRepository()
    private val formsRepository = InMemFormsRepository()

    private val formWithAutoSend = buildForm("1", "1", createTempDir().absolutePath, autosend = "true").build()

    private val instanceOfFormWithAutoSendIncomplete = buildInstance("1", "1", "instance 1", Instance.STATUS_INCOMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithAutoSendComplete = buildInstance("1", "1", "instance 2", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithAutoSendSubmissionFailed = buildInstance("1", "1", "instance 3", Instance.STATUS_SUBMISSION_FAILED, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithAutoSendSubmitted = buildInstance("1", "1", "instance 4", Instance.STATUS_SUBMITTED, null, createTempDir().absolutePath).build()

    private val formWithoutAutoSend = buildForm("2", "1", createTempDir().absolutePath).build()

    private val instanceOfFormWithoutAutoSendIncomplete = buildInstance("2", "1", "instance 1", Instance.STATUS_INCOMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithoutAutoSendComplete = buildInstance("2", "1", "instance 2", Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithoutAutoSendSubmissionFailed = buildInstance("2", "1", "instance 3", Instance.STATUS_SUBMISSION_FAILED, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithoutAutoSendSubmitted = buildInstance("2", "1", "instance 4", Instance.STATUS_SUBMITTED, null, createTempDir().absolutePath).build()

    @Before
    fun setup() {
        instanceAutoSendFetcher = InstanceAutoSendFetcher(autoSendSettingChecker)

        formsRepository.save(formWithAutoSend)
        formsRepository.save(formWithoutAutoSend)

        instancesRepository.apply {
            save(instanceOfFormWithAutoSendIncomplete)
            save(instanceOfFormWithAutoSendComplete)
            save(instanceOfFormWithAutoSendSubmissionFailed)
            save(instanceOfFormWithAutoSendSubmitted)

            save(instanceOfFormWithoutAutoSendIncomplete)
            save(instanceOfFormWithoutAutoSendComplete)
            save(instanceOfFormWithoutAutoSendSubmissionFailed)
            save(instanceOfFormWithoutAutoSendSubmitted)
        }
    }

    @Test
    fun `when autosend enabled in settings return all finalized forms`() {
        whenever(autoSendSettingChecker.isAutoSendEnabledInSettings(projectId)).thenReturn(true)

        val instancesToSend = instanceAutoSendFetcher.getInstancesToAutoSend(projectId, instancesRepository, formsRepository)

        assertThat(instancesToSend.size, `is`(4))
        assertTrue(instancesToSend.contains(instanceOfFormWithAutoSendComplete))
        assertTrue(instancesToSend.contains(instanceOfFormWithAutoSendSubmissionFailed))
        assertTrue(instancesToSend.contains(instanceOfFormWithoutAutoSendComplete))
        assertTrue(instancesToSend.contains(instanceOfFormWithoutAutoSendSubmissionFailed))
    }

    @Test
    fun `when autosend disabled in settings return only those instances with autosend enabled on a form level`() {
        whenever(autoSendSettingChecker.isAutoSendEnabledInSettings(projectId)).thenReturn(false)

        val instancesToSend = instanceAutoSendFetcher.getInstancesToAutoSend(projectId, instancesRepository, formsRepository)

        assertThat(instancesToSend.size, `is`(2))
        assertTrue(instancesToSend.contains(instanceOfFormWithAutoSendComplete))
        assertTrue(instancesToSend.contains(instanceOfFormWithAutoSendSubmissionFailed))
    }
}
