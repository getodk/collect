package org.odk.collect.androidshared.system

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.shared.TempFiles
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream

@RunWith(AndroidJUnit4::class)
class UriExtTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `copyToFile creates the target file with the contents of the uri`() {
        val uri = Uri.parse("content://media/external/downloads/64711")
        val targetFile = File(TempFiles.createTempDir(), "target")
        val context = contextWhereOpeningUri(uri) { ByteArrayInputStream("blah".toByteArray()) }

        uri.copyToFile(context, targetFile)

        assertThat(targetFile.readText(), equalTo("blah"))
    }

    @Test
    fun `copyToFile creates no file when the uri cannot be opened`() {
        val uri = Uri.parse("content://media/external/downloads/64711")
        val targetFile = File(TempFiles.createTempDir(), "target")
        val context = contextWhereOpeningUri(uri) {
            throw IllegalStateException("Only owner is able to interact with pending media")
        }

        uri.copyToFile(context, targetFile)

        assertThat(targetFile.exists(), equalTo(false))
    }

    @Test
    fun `copyToFile creates no file when there is nothing to read`() {
        val uri = Uri.parse("content://media/external/downloads/64711")
        val targetFile = File(TempFiles.createTempDir(), "target")
        val context = contextWhereOpeningUri(uri) { null }

        uri.copyToFile(context, targetFile)

        assertThat(targetFile.exists(), equalTo(false))
    }

    @Test
    fun `copyToFile leaves no partial file when reading fails part way through`() {
        val uri = Uri.parse("content://media/external/downloads/64711")
        val targetFile = File(TempFiles.createTempDir(), "target")
        val context = contextWhereOpeningUri(uri) {
            object : ByteArrayInputStream("blah".toByteArray()) {
                override fun read(b: ByteArray, off: Int, len: Int): Int {
                    return if (available() > 0) {
                        super.read(b, off, len)
                    } else {
                        throw IOException("connection lost")
                    }
                }
            }
        }

        uri.copyToFile(context, targetFile)

        assertThat(targetFile.exists(), equalTo(false))
    }

    @Test
    fun `getFileExtension returns file extension`() {
        val file = TempFiles.createTempFile(".jpg")
        val fileUri = file.toUri()

        assertThat(fileUri.getFileExtension(context), equalTo(".jpg"))
    }

    @Test
    fun `getFileName returns file name`() {
        val file = TempFiles.createTempFile()
        val fileUri = file.toUri()

        assertThat(fileUri.getFileName(context), equalTo(file.name))
    }

    @Test
    fun `#addQueryParam adds single query param`() {
        val uri = Uri.parse("https://example.com")
        val result = uri.addQueryParam("id", "123")

        assertThat(result.toString(), equalTo("https://example.com?id=123"))
    }

    @Test
    fun `#addQueryParam preserves existing query parameters`() {
        val uri = Uri.parse("https://example.com?foo=bar")
        val result = uri.addQueryParam("id", "123")

        assertThat(result.toString(), equalTo("https://example.com?foo=bar&id=123"))
    }

    @Test
    fun `#addQueryParam allows null value`() {
        val uri = Uri.parse("https://example.com")
        val result = uri.addQueryParam("id", null)

        assertThat(result.toString(), equalTo("https://example.com?id=null"))
    }

    @Test
    fun `#addQueryParam does not change uri path`() {
        val uri = Uri.parse("https://example.com/path/subpath")
        val result = uri.addQueryParam("id", "123")

        assertThat(result.toString(), equalTo("https://example.com/path/subpath?id=123"))
    }

    private fun contextWhereOpeningUri(uri: Uri, open: () -> InputStream?): Context {
        val contentResolver = mock<ContentResolver> {
            on { openInputStream(uri) } doAnswer { open() }
        }

        return mock<Context> { on { getContentResolver() } doReturn contentResolver }
    }
}
