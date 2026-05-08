package org.odk.collect.androidshared.system

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.shared.TempFiles

@RunWith(AndroidJUnit4::class)
class UriExtTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `copyToFile copies the source file to the target file`() {
        val sourceFile = TempFiles.createTempFile().also {
            it.writeText("blah")
        }
        val sourceFileUri = sourceFile.toUri()
        val targetFile = TempFiles.createTempFile()

        sourceFileUri.copyToFile(context, targetFile)
        assertThat(targetFile.readText(), equalTo(sourceFile.readText()))
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
}
