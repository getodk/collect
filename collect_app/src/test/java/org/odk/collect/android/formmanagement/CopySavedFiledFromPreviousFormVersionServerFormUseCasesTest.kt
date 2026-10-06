package org.odk.collect.android.formmanagement

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.forms.Form
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.formstest.FormUtils
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.shared.TempFiles
import java.io.File

class CopySavedFiledFromPreviousFormVersionServerFormUseCasesTest {

    @Test
    fun `#copySavedFileFromPreviousFormVersion does not copy any file if there is no matching last-saved file`() {
        val formsRepository = InMemFormsRepository()
        val form = formsRepository.save(FormFixtures.form())

        ServerFormUseCases.copySavedFileFromPreviousFormVersion(formsRepository, form)

        val resultFile = File(form.formMediaPath, FileUtils.LAST_SAVED_FILENAME)
        assertThat(resultFile.exists(), equalTo(false))
    }

    @Test
    fun `#copySavedFileFromPreviousFormVersion copies the newest matching last-saved file for given form`() {
        val tempDir1 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(tempDir1, "last-saved", ".xml")
        org.apache.commons.io.FileUtils.writeByteArrayToFile(file1, "file1".toByteArray())

        val tempDir2 = TempFiles.createTempDir()
        val file2 = TempFiles.createTempFile(tempDir2, "last-saved", ".xml")
        org.apache.commons.io.FileUtils.writeByteArrayToFile(file2, "file2".toByteArray())

        val tempDir3 = TempFiles.createTempDir()
        val file3 = TempFiles.createTempFile(tempDir3, "last-saved", ".xml")
        org.apache.commons.io.FileUtils.writeByteArrayToFile(file3, "file3".toByteArray())

        val tempDir4 = TempFiles.createTempDir()
        val file4 = TempFiles.createTempFile(tempDir4, "last-saved", ".xml")
        org.apache.commons.io.FileUtils.writeByteArrayToFile(file4, "file4".toByteArray())

        val formsRepository = InMemFormsRepository().also {
            it.save(
                Form.Builder()
                    .formId("1")
                    .version("1")
                    .date(0)
                    .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
                    .formMediaPath(file1.parent)
                    .build()
            )

            it.save(
                Form.Builder()
                    .formId("1")
                    .version("2")
                    .date(2)
                    .formFilePath(FormUtils.createXFormFile("1", "2").absolutePath)
                    .formMediaPath(file2.parent)
                    .build()
            )

            it.save(
                Form.Builder()
                    .formId("1")
                    .version("3")
                    .date(1)
                    .formFilePath(FormUtils.createXFormFile("1", "3").absolutePath)
                    .formMediaPath(file3.parent)
                    .build()
            )

            it.save(
                Form.Builder()
                    .formId("2")
                    .version("1")
                    .date(3)
                    .formFilePath(FormUtils.createXFormFile("2", "1").absolutePath)
                    .formMediaPath(file4.parent)
                    .build()
            )
        }

        val form = formsRepository.save(FormFixtures.form(formId = "1", version = "4"))

        ServerFormUseCases.copySavedFileFromPreviousFormVersion(formsRepository, form)

        val resultFile = File(form.formMediaPath, FileUtils.LAST_SAVED_FILENAME)
        assertThat(resultFile.readText(), equalTo("file2"))
    }

    @Test
    fun `#copySavedFileFromPreviousFormVersion does not override existing version`() {
        val tempDir1 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(tempDir1, "last-saved", ".xml")
        org.apache.commons.io.FileUtils.writeByteArrayToFile(file1, "file1".toByteArray())
        val formsRepository = InMemFormsRepository().also {
            it.save(
                Form.Builder()
                    .formId("1")
                    .version("1")
                    .date(1)
                    .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
                    .formMediaPath(file1.parent)
                    .build()
            )
        }

        val tempDir2 = TempFiles.createTempDir()
        val file2 = TempFiles.createTempFile(tempDir2, "last-saved", ".xml")
        org.apache.commons.io.FileUtils.writeByteArrayToFile(file2, "file2".toByteArray())
        val form = formsRepository.save(
            Form.Builder()
                .formId("1")
                .version("2")
                .date(2)
                .formFilePath(FormUtils.createXFormFile("1", "2").absolutePath)
                .formMediaPath(file2.parent)
                .build()
        )

        ServerFormUseCases.copySavedFileFromPreviousFormVersion(formsRepository, form)
        val resultFile = File(form.formMediaPath, FileUtils.LAST_SAVED_FILENAME)
        assertThat(resultFile.readText(), equalTo("file2"))
    }
}
