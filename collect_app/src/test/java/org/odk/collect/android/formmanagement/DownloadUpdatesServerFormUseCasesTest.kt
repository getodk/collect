package org.odk.collect.android.formmanagement

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.doAnswer
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.mock
import org.mockito.stubbing.Answer
import org.odk.collect.android.formmanagement.download.FormDownloadException
import org.odk.collect.android.formmanagement.download.FormDownloader
import org.odk.collect.forms.ManifestFile

class DownloadUpdatesServerFormUseCasesTest {

    @Test
    fun `#downloadUpdates returns completed downloads when cancelled`() {
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
            formDownloader
        )

        assertThat(results.size, equalTo(1))
        assertThat(results[serverForms[0]], equalTo(null))
    }
}
