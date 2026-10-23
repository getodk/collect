package org.odk.collect.android.backgroundwork

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.android.formmanagement.EntityListDownload
import org.odk.collect.android.formmanagement.FormDownload
import org.odk.collect.android.formmanagement.MediaFilesDownload
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.shared.TempFiles

class FormDownloadSerializerTest {

    private val formsRepository = InMemFormsRepository()

    @Test
    fun `serializes and deserializes New`() {
        val formDownload = FormDownload.New(
            file = TempFiles.createTempFile(),
            hash = "hash",
            tempDir = TempFiles.createTempDir(),
            mediaFilesDownload = MediaFilesDownload(
                tempMediaPath = TempFiles.createTempDir().absolutePath,
                newAttachmentsDownloaded = false,
                entityLists = listOf(
                    EntityListDownload.Update(
                        mediaFile = FormFixtures.mediaFile(),
                        file = TempFiles.createTempFile()
                    )
                )
            )
        )

        val serialized = FormDownloadSerializer.serialize(formDownload)
        assertThat(
            FormDownloadSerializer.deserialize(serialized, formsRepository),
            equalTo(formDownload)
        )
    }

    @Test
    fun `serializes and deserializes Existing`() {
        val form = formsRepository.save(FormFixtures.form())

        val formDownload = FormDownload.Existing(
            form = form,
            tempDir = TempFiles.createTempDir(),
            mediaFilesDownload = MediaFilesDownload(
                tempMediaPath = TempFiles.createTempDir().absolutePath,
                newAttachmentsDownloaded = false,
                entityLists = listOf(
                    EntityListDownload.Update(
                        mediaFile = FormFixtures.mediaFile(),
                        file = TempFiles.createTempFile()
                    )
                )
            )
        )

        val serialized = FormDownloadSerializer.serialize(formDownload)
        assertThat(
            FormDownloadSerializer.deserialize(serialized, formsRepository),
            equalTo(formDownload)
        )
    }
}
