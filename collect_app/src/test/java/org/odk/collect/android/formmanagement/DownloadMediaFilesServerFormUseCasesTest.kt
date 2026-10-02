package org.odk.collect.android.formmanagement

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.odk.collect.entities.storage.InMemEntitiesRepository
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.ManifestFile
import org.odk.collect.forms.MediaFile
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.shared.TempFiles
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.File

class DownloadMediaFilesServerFormUseCasesTest {
    @Test
    fun `#downloadMediaFiles returns false when there is an existing copy of a media file and an older one`() {
        var date: Long = 0
        // Save forms
        val formsRepository = InMemFormsRepository {
            date += 1
            date
        }
        val form1 = FormFixtures.form(
            version = "1",
            mediaFiles = listOf(Pair("file", "old"))
        )
        formsRepository.save(form1)

        val form2 = FormFixtures.form(
            version = "2",
            mediaFiles = listOf(Pair("file", "existing"))
        )
        formsRepository.save(form2)

        // Set up same media file on server
        val existingMediaFileHash = File(form2.formMediaPath, "file").getMd5Hash()!!
        val mediaFile = MediaFile("file", existingMediaFileHash, "downloadUrl")
        val manifestFile = ManifestFile(null, listOf(mediaFile))
        val serverFormDetails =
            ServerFormDetails("blah", "http://example.com", "formId", "3", null, false, true, manifestFile)
        val formSource = mock<FormSource> {
            on { fetchMediaFile(mediaFile.downloadUrl) } doReturn "existing".toByteArray()
                .inputStream()
        }

        val tempMediaPath = File(TempFiles.createTempDir(), "temp").absolutePath
        val result = ServerFormUseCases.downloadMediaFiles(
            serverFormDetails,
            formSource,
            formsRepository,
            tempMediaPath,
            TempFiles.createTempDir(),
            InMemEntitiesRepository(),
            mock()
        )

        assertThat(result, equalTo(MediaFilesDownload(tempMediaPath, false, emptyList())))
    }

    @Test
    fun `#downloadMediaFiles returns false when there is an existing copy of a media file and an older one and media file list hash doesn't match existing copy`() {
        // Save forms
        var date: Long = 0
        val formsRepository = InMemFormsRepository {
            date += 1
            date
        }
        val form1 = FormFixtures.form(
            version = "1",
            mediaFiles = listOf(Pair("file", "old"))
        )
        formsRepository.save(form1)

        val form2 = FormFixtures.form(
            version = "2",
            mediaFiles = listOf(Pair("file", "existing"))
        )
        formsRepository.save(form2)

        // Set up same media file on server
        val mediaFile = MediaFile("file", "somethingElse", "downloadUrl")
        val manifestFile = ManifestFile(null, listOf(mediaFile))
        val serverFormDetails =
            ServerFormDetails("blah", "http://example.com", "formId", "3", null, false, true, manifestFile)
        val formSource = mock<FormSource> {
            on { fetchMediaFile(mediaFile.downloadUrl) } doReturn "existing".toByteArray()
                .inputStream()
        }

        val tempMediaPath = File(TempFiles.createTempDir(), "temp").absolutePath
        val result = ServerFormUseCases.downloadMediaFiles(
            serverFormDetails,
            formSource,
            formsRepository,
            tempMediaPath,
            TempFiles.createTempDir(),
            InMemEntitiesRepository(),
            mock()
        )

        assertThat(result, equalTo(MediaFilesDownload(tempMediaPath, false, emptyList())))
    }

    @Test
    fun `#downloadMediaFiles does not download an entity list when the local list hash matches`() {
        val formsRepository = InMemFormsRepository()
        val entitiesRepository = InMemEntitiesRepository()

        val listName = "list"
        val listHash = "hash"
        entitiesRepository.addList(listName)
        entitiesRepository.updateList(listName, hash = listHash, needsApproval = false)

        val mediaFile = MediaFile("$listName.csv", listHash, "downloadUrl", type = MediaFile.Type.ENTITY_LIST)
        val manifestFile = ManifestFile(null, listOf(mediaFile))
        val form =
            ServerFormDetails("blah", "http://example.com", "2", "1", null, true, false, manifestFile)
        val formSource = mock<FormSource> {
            on { fetchMediaFile(mediaFile.downloadUrl) } doAnswer {
                "name,label,__version".toByteArray().inputStream()
            }
        }

        val mediaFilesDownload = ServerFormUseCases.downloadMediaFiles(
            form,
            formSource,
            formsRepository,
            File(TempFiles.createTempDir(), "temp").absolutePath,
            TempFiles.createTempDir(),
            entitiesRepository,
            mock()
        )

        assertThat(mediaFilesDownload.entityLists.isNotEmpty(), equalTo(true))
        verify(formSource, never()).fetchMediaFile(mediaFile.downloadUrl)
    }
}
