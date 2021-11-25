package org.odk.collect.android.formmanagement

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormListItem
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.ManifestFile
import org.odk.collect.forms.MediaFile
import org.odk.collect.formstest.FormUtils
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.shared.TempFiles
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileWriter

class ServerFormsDetailsFetcherTest {

    private val formsRepository: FormsRepository = InMemFormsRepository()
    private val diskFormsSynchronizer = mock<DiskFormsSynchronizer>()
    private val formSource = mock<FormSource> {
        on { fetchManifest(MANIFEST_URL) } doReturn ManifestFile(
            "manifest-hash",
            listOf(
                MEDIA_FILE
            )
        )
    }

    private val fetcher =
        ServerFormsDetailsFetcher(formsRepository, formSource, diskFormsSynchronizer)

    @Test
    fun whenFormHasManifestUrl_returnsMediaFilesInDetails() {
        whenever(formSource.fetchFormList()).thenReturn(
            listOf(FORM_WITHOUT_MANIFEST, FORM_WITH_MANIFEST)
        )

        val serverFormDetails = fetcher.fetchFormDetails()
        assertThat(getFormFromList(serverFormDetails, "form-1").manifest, nullValue())
        assertThat(
            getFormFromList(serverFormDetails, "form-2").manifest!!.mediaFiles,
            contains(MEDIA_FILE)
        )
    }

    @Test
    fun whenFormDoesNotExist_isNotOnDevice() {
        whenever(formSource.fetchFormList()).thenReturn(listOf(FORM_WITHOUT_MANIFEST))

        val serverFormDetails = fetcher.fetchFormDetails()
        assertThat(getFormFromList(serverFormDetails, "form-1").isNotOnDevice, `is`(true))
    }

    @Test
    fun whenAFormIsSoftDeleted_isNotOnDevice() {
        whenever(formSource.fetchFormList()).thenReturn(listOf(FORM_WITHOUT_MANIFEST))
        formsRepository.save(
            Form.Builder()
                .formId("form-1")
                .version("server")
                .md5Hash("form-1-hash")
                .deleted(true)
                .formFilePath(FormUtils.createXFormFile("form-1", "server").absolutePath)
                .build()
        )

        val serverFormDetails = fetcher.fetchFormDetails()
        assertThat(getFormFromList(serverFormDetails, "form-1").isNotOnDevice, `is`(true))
    }

    @Test
    fun whenAFormExists_andListContainsVersionWithDifferentHash_isUpdated() {
        whenever(formSource.fetchFormList()).thenReturn(listOf(FORM_WITHOUT_MANIFEST))
        formsRepository.save(
            Form.Builder()
                .formId("form-1")
                .md5Hash("form-1-hash-old")
                .formFilePath(FormUtils.createXFormFile("form-1", null).absolutePath)
                .build()
        )

        val serverFormDetails = fetcher.fetchFormDetails()
        assertThat(getFormFromList(serverFormDetails, "form-1").isUpdated, `is`(true))
    }

    @Test
    fun whenAFormExists_andHasNewMediaFileOnServer_isUpdated() {
        whenever(formSource.fetchFormList()).thenReturn(listOf(FORM_WITH_MANIFEST))
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .version("2")
                .md5Hash("form-2-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").absolutePath)
                .formMediaPath("/does-not-exist")
                .build()
        )

        val serverFormDetails = fetcher.fetchFormDetails()
        assertThat(getFormFromList(serverFormDetails, "form-2").isUpdated, `is`(true))
    }

    @Test
    fun whenAFormExists_andHasUpdatedMediaFileOnServer_isUpdated() {
        whenever(formSource.fetchFormList()).thenReturn(listOf(FORM_WITH_MANIFEST))

        val mediaDir = TempFiles.createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .version("2")
                .md5Hash("form-2-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").absolutePath)
                .formMediaPath(mediaDir.absolutePath)
                .build()
        )
        val oldMediaFile = TempFiles.createTempFile(mediaDir, "blah", ".csv")
        writeToFile(oldMediaFile, "blah before")

        val serverFormDetails = fetcher.fetchFormDetails()
        assertThat(getFormFromList(serverFormDetails, "form-2").isUpdated, `is`(true))
    }

    @Test
    fun whenAFormExists_andItsNewerVersionWithUpdatedMediaFilesHasBeenAlreadyDownloaded_isNotNewOrUpdated() {
        whenever(formSource.fetchFormList()).thenReturn(listOf(FORM_WITH_MANIFEST))

        val mediaDir1 = TempFiles.createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .version("1")
                .md5Hash("form-2_v1-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "1").absolutePath)
                .formMediaPath(mediaDir1.absolutePath)
                .build()
        )
        val mediaFile1 = TempFiles.createTempFile(mediaDir1, MEDIA_FILE.filename)
        writeToFile(mediaFile1, "old blah")

        val mediaDir2 = TempFiles.createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .version("2")
                .md5Hash("form-2-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").absolutePath)
                .formMediaPath(mediaDir2.absolutePath)
                .build()
        )
        val mediaFile2 = TempFiles.createTempFile(mediaDir2, MEDIA_FILE.filename)
        writeToFile(mediaFile2, FILE_CONTENT)

        val serverFormDetails = fetcher.fetchFormDetails()
        val form = getFormFromList(serverFormDetails, "form-2")
        assertThat(form.isUpdated, `is`(false))
        assertThat(form.isNotOnDevice, `is`(false))
    }

    @Test
    fun whenAFormExists_andItsNewerVersionHasBeenAlreadyDownloadedButThenSoftDeleted_isUpdated() {
        whenever(formSource.fetchFormList()).thenReturn(listOf(FORM_WITHOUT_MANIFEST))

        formsRepository.save(
            Form.Builder()
                .formId("form-1")
                .version("0")
                .md5Hash("form-1-hash0")
                .formFilePath(FormUtils.createXFormFile("form-1", "0").absolutePath)
                .build()
        )
        formsRepository.save(
            Form.Builder()
                .formId("form-1")
                .version("1")
                .md5Hash("form-1-hash")
                .formFilePath(FormUtils.createXFormFile("form-1", "1").absolutePath)
                .deleted(true)
                .build()
        )

        val serverFormDetails = fetcher.fetchFormDetails()
        val form = getFormFromList(serverFormDetails, "form-1")
        assertThat(form.isUpdated, `is`(true))
        assertThat(form.isNotOnDevice, `is`(false))
    }

    @Test
    fun whenAFormExists_andItsNewerVersionWithMediaFilesHasBeenAlreadyDownloadedButThenSoftDeleted_isUpdated() {
        whenever(formSource.fetchFormList()).thenReturn(listOf(FORM_WITH_MANIFEST))

        val mediaDir1 = TempFiles.createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .version("1")
                .md5Hash("form-2_v1-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "1").absolutePath)
                .formMediaPath(mediaDir1.absolutePath)
                .build()
        )
        val mediaFile1 = TempFiles.createTempFile(mediaDir1, MEDIA_FILE.filename)
        writeToFile(mediaFile1, FILE_CONTENT)

        val mediaDir2 = TempFiles.createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .version("2")
                .md5Hash("form-2-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").absolutePath)
                .formMediaPath(mediaDir2.absolutePath)
                .deleted(true)
                .build()
        )
        val mediaFile2 = TempFiles.createTempFile(mediaDir2, MEDIA_FILE.filename)
        writeToFile(mediaFile2, FILE_CONTENT)

        val serverFormDetails = fetcher.fetchFormDetails()
        val form = getFormFromList(serverFormDetails, "form-2")
        assertThat(form.isUpdated, `is`(true))
        assertThat(form.isNotOnDevice, `is`(false))
    }

    @Test
    fun whenAFormExists_andIsNotUpdatedOnServer_andDoesNotHaveAManifest_isNotNewOrUpdated() {
        whenever(formSource.fetchFormList()).thenReturn(listOf(FORM_WITHOUT_MANIFEST))

        formsRepository.save(
            Form.Builder()
                .formId("form-1")
                .version("1")
                .md5Hash("form-1-hash")
                .formFilePath(FormUtils.createXFormFile("form-1", "1").absolutePath)
                .build()
        )

        val serverFormDetails = fetcher.fetchFormDetails()
        val form = getFormFromList(serverFormDetails, "form-1")
        assertThat(form.isUpdated, `is`(false))
        assertThat(form.isNotOnDevice, `is`(false))
    }

    @Test
    fun whenFormExists_andMediaFilesExist_isNotNewOrUpdated() {
        whenever(formSource.fetchFormList()).thenReturn(listOf(FORM_WITH_MANIFEST))

        val mediaDir = TempFiles.createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .version("2")
                .md5Hash("form-2-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").absolutePath)
                .formMediaPath(mediaDir.absolutePath)
                .build()
        )
        val mediaFile = TempFiles.createTempFile(mediaDir, "blah", ".csv")
        writeToFile(mediaFile, FILE_CONTENT)

        val serverFormDetails = fetcher.fetchFormDetails()
        val form = getFormFromList(serverFormDetails, "form-2")
        assertThat(form.isNotOnDevice, `is`(false))
        assertThat(form.isUpdated, `is`(false))
    }

    @Test
    fun whenAFormExists_andIsUpdatedOnServer_andDoesNotHaveNewMedia_isUpdated() {
        whenever(formSource.fetchFormList()).thenReturn(listOf(FORM_WITH_MANIFEST))

        val mediaDir = TempFiles.createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .md5Hash("form-2-hash-old")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").absolutePath)
                .formMediaPath(mediaDir.absolutePath)
                .build()
        )
        val localMediaFile = TempFiles.createTempFile(mediaDir, "blah", ".csv")
        writeToFile(localMediaFile, FILE_CONTENT)

        val serverFormDetails = fetcher.fetchFormDetails()
        assertThat(getFormFromList(serverFormDetails, "form-2").isUpdated, `is`(true))
    }

    @Test
    fun whenAFormExists_andItsNewerVersionWithManifestIsAvailableButHasNullHash_isNotNewOrUpdated() {
        whenever(formSource.fetchFormList()).thenReturn(
            listOf(FORM_WITH_MANIFEST.copy(hash = null))
        )

        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .version("0")
                .md5Hash("form-2-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "1").absolutePath)
                .build()
        )

        val serverFormDetails = fetcher.fetchFormDetails()
        val form = getFormFromList(serverFormDetails, "form-2")
        assertThat(form.isUpdated, `is`(false))
        assertThat(form.isNotOnDevice, `is`(false))
    }

    private fun writeToFile(mediaFile: File, blah: String) {
        val bw = BufferedWriter(FileWriter(mediaFile))
        bw.write(blah)
        bw.close()
    }

    private fun getFormFromList(
        serverFormDetails: List<ServerFormDetails>,
        formId: String
    ): ServerFormDetails {
        return serverFormDetails.stream().filter { it.formId == formId }.findAny().get()
    }
}

private const val MANIFEST_URL = "http://example.com/form-3-manifest"
private const val FILE_CONTENT = "blah"
private val MEDIA_FILE = MediaFile(
    "blah.txt",
    getMd5Hash(
        ByteArrayInputStream(
            FILE_CONTENT.toByteArray()
        )
    )!!,
    "http://example.com/media-file"
)

private val FORM_WITHOUT_MANIFEST =
    FormListItem("http://example.com/form-1", "form-1", "1", "form-1-hash", "Form 1", null)

private val FORM_WITH_MANIFEST = FormListItem(
    "http://example.com/form-2",
    "form-2",
    "2",
    "form-2-hash",
    "Form 2",
    MANIFEST_URL
)
