package org.odk.collect.android.utilities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.BuildConfig
import org.odk.collect.androidshared.system.IntentLauncher
import org.robolectric.shadows.ShadowToast
import java.io.File

@RunWith(AndroidJUnit4::class)
class MediaUtilsTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private var intentLauncher = mock<IntentLauncher>()
    private var contentUriProvider = mock<ContentUriProvider>()
    private var mediaUtils = MediaUtils(intentLauncher, contentUriProvider)

    @Test
    fun `When file that we try to open does not exist a toast should be displayed`() {
        val file = File("file://image.png")

        mediaUtils.openFile(context, file, "image/*")

        assertThat(ShadowToast.getTextOfLatestToast(), `is`("File: file:/image.png is missing."))
        assertThat(ShadowToast.getLatestToast().duration, `is`(Toast.LENGTH_LONG))
        verify(intentLauncher, never()).launch(any(), any(), any())
    }

    @Test
    fun `When URI for file we try to open is null a toast should be displayed`() {
        val file = File.createTempFile("image", ".png")
        whenever(
            contentUriProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            )
        ).thenReturn(null)

        mediaUtils.openFile(context, file, "image/*")

        assertThat(ShadowToast.getTextOfLatestToast(), `is`("Can't open file. If you are on a Huawei device, this is expected and will not be fixed."))
        assertThat(ShadowToast.getLatestToast().duration, `is`(Toast.LENGTH_LONG))
        verify(intentLauncher, never()).launch(any(), any(), any())
    }

    @Test
    fun `When open file, launch on intentLauncher should be called with proper parameters`() {
        val file = File.createTempFile("image", ".png")
        whenever(
            contentUriProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            )
        ).thenReturn(
            Uri.parse("content://org.odk.collect.android.provider/external_files/Android/data/org.odk.collect.android/files/image.png")
        )

        val contextCaptor = argumentCaptor<Context>()
        val intentCaptor = argumentCaptor<Intent>()
        val runnableCaptor = argumentCaptor<() -> Unit>()

        mediaUtils.openFile(context, file, "image/*")

        verify(intentLauncher).launch(
            contextCaptor.capture(),
            intentCaptor.capture(),
            runnableCaptor.capture()
        )
        assertThat(contextCaptor.firstValue, `is`(context))
        assertThat(intentCaptor.firstValue.action, `is`(Intent.ACTION_VIEW))
        assertThat(
            intentCaptor.firstValue.data.toString(),
            `is`("content://org.odk.collect.android.provider/external_files/Android/data/org.odk.collect.android/files/image.png")
        )
        assertThat(intentCaptor.firstValue.type, `is`("image/*"))
    }
}
