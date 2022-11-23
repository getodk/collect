package org.odk.collect.android.utilities

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.FormUtils
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.shared.TempFiles

class InstanceAutoDeleteCheckerTest {
    private val formsRepository = InMemFormsRepository()

    @Test
    fun `Instance should be deleted if auto-delete enabled in project settings and not set on a form level`() {
        val instance = saveInstance()
        assertTrue(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, true, instance))
    }

    @Test
    fun `Instance should be deleted if auto-delete enabled in project settings and set on a form level but with an unsupported value`() {
        val instance = saveInstance("anything")
        assertTrue(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, true, instance))
    }

    @Test
    fun `Instance should not be deleted if auto-delete enabled in project settings but disabled on a form level`() {
        val instance = saveInstance("false")
        assertFalse(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, true, instance))
    }

    @Test
    fun `Disabling aut-delete should not be case sensitive`() {
        val instance = saveInstance(" FaLsE ")
        assertFalse(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, true, instance))
    }

    @Test
    fun `Instance should be deleted if auto-delete enabled in project settings and on a form level`() {
        val instance = saveInstance("true")
        assertTrue(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, true, instance))
    }

    @Test
    fun `Instance should be deleted if auto-delete disabled in project settings but enabled on a form level`() {
        val instance = saveInstance("true")
        assertTrue(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, false, instance))
    }

    @Test
    fun `Enabling auto-delete should not be case sensitive`() {
        val instance = saveInstance(" TrUe ")
        assertTrue(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, false, instance))
    }

    @Test
    fun `Instance should not be deleted if auto-delete disabled in project settings and disabled on a form level`() {
        val instance = saveInstance("false")
        assertFalse(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, false, instance))
    }

    @Test
    fun `Instance should not be deleted if auto-delete disabled in project settings and not set on a form level`() {
        val instance = saveInstance()
        assertFalse(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, false, instance))
    }

    @Test
    fun `Instance should not be deleted if auto-delete disabled in project settings and set on a form level with unsupported value`() {
        val instance = saveInstance("anything")
        assertFalse(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, false, instance))
    }

    @Test
    fun `Only instances of a form version with auto-delete enabled should be deleted`() {
        val formV1 = formsRepository.save(
            Form.Builder()
                .formId("1")
                .version("1")
                .autoDelete("false")
                .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
                .build()
        )

        val instanceV1 = Instance.Builder()
            .formId(formV1.formId)
            .formVersion(formV1.version)
            .instanceFilePath(TempFiles.createTempDir().absolutePath)
            .build()

        val formV2 = formsRepository.save(
            Form.Builder(formV1)
                .version("2")
                .autoDelete("true")
                .build()
        )

        val instanceV2 = Instance.Builder()
            .formId(formV2.formId)
            .formVersion(formV2.version)
            .instanceFilePath(TempFiles.createTempDir().absolutePath)
            .build()

        assertFalse(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, false, instanceV1))
        assertTrue(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, false, instanceV2))
    }

    private fun saveInstance(autoDelete: String = ""): Instance {
        val form = formsRepository.save(
            Form.Builder()
                .formId("1")
                .version("1")
                .autoDelete(autoDelete)
                .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
                .build()
        )

        return Instance.Builder()
            .formId(form.formId)
            .formVersion(form.version)
            .instanceFilePath(TempFiles.createTempDir().absolutePath)
            .build()
    }
}
