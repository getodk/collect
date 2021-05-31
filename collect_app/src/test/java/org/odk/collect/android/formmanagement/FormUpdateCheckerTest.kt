package org.odk.collect.android.formmanagement

import android.content.ContentResolver
import android.content.Context
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.provider.FormsProviderAPI

class FormUpdateCheckerTest {

    @Test
    fun `checkForUpdates() notifies Forms content resolver`() {
        val contentResolver = mock<ContentResolver>()
        val context = mock<Context> {
            on { getContentResolver() } doReturn contentResolver
        }

        val service =
            FormUpdateChecker(context, mock(), mock(), mock(), mock(), mock(), mock(), mock())

        service.checkForUpdates()
        verify(contentResolver).notifyChange(FormsProviderAPI.CONTENT_URI, null)
    }
}
