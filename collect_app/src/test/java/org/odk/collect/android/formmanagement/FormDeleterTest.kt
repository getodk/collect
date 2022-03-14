package org.odk.collect.android.formmanagement

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.FormUtils
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.formstest.InstanceUtils

class FormDeleterTest {
    private val formsRepository = InMemFormsRepository()
    private val instancesRepository = InMemInstancesRepository()
    private val formDeleter = FormDeleter(formsRepository, instancesRepository)

    @Test
    fun whenFormHasDeletedInstances_deletesForm() {
        val formToDelete = formsRepository.save(
            Form.Builder()
                .formId("id")
                .version("version")
                .formFilePath(FormUtils.createXFormFile("id", "version").absolutePath)
                .build()
        )
        instancesRepository.save(
            Instance.Builder()
                .formId("id")
                .formVersion("version")
                .deletedDate(0L)
                .build()
        )
        formDeleter.delete(formToDelete.dbId)
        assertThat(formsRepository.all.size, `is`(0))
    }

    @Test
    fun whenOtherVersionOfFormHasInstances_deletesForm() {
        formsRepository.save(
            Form.Builder()
                .formId("1")
                .version("old")
                .formFilePath(FormUtils.createXFormFile("1", "old").absolutePath)
                .build()
        )
        val formToDelete = formsRepository.save(
            Form.Builder()
                .formId("1")
                .version("new")
                .formFilePath(FormUtils.createXFormFile("1", "new").absolutePath)
                .build()
        )
        instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .formVersion("old")
                .build()
        )
        formDeleter.delete(formToDelete.dbId)
        val forms = formsRepository.all
        assertThat(forms.size, `is`(1))
        assertThat(forms[0].version, `is`("old"))
    }

    @Test
    fun whenFormHasNullVersion_butAnotherVersionHasInstances_deletesForm() {
        formsRepository.save(
            Form.Builder()
                .formId("1")
                .version("version")
                .formFilePath(FormUtils.createXFormFile("1", "version").absolutePath)
                .build()
        )
        val formToDelete = formsRepository.save(
            Form.Builder()
                .formId("1")
                .version(null)
                .formFilePath(FormUtils.createXFormFile("1", null).absolutePath)
                .build()
        )
        instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .formVersion("version")
                .build()
        )
        formDeleter.delete(formToDelete.dbId)
        val forms = formsRepository.all
        assertThat(forms.size, `is`(1))
        assertThat(forms[0].version, `is`("version"))
    }

    @Test
    fun whenFormHasNullVersion_andInstancesWithNullVersion_softDeletesForm() {
        val formToDelete = formsRepository.save(
            Form.Builder()
                .formId("1")
                .version(null)
                .formFilePath(FormUtils.createXFormFile("1", null).absolutePath)
                .build()
        )
        instancesRepository.save(
            InstanceUtils.buildInstance(
                "1",
                null,
                createTempDir().absolutePath
            ).build()
        )
        formDeleter.delete(formToDelete.dbId)
        val forms = formsRepository.all
        assertThat(forms.size, `is`(1))
        assertThat(forms[0].isDeleted, `is`(true))
    }
}
