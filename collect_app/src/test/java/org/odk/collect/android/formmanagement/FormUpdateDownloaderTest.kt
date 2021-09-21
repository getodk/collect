package org.odk.collect.android.formmanagement

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.never
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.stubbing.Answer
import org.odk.collect.forms.ManifestFile
import org.odk.collect.testshared.BooleanChangeLock

class FormUpdateDownloaderTest {

    private val changeLock = BooleanChangeLock()
    private val formDownloader = mock<FormDownloader>()

    @Test
    fun `does not download when change lock locked`() {
        changeLock.lock()

        val serverForm =
            ServerFormDetails("", "", "", "", "", false, true, ManifestFile("", emptyList()))

        FormUpdateDownloader().downloadUpdates(
            listOf(serverForm),
            changeLock,
            formDownloader,
            "success",
            "failure"
        )

        verify(formDownloader, never()).downloadForm(any(), any(), any())
    }

    @Test
    fun `returns completed downloads when cancelled`() {
        val serverForms = listOf(
            ServerFormDetails("", "", "", "", "", false, true, ManifestFile("", emptyList())),
            ServerFormDetails("", "", "", "", "", false, true, ManifestFile("", emptyList()))
        )

        // Cancel form download after downloading one form
        doAnswer(object : Answer<Unit> {
            private var calledBefore = false

            @Throws(Throwable::class)
            override fun answer(invocation: InvocationOnMock) {
                calledBefore = if (!calledBefore) {
                    true
                } else {
                    throw FormDownloadException.DownloadingInterrupted()
                }
            }
        }).`when`(formDownloader).downloadForm(any(), any(), any())

        val results = FormUpdateDownloader().downloadUpdates(
            serverForms,
            changeLock,
            formDownloader,
            "success",
            "failure"
        )

        assertThat(results.size, `is`(1))
        assertThat(results[serverForms[0]], `is`("success"))
    }
}
