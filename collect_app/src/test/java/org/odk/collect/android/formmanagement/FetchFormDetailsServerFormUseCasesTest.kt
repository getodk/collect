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
import org.odk.collect.shared.strings.Md5.getMd5Hash

class FetchFormDetailsServerFormUseCasesTest {

    private val formsRepository: FormsRepository = InMemFormsRepository()
    private val formSource = mock<FormSource>()

    @Test
    fun `#fetchFormDetails returns media files in details when form has manifest URL`() {
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
    fun `#fetchFormDetails returns New type when form does not exist`() {
        val form = FormListFixtures.formListItem()
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.New))
    }

    @Test
    fun `#fetchFormDetails returns New type when a form is soft deleted`() {
        val form = FormListFixtures.formListItem()
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))
        formsRepository.save(
            Form.Builder()
                .formId(form.formID)
                .version(form.version)
                .md5Hash(form.hash)
                .deleted(true)
                .formFilePath(FormUtils.createXFormFile(form.formID, form.version).absolutePath)
                .build()
        )

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.New))
    }

    @Test
    fun `#fetchFormDetails returns UpdatedVersion type when a form exists and list contains new version with different hash`() {
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
    fun `#fetchFormDetails returns UpdatedHash type when a form exists and list contains same version with different hash`() {
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
    fun `#fetchFormDetails returns UpdatedMedia type when a form exists and has new media file on server`() {
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
    fun `#fetchFormDetails returns UpdatedMedia type when a form exists and has updated media file on server`() {
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
        oldMediaFile.writeText("blah before")

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.UpdatedMedia))
    }

    @Test
    fun `#fetchFormDetails returns OnDevice type when a form exists and its newer version with updated media files has been already downloaded`() {
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
        mediaFile1.writeText("old blah")

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
        mediaFile2.writeText("blah")

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.OnDevice))
    }

    @Test
    fun `#fetchFormDetails returns New type when a form exists and its newer version has been already downloaded but then soft deleted`() {
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
    fun `#fetchFormDetails returns New type when a form exists and its newer version with media files has been already downloaded but then soft deleted`() {
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
        mediaFile1.writeText("blah")

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
        mediaFile2.writeText("blah")

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.New))
    }

    @Test
    fun `#fetchFormDetails returns OnDevice type when a form exists and is not updated on server and does not have a manifest`() {
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
    fun `#fetchFormDetails returns OnDevice type when form exists and media files exist`() {
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
        localMediaFile.writeText("blah")

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.OnDevice))
    }

    @Test
    fun `#fetchFormDetails returns UpdatedVersion type when a form exists and is updated on server and does not have new media`() {
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
        localMediaFile.writeText("blah")

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.UpdatedVersion))
    }

    @Test
    fun `#fetchFormDetails returns OnDevice type when a form exists and its newer version with manifest is available but has null hash`() {
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

    @Test
    fun `#fetchFormDetails returns UpdatedMedia type when a form exists and there is a new media file with the same contents as an existing one`() {
        val manifestUrl = "http://example.com/manifest"
        val form = FormListFixtures.formListItem(manifestUrl = manifestUrl)
        whenever(formSource.fetchFormList()).thenReturn(listOf(form))

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

        val localMediaFile = TempFiles.createTempFile(mediaDir, "file1")
        localMediaFile.writeText("blah")
        val localMediaFileHash = localMediaFile.getMd5Hash()!!

        val mediaFile1 = FormListFixtures.mediaFile(name = "file1", localMediaFileHash)
        val mediaFile2 = FormListFixtures.mediaFile(name = "file2", localMediaFileHash)
        whenever(formSource.fetchManifest(manifestUrl)).thenReturn(
            FormListFixtures.manifestFile(listOf(mediaFile1, mediaFile2))
        )

        val serverFormDetails = ServerFormUseCases.fetchFormDetails(formsRepository, formSource)
        val formDetails = getFormFromList(serverFormDetails, form.formID)
        assertThat(formDetails.type, equalTo(ServerFormDetails.Type.UpdatedMedia))
    }

    private fun getFormFromList(
        serverFormDetails: List<ServerFormDetails>,
        formId: String
    ): ServerFormDetails {
        return serverFormDetails.stream().filter { it.formId == formId }.findAny().get()
    }
}
