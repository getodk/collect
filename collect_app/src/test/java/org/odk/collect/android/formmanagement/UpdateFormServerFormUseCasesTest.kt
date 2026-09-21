package org.odk.collect.android.formmanagement

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.entities.storage.InMemEntitiesRepository
import org.odk.collect.forms.MediaFile
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.shared.TempFiles

class UpdateFormServerFormUseCasesTest {

    @Test
    fun `#updateForm uses the time from last attachments update if it is newer than the entity list updates`() {
        val formsRepository = InMemFormsRepository { 8 }
        val entitiesRepository = InMemEntitiesRepository { 9 }

        val form = formsRepository.save(FormFixtures.form())
        entitiesRepository.addList("fields")
        entitiesRepository.updateList("fields", "hash", false)

        val formFileDownload = FormFileDownload.Existing(form)
        val mediaFileDownload = MediaFilesDownload(
            TempFiles.createTempFile().absolutePath,
            true,
            listOf(
                EntityListDownload.Skipped(MediaFile("fields.csv", "hash", "http://example.com"))
            )
        )

        ServerFormUseCases.updateForm(
            formFileDownload,
            mediaFileDownload,
            entitiesRepository,
            formsRepository,
            10
        )

        assertThat(formsRepository.get(form.dbId)?.lastDetectedAttachmentsUpdateDate, equalTo(10))
    }

    @Test
    fun `#updateForm uses the time from entity list update if it is newer than attachment update`() {
        val formsRepository = InMemFormsRepository { 8 }
        val entitiesRepository = InMemEntitiesRepository { 11 }

        val form = formsRepository.save(FormFixtures.form())
        entitiesRepository.addList("fields")
        entitiesRepository.updateList("fields", "hash", false)

        val formFileDownload = FormFileDownload.Existing(form)
        val mediaFileDownload = MediaFilesDownload(
            TempFiles.createTempFile().absolutePath,
            true,
            listOf(
                EntityListDownload.Skipped(MediaFile("fields.csv", "hash", "http://example.com"))
            )
        )

        ServerFormUseCases.updateForm(
            formFileDownload,
            mediaFileDownload,
            entitiesRepository,
            formsRepository,
            10
        )

        assertThat(formsRepository.get(form.dbId)?.lastDetectedAttachmentsUpdateDate, equalTo(11))
    }
}