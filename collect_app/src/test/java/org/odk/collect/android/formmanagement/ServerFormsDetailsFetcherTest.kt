package org.odk.collect.android.formmanagement

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
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
import org.odk.collect.shared.TempFiles.createTempDir
import org.odk.collect.shared.TempFiles.createTempFile
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileWriter

class ServerFormsDetailsFetcherTest {

    private val formList = listOf(
        FormListItem("http://example.com/form-1", "form-1", "1", "md5:form-1-hash", "Form 1", null),
        FormListItem(
            "http://example.com/form-2",
            "form-2",
            "2",
            "md5:form-2-hash",
            "Form 2",
            "http://example.com/form-2-manifest"
        ),
        FormListItem(
            "http://example.com/form-3",
            "form-3",
            "1",
            null,
            "Form 1",
            "http://example.com/form-3-manifest"
        ),
        FormListItem("http://example.com/form-4", "form-4", "1", "form-4-hash", "Form 4", null)
    )

    private val formsRepository: FormsRepository = InMemFormsRepository()
    private val diskFormsSynchronizer = mock<DiskFormsSynchronizer>()
    private val formSource = mock<FormSource>()

    private val fetcher =
        ServerFormsDetailsFetcher(formsRepository, formSource, diskFormsSynchronizer)

    @Before
    fun setup() {
        whenever(formSource.fetchFormList()).thenReturn(formList)
        whenever(formSource.fetchManifest(formList[1].manifestURL)).thenReturn(
            ManifestFile(
                "manifest-2-hash",
                listOf(
                    MEDIA_FILE
                )
            )
        )
        whenever(formSource.fetchManifest(formList[2].manifestURL)).thenReturn(
            ManifestFile(
                "manifest-3-hash",
                listOf(
                    MEDIA_FILE
                )
            )
        )
    }

    @Test
    fun whenFormHasManifestUrl_returnsMediaFilesInDetails() {
        val serverFormDetails = fetcher.fetchFormDetails()
        assertThat(
            getForm(serverFormDetails, "form-1").manifest,
            nullValue()
        )
        assertThat(
            getForm(serverFormDetails, "form-2").manifest!!.mediaFiles,
            contains(
                MEDIA_FILE
            )
        )
    }

    @Test
    fun whenNoFormsExist_isNotOnDevice() {
        val serverFormDetails = fetcher.fetchFormDetails()
        assertThat(
            getForm(serverFormDetails, "form-1").isNotOnDevice,
            `is`(true)
        )
        assertThat(
            getForm(serverFormDetails, "form-2").isNotOnDevice,
            `is`(true)
        )
    }

    @Test
    fun whenAFormIsSoftDeleted_isNotOnDevice() {
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
        assertThat(
            getForm(serverFormDetails, "form-1").isNotOnDevice,
            `is`(true)
        )
        assertThat(
            getForm(serverFormDetails, "form-2").isNotOnDevice,
            `is`(true)
        )
    }

    @Test
    fun whenAFormExists_andListContainsUpdatedVersion_isUpdated() {
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .md5Hash("form-2-hash-old")
                .formFilePath(FormUtils.createXFormFile("form-2", null).absolutePath)
                .build()
        )
        val serverFormDetails = fetcher.fetchFormDetails()
        assertThat(
            getForm(serverFormDetails, "form-2").isUpdated,
            `is`(true)
        )
    }

    @Test
    fun whenAFormExists_andHasNewMediaFileOnServer_isUpdated() {
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
        assertThat(
            getForm(serverFormDetails, "form-2").isUpdated,
            `is`(true)
        )
    }

    @Test
    fun whenAFormExists_andHasUpdatedMediaFileOnServer_isUpdated() {
        val mediaDir = createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .version("2")
                .md5Hash("form-2-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").absolutePath)
                .formMediaPath(mediaDir.absolutePath)
                .build()
        )
        val oldMediaFile = createTempFile(mediaDir, "blah", ".csv")
        writeToFile(oldMediaFile, "blah before")
        val serverFormDetails = fetcher.fetchFormDetails()
        assertThat(
            getForm(serverFormDetails, "form-2").isUpdated,
            `is`(true)
        )
    }

    @Test
    fun whenAFormExists_andItsNewerVersionWithUpdatedMediaFilesHasBeenAlreadyDownloaded_isNotNewOrUpdated() {
        val mediaDir1 = createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .version("1")
                .md5Hash("form-2_v1-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "1").absolutePath)
                .formMediaPath(mediaDir1.absolutePath)
                .build()
        )
        val mediaFile1 = createTempFile(mediaDir1, MEDIA_FILE.filename)
        writeToFile(mediaFile1, "old blah")
        val mediaDir2 = createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .version("2")
                .md5Hash("form-2-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").absolutePath)
                .formMediaPath(mediaDir2.absolutePath)
                .build()
        )
        val mediaFile2 = createTempFile(mediaDir2, MEDIA_FILE.filename)
        writeToFile(mediaFile2, FILE_CONTENT)
        val serverFormDetails = fetcher.fetchFormDetails()
        val (_, _, _, _, _, isNotOnDevice, isUpdated) = getForm(serverFormDetails, "form-2")
        assertThat(isUpdated, `is`(false))
        assertThat(isNotOnDevice, `is`(false))
    }

    @Test
    fun whenAFormExists_andItsNewerVersionIsAvailableButHasHashWithoutPrefix_isNotNewOrUpdated() {
        formsRepository.save(
            Form.Builder()
                .formId("form-4")
                .version("0")
                .md5Hash("form-4-hash_v0")
                .formFilePath(FormUtils.createXFormFile("form-4", "1").absolutePath)
                .build()
        )
        val serverFormDetails = fetcher.fetchFormDetails()
        val (_, _, _, _, _, isNotOnDevice, isUpdated) = getForm(serverFormDetails, "form-4")
        assertThat(isUpdated, `is`(false))
        assertThat(isNotOnDevice, `is`(false))
    }

    @Test
    fun whenAFormExists_andItsNewerVersionIsAvailableButHasNullHash_isNotNewOrUpdated() {
        formsRepository.save(
            Form.Builder()
                .formId("form-3")
                .version("0")
                .md5Hash("form-3-hash")
                .formFilePath(FormUtils.createXFormFile("form-3", "1").absolutePath)
                .build()
        )
        val serverFormDetails = fetcher.fetchFormDetails()
        val (_, _, _, _, _, isNotOnDevice, isUpdated) = getForm(serverFormDetails, "form-3")
        assertThat(isUpdated, `is`(false))
        assertThat(isNotOnDevice, `is`(false))
    }

    @Test
    fun whenAFormExists_andItsNewerVersionHasBeenAlreadyDownloadedButThenSoftDeleted_isUpdated() {
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
        val (_, _, _, _, _, isNotOnDevice, isUpdated) = getForm(serverFormDetails, "form-1")
        assertThat(isUpdated, `is`(true))
        assertThat(isNotOnDevice, `is`(false))
    }

    @Test
    fun whenAFormExists_andItsNewerVersionWithMediaFilesHasBeenAlreadyDownloadedButThenSoftDeleted_isUpdated() {
        val mediaDir1 = createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .version("1")
                .md5Hash("form-2_v1-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "1").absolutePath)
                .formMediaPath(mediaDir1.absolutePath)
                .build()
        )
        val mediaFile1 = createTempFile(mediaDir1, MEDIA_FILE.filename)
        writeToFile(mediaFile1, FILE_CONTENT)
        val mediaDir2 = createTempDir()
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
        val mediaFile2 = createTempFile(mediaDir2, MEDIA_FILE.filename)
        writeToFile(mediaFile2, FILE_CONTENT)
        val serverFormDetails = fetcher.fetchFormDetails()
        val (_, _, _, _, _, isNotOnDevice, isUpdated) = getForm(serverFormDetails, "form-2")
        assertThat(isUpdated, `is`(true))
        assertThat(isNotOnDevice, `is`(false))
    }

    @Test
    fun whenAFormExists_andIsNotUpdatedOnServer_andDoesNotHaveAManifest_isNotNewOrUpdated() {
        formsRepository.save(
            Form.Builder()
                .formId("form-1")
                .version("1")
                .md5Hash("form-1-hash")
                .formFilePath(FormUtils.createXFormFile("form-1", "1").absolutePath)
                .build()
        )
        val serverFormDetails = fetcher.fetchFormDetails()
        val (_, _, _, _, _, isNotOnDevice, isUpdated) = getForm(serverFormDetails, "form-1")
        assertThat(isUpdated, `is`(false))
        assertThat(isNotOnDevice, `is`(false))
    }

    @Test
    fun whenFormExists_isNotNewOrUpdated() {
        val mediaDir = createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .version("2")
                .md5Hash("form-2-hash")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").absolutePath)
                .formMediaPath(mediaDir.absolutePath)
                .build()
        )
        val mediaFile = createTempFile(mediaDir, "blah", ".csv")
        writeToFile(mediaFile, FILE_CONTENT)
        val serverFormDetails = fetcher.fetchFormDetails()
        val (_, _, _, _, _, isNotOnDevice, isUpdated) = getForm(serverFormDetails, "form-2")
        assertThat(isUpdated, `is`(false))
        assertThat(isNotOnDevice, `is`(false))
    }

    @Test
    fun whenAFormExists_andIsUpdatedOnServer_andDoesNotHaveNewMedia_isUpdated() {
        val mediaDir = createTempDir()
        formsRepository.save(
            Form.Builder()
                .formId("form-2")
                .md5Hash("form-2-hash-old")
                .formFilePath(FormUtils.createXFormFile("form-2", "2").absolutePath)
                .formMediaPath(mediaDir.absolutePath)
                .build()
        )
        val localMediaFile = createTempFile(mediaDir, "blah", ".csv")
        writeToFile(localMediaFile, FILE_CONTENT)
        val serverFormDetails = fetcher.fetchFormDetails()
        assertThat(
            getForm(serverFormDetails, "form-2").isUpdated,
            `is`(true)
        )
    }

    private fun writeToFile(mediaFile: File, blah: String) {
        val bw = BufferedWriter(FileWriter(mediaFile))
        bw.write(blah)
        bw.close()
    }

    private fun getForm(serverFormDetails: List<ServerFormDetails>, s2: String): ServerFormDetails {
        return serverFormDetails.stream().filter { (_, _, formId) -> formId == s2 }.findAny().get()
    }

    companion object {
        private const val FILE_CONTENT = "blah"
        private val MEDIA_FILE = MediaFile(
            "blah.txt",
            "md5:" + getMd5Hash(
                ByteArrayInputStream(
                    FILE_CONTENT.toByteArray()
                )
            ),
            "http://example.com/media-file"
        )
    }
}
