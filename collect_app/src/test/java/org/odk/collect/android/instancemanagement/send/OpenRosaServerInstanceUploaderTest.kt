package org.odk.collect.android.instancemanagement.send

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.mock
import java.io.File

class OpenRosaServerInstanceUploaderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    /**
     * The attachment list must be deterministically ordered so the chunk boundaries are reproducible
     * across upload attempts (which is what makes resuming a chunked upload safe). This also confirms
     * the hidden `.upload_progress` resume file is not treated as an attachment (so it is never
     * uploaded), alongside the existing instance/submission XML exclusions.
     */
    @Test
    fun `getFilesInParentDirectory returns attachments sorted by name, ignoring hidden and xml files`() {
        val instanceDir = tempFolder.newFolder()
        val instanceFile = File(instanceDir, "instance.xml").apply { writeText("<x/>") }
        val submissionFile = File(instanceDir, "submission.xml").apply { writeText("<x/>") }

        // created in a deliberately non-sorted order
        File(instanceDir, "3.jpg").writeText("c")
        File(instanceDir, "1.jpg").writeText("a")
        File(instanceDir, "2.jpg").writeText("b")
        File(instanceDir, InstanceUploadProgressTracker.PROGRESS_FILE_NAME).writeText("progress")
        File(instanceDir, ".other").writeText("hidden")

        val uploader = OpenRosaServerInstanceUploader(mock(), mock())

        val files = uploader.getFilesInParentDirectory(instanceFile, submissionFile)!!

        assertThat(files.map { it.name }, equalTo(listOf("1.jpg", "2.jpg", "3.jpg")))
    }
}
