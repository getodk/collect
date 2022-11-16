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
        formsRepository.save(
            Form.Builder()
                .formId("1")
                .version("1")
                .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
                .build()
        )

        val instance = Instance.Builder()
            .formId("1")
            .formVersion("1")
            .instanceFilePath(TempFiles.createTempDir().absolutePath)
            .build()

        assertTrue(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, true, instance))
    }

    @Test
    fun `Instance should be deleted if auto-delete enabled in project settings and set on a form level but with an unsupported value`() {
        formsRepository.save(
            Form.Builder()
                .formId("1")
                .version("1")
                .autoDelete("anything")
                .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
                .build()
        )

        val instance = Instance.Builder()
            .formId("1")
            .formVersion("1")
            .instanceFilePath(TempFiles.createTempDir().absolutePath)
            .build()

        assertTrue(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, true, instance))
    }

    @Test
    fun `Instance should not be deleted if auto-delete enabled in project settings but disabled on a form level`() {
        formsRepository.save(
            Form.Builder()
                .formId("1")
                .version("1")
                .autoDelete("false")
                .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
                .build()
        )

        val instance = Instance.Builder()
            .formId("1")
            .formVersion("1")
            .instanceFilePath(TempFiles.createTempDir().absolutePath)
            .build()

        assertFalse(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, true, instance))
    }

    @Test
    fun `Instance should be deleted if auto-delete disabled in project settings but enabled on a form level`() {
        formsRepository.save(
            Form.Builder()
                .formId("1")
                .version("1")
                .autoDelete("true")
                .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
                .build()
        )

        val instance = Instance.Builder()
            .formId("1")
            .formVersion("1")
            .instanceFilePath(TempFiles.createTempDir().absolutePath)
            .build()

        assertTrue(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, false, instance))
    }

    @Test
    fun `Instance should not be deleted if auto-delete disabled in project settings and disabled on a form level`() {
        formsRepository.save(
            Form.Builder()
                .formId("1")
                .version("1")
                .autoDelete("false")
                .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
                .build()
        )

        val instance = Instance.Builder()
            .formId("1")
            .formVersion("1")
            .instanceFilePath(TempFiles.createTempDir().absolutePath)
            .build()

        assertFalse(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, false, instance))
    }

    @Test
    fun `Instance should not be deleted if auto-delete disabled in project settings and not set on a form level`() {
        formsRepository.save(
            Form.Builder()
                .formId("1")
                .version("1")
                .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
                .build()
        )

        val instance = Instance.Builder()
            .formId("1")
            .formVersion("1")
            .instanceFilePath(TempFiles.createTempDir().absolutePath)
            .build()

        assertFalse(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, false, instance))
    }

    @Test
    fun `Instance should not be deleted if auto-delete disabled in project settings and set on a form level with unsupported value`() {
        formsRepository.save(
            Form.Builder()
                .formId("1")
                .version("1")
                .autoDelete("anything")
                .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
                .build()
        )

        val instance = Instance.Builder()
            .formId("1")
            .formVersion("1")
            .instanceFilePath(TempFiles.createTempDir().absolutePath)
            .build()

        assertFalse(InstanceAutoDeleteChecker.shouldInstanceBeDeleted(formsRepository, false, instance))
    }
}
