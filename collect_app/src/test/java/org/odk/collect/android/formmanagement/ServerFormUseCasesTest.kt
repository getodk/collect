package org.odk.collect.android.formmanagement

import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matchers
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.never
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.stubbing.Answer
import org.odk.collect.android.formmanagement.download.FormDownloadException
import org.odk.collect.android.formmanagement.download.FormDownloader
import org.odk.collect.forms.ManifestFile
import org.odk.collect.testshared.BooleanChangeLock

class ServerFormUseCasesTest {

    @Test
    fun `downloadUpdates does not download when change lock locked`() {
        val changeLock = BooleanChangeLock()
        val formDownloader = mock<FormDownloader>()

        changeLock.lock()

        val serverForm =
            ServerFormDetails("", "", "", "", "", false, true, ManifestFile("", emptyList()))

        ServerFormUseCases.downloadForms(
            listOf(serverForm),
            changeLock,
            formDownloader
        )

        verify(formDownloader, never()).downloadForm(
            any(),
            any(),
            any()
        )
    }

    @Test
    fun `downloadUpdates returns completed downloads when cancelled`() {
        val changeLock = BooleanChangeLock()
        val formDownloader = mock<FormDownloader>()

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

        val results = ServerFormUseCases.downloadForms(
            serverForms,
            changeLock,
            formDownloader
        )

        ViewMatchers.assertThat(results.size, Matchers.`is`(1))
        ViewMatchers.assertThat(results[serverForms[0]], Matchers.equalTo(null))
    }
}
