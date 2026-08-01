package org.odk.collect.android.instancemanagement.send

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class InstanceUploadProgressTrackerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `a fresh upload starts from the first chunk and stores progress`() {
        val dir = tempFolder.newFolder()
        val tracker = InstanceUploadProgressTracker(dir)

        tracker.start("fingerprint-1")

        assertThat(tracker.getResumeFromChunkIndex(), equalTo(0))
        assertThat(File(dir, InstanceUploadProgressTracker.PROGRESS_FILE_NAME).exists(), equalTo(true))
    }

    @Test
    fun `the same fingerprint resumes from the chunk after the last uploaded one`() {
        val dir = tempFolder.newFolder()
        InstanceUploadProgressTracker(dir).apply {
            start("fingerprint-1")
            onChunkUploaded(0)
            onChunkUploaded(1)
        }

        val resumed = InstanceUploadProgressTracker(dir)
        resumed.start("fingerprint-1")

        assertThat(resumed.getResumeFromChunkIndex(), equalTo(2))
    }

    @Test
    fun `a different fingerprint restarts from the first chunk and resets stored progress`() {
        val dir = tempFolder.newFolder()
        InstanceUploadProgressTracker(dir).apply {
            start("fingerprint-1")
            onChunkUploaded(3)
        }

        val changed = InstanceUploadProgressTracker(dir)
        changed.start("fingerprint-2")
        assertThat(changed.getResumeFromChunkIndex(), equalTo(0))

        // Stored progress now belongs to the new fingerprint: a further retry with fingerprint-2
        // resumes from the start (nothing uploaded for it yet), not from fingerprint-1's chunk 3.
        val retry = InstanceUploadProgressTracker(dir)
        retry.start("fingerprint-2")
        assertThat(retry.getResumeFromChunkIndex(), equalTo(0))
    }

    @Test
    fun `clearing progress makes the next upload start from the first chunk`() {
        val dir = tempFolder.newFolder()
        InstanceUploadProgressTracker(dir).apply {
            start("fingerprint-1")
            onChunkUploaded(2)
            clear()
        }

        assertThat(File(dir, InstanceUploadProgressTracker.PROGRESS_FILE_NAME).exists(), equalTo(false))

        val next = InstanceUploadProgressTracker(dir)
        next.start("fingerprint-1")
        assertThat(next.getResumeFromChunkIndex(), equalTo(0))
    }

    @Test
    fun `the fingerprint is stable for the same files and changes when a file's content changes`() {
        val submission = tempFolder.newFile().apply { writeText("<x/>") }
        val a = tempFolder.newFile().apply { writeText("AAA") }
        val b = tempFolder.newFile().apply { writeText("BBB") }

        val fingerprint = InstanceUploadProgressTracker.computeUploadFingerprint(submission, listOf(a, b))
        val recomputed = InstanceUploadProgressTracker.computeUploadFingerprint(submission, listOf(a, b))
        assertThat(recomputed, equalTo(fingerprint))

        a.writeText("AAAA")
        val afterContentChange = InstanceUploadProgressTracker.computeUploadFingerprint(submission, listOf(a, b))
        assertThat(afterContentChange, not(equalTo(fingerprint)))
    }

    @Test
    fun `the fingerprint changes when the set of files changes`() {
        val submission = tempFolder.newFile().apply { writeText("<x/>") }
        val a = tempFolder.newFile().apply { writeText("AAA") }
        val b = tempFolder.newFile().apply { writeText("BBB") }

        val withOne = InstanceUploadProgressTracker.computeUploadFingerprint(submission, listOf(a))
        val withTwo = InstanceUploadProgressTracker.computeUploadFingerprint(submission, listOf(a, b))

        assertThat(withTwo, not(equalTo(withOne)))
    }
}
