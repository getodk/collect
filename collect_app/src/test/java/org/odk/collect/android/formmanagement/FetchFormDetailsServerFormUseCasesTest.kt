package org.odk.collect.android.formmanagement

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.formmanagement.support.FormListFixtures
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormsRepository
import org.odk.collect.formstest.FormUtils
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.shared.TempFiles
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class FetchFormDetailsServerFormUseCasesTest {

    private val formsRepository: FormsRepository = InMemFormsRepository()
    private val formSource = mock<FormSource>()

    @Test
    fun whenFormHasManifestUrl_returnsMediaFilesInDetails() {
        val form = FormListFixtures.formListItem()
        val manifestUrl = "http://example.com/manifest"
        val formWithManifest = FormListFixtures.formListItem(manifestUrl = manifestUrl)
        whenever(formSource.fetchFormList()).thenReturn(listOf(form, formWithManifest))

        val mediaFile = FormListFixtures.mediaFile()
        whenever(formSource.fetchManifest(manifestUrl)).thenReturn(
            FormListFixtures.manifestFile(listOf(mediaFile))
        )

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        assertThat(getFormFromList(serverFormDetails, form.formID).manifest, nullValue())
        assertThat(
            getFormFromList(serverFormDetails, formWithManifest.formID).manifest!!.mediaFiles,
            contains(mediaFile)
        )
    }

    @Test
    fun whenFormDoesNotExist_isNew() {
        val form = FormListFixtures.formListItem()
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.New))
    }

    @Test
    fun whenAFormIsSoftDeleted_isNew() {
        val form = FormListFixtures.formListItem()
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))
        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version(form.version)
                .md5Hash(form.hash)
                .deleted(true)
                .formFilePath(FormUtils.createXFormFile(form.formID, form.hash).absolutePath)
                .build()
        )

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.New))
    }

    @Test
    fun whenAFormExists_andListContainsNewVersionWithDifferentHash_isUpdatedVersion() {
        val form = FormListFixtures.formListItem()
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))
        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version("old")
                .md5Hash("old")
                .formFilePath(FormUtils.createXFormFile(form.formID, "old").absolutePath)
                .build()
        )

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.UpdatedVersion))
    }

    @Test
    fun whenAFormExists_andListContainsSameVersionWithDifferentHash_isUpdatedHash() {
        val form = FormListFixtures.formListItem()
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))
        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version(form.version)
                .md5Hash("new")
                .formFilePath(FormUtils.createXFormFile(form.formID, "new").absolutePath)
                .build()
        )

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.UpdatedHash))
    }

    @Test
    fun whenAFormExists_andHasNewMediaFileOnServer_isUpdatedMedia() {
        val manifestUrl = "http://example.com/manifest"
        val form = FormListFixtures.formListItem(manifestUrl = manifestUrl)
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))

        val mediaFile = FormListFixtures.mediaFile()
        whenever(formSource.fetchManifest(manifestUrl)).thenReturn(
            FormListFixtures.manifestFile(listOf(mediaFile))
        )

        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version(form.version)
                .md5Hash(form.hash)
                .formFilePath(FormUtils.createXFormFile(form.formID, form.version).absolutePath)
                .formMediaPath("/does-not-exist")
                .build()
        )

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.UpdatedMedia))
    }

    @Test
    fun whenAFormExists_andHasUpdatedMediaFileOnServer_isUpdatedMedia() {
        val manifestUrl = "http://example.com/manifest"
        val form = FormListFixtures.formListItem(manifestUrl = manifestUrl)
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))

        val mediaFile = FormListFixtures.mediaFile()
        whenever(formSource.fetchManifest(manifestUrl)).thenReturn(
            FormListFixtures.manifestFile(listOf(mediaFile))
        )

        val mediaDir = TempFiles.createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version(form.version)
                .md5Hash(form.hash)
                .formFilePath(FormUtils.createXFormFile(form.formID, form.version).absolutePath)
                .formMediaPath(mediaDir.absolutePath)
                .build()
        )

        val oldMediaFile = TempFiles.createTempFile(mediaDir, mediaFile.filename)
        writeToFile(oldMediaFile, "blah before")

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.UpdatedMedia))
    }

    @Test
    fun whenAFormExists_andItsNewerVersionWithUpdatedMediaFilesHasBeenAlreadyDownloaded_isOnDevice() {
        val manifestUrl = "http://example.com/manifest"
        val form = FormListFixtures.formListItem(manifestUrl = manifestUrl)
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))

        val mediaFile = FormListFixtures.mediaFile()
        whenever(formSource.fetchManifest(manifestUrl)).thenReturn(
            FormListFixtures.manifestFile(listOf(mediaFile))
        )

        val mediaDir1 = TempFiles.createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version("1")
                .md5Hash("${form.formID}_v1-hash")
                .formFilePath(FormUtils.createXFormFile(form.formID, "1").absolutePath)
                .formMediaPath(mediaDir1.absolutePath)
                .build()
        )

        val mediaFile1 = TempFiles.createTempFile(mediaDir1, mediaFile.filename)
        writeToFile(mediaFile1, "old blah")

        val mediaDir2 = TempFiles.createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version(form.version)
                .md5Hash(form.hash)
                .formFilePath(FormUtils.createXFormFile(form.formID, form.version).absolutePath)
                .formMediaPath(mediaDir2.absolutePath)
                .build()
        )
        val mediaFile2 = TempFiles.createTempFile(mediaDir2, mediaFile.filename)
        writeToFile(mediaFile2, "blah")

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.OnDevice))
    }

    @Test
    fun whenAFormExists_andItsNewerVersionHasBeenAlreadyDownloadedButThenSoftDeleted_isNew() {
        val form = FormListFixtures.formListItem()
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))

        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version("old")
                .md5Hash("old")
                .formFilePath(FormUtils.createXFormFile(form.formID, "old").absolutePath)
                .build()
        )
        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version(form.version)
                .md5Hash(form.hash)
                .formFilePath(FormUtils.createXFormFile(form.formID, form.version).absolutePath)
                .deleted(true)
                .build()
        )

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.New))
    }

    @Test
    fun whenAFormExists_andItsNewerVersionWithMediaFilesHasBeenAlreadyDownloadedButThenSoftDeleted_isNew() {
        val manifestUrl = "http://example.com/manifest"
        val form = FormListFixtures.formListItem(manifestUrl = manifestUrl)
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))

        val mediaFile = FormListFixtures.mediaFile()
        whenever(formSource.fetchManifest(manifestUrl)).thenReturn(
            FormListFixtures.manifestFile(listOf(mediaFile))
        )

        val mediaDir1 = TempFiles.createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version("old")
                .md5Hash("old")
                .formFilePath(FormUtils.createXFormFile(form.formID, "old").absolutePath)
                .formMediaPath(mediaDir1.absolutePath)
                .build()
        )

        val mediaFile1 = TempFiles.createTempFile(mediaDir1, mediaFile.filename)
        writeToFile(mediaFile1, "blah")

        val mediaDir2 = TempFiles.createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version(form.version)
                .md5Hash(form.hash)
                .formFilePath(FormUtils.createXFormFile(form.formID, form.version).absolutePath)
                .formMediaPath(mediaDir2.absolutePath)
                .deleted(true)
                .build()
        )
        val mediaFile2 = TempFiles.createTempFile(mediaDir2, mediaFile.filename)
        writeToFile(mediaFile2, "blah")

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.New))
    }

    @Test
    fun whenAFormExists_andIsNotUpdatedOnServer_andDoesNotHaveAManifest_isOnDevice() {
        val form = FormListFixtures.formListItem()
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))

        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version(form.version)
                .md5Hash(form.hash)
                .formFilePath(FormUtils.createXFormFile(form.formID, form.version).absolutePath)
                .build()
        )

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.OnDevice))
    }

    @Test
    fun whenFormExists_andMediaFilesExist_isOnDevice() {
        val manifestUrl = "http://example.com/manifest"
        val form = FormListFixtures.formListItem(manifestUrl = manifestUrl)
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))

        val mediaFile = FormListFixtures.mediaFile()
        whenever(formSource.fetchManifest(manifestUrl)).thenReturn(
            FormListFixtures.manifestFile(listOf(mediaFile))
        )

        val mediaDir = TempFiles.createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version(form.version)
                .md5Hash(form.hash)
                .formFilePath(FormUtils.createXFormFile(form.formID, form.version).absolutePath)
                .formMediaPath(mediaDir.absolutePath)
                .build()
        )

        val localMediaFile = TempFiles.createTempFile(mediaDir, mediaFile.filename)
        writeToFile(localMediaFile, "blah")

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.OnDevice))
    }

    @Test
    fun whenAFormExists_andIsUpdatedOnServer_andDoesNotHaveNewMedia_isUpdatedVersion() {
        val manifestUrl = "http://example.com/manifest"
        val form = FormListFixtures.formListItem(manifestUrl = manifestUrl)
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))

        val mediaFile = FormListFixtures.mediaFile()
        whenever(formSource.fetchManifest(manifestUrl)).thenReturn(
            FormListFixtures.manifestFile(listOf(mediaFile))
        )

        val mediaDir = TempFiles.createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version("old")
                .md5Hash("old")
                .formFilePath(FormUtils.createXFormFile(form.formID, "old").absolutePath)
                .formMediaPath(mediaDir.absolutePath)
                .build()
        )

        val localMediaFile = TempFiles.createTempFile(mediaDir, mediaFile.filename)
        writeToFile(localMediaFile, "blah")

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.UpdatedVersion))
    }

    @Test
    fun whenAFormExists_andItsNewerVersionWithManifestIsAvailableButHasNullHash_isOnDevice() {
        val form =
            FormListFixtures.formListItem(manifestUrl = "http://example.com/manifest", hash = null)
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))

        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version("old")
                .md5Hash(form.hash)
                .formFilePath(FormUtils.createXFormFile(form.formID, "old").absolutePath)
                .build()
        )

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.OnDevice))
    }

    private fun writeToFile(mediaFile: File, content: String) {
        val bw = BufferedWriter(FileWriter(mediaFile))
        bw.write(content)
        bw.close()
    }

    private fun getFormFromList(
        serverFormDetails: List<ServerFormDetails>,
        formId: String
    ): ServerFormDetails {
        return serverFormDetails.stream().filter { it.formId == formId }.findAny().get()
    }
}
