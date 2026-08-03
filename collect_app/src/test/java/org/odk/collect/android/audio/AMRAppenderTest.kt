package org.odk.collect.android.audio

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import java.io.File

class AMRAppenderTest {

    private val appender = AMRAppender()

    @Test
    fun `appends second to first file without the header bytes from the second`() {
        val file1 = File.createTempFile("file1", ".amr").also {
            it.writeBytes(AMR_HEADER + byteArrayOf(1, 2))
        }

        val file2 = File.createTempFile("file2", ".amr").also {
            it.writeBytes(AMR_HEADER + byteArrayOf(3, 4))
        }

        appender.append(file1, file2)
        assertThat(file1.readBytes(), equalTo(AMR_HEADER + byteArrayOf(1, 2, 3 , 4)))
    }

    @Test
    fun `doesn't append empty files`() {
        val file1 = File.createTempFile("file1", ".amr").also {
            it.writeBytes(AMR_HEADER + byteArrayOf(1, 2))
        }

        val file2 = File.createTempFile("file2", ".amr")

        appender.append(file1, file2)
        assertThat(file1.readBytes(), equalTo(AMR_HEADER + byteArrayOf(1, 2)))
    }

    @Test
    fun `doesn't append files smaller than the AMR header`() {
        val file1 = File.createTempFile("file1", ".amr").also {
            it.writeBytes(AMR_HEADER + byteArrayOf(1, 2))
        }

        val file2 = File.createTempFile("file2", ".amr").also {
            it.writeBytes(byteArrayOf(3, 4))
        }

        appender.append(file1, file2)
        assertThat(file1.readBytes(), equalTo(AMR_HEADER + byteArrayOf(1, 2)))
    }

    @Test
    fun `doesn't append files that are just the AMR header`() {
        val file1 = File.createTempFile("file1", ".amr").also {
            it.writeBytes(AMR_HEADER + byteArrayOf(1, 2))
        }

        val file2 = File.createTempFile("file2", ".amr").also {
            it.writeBytes(AMR_HEADER)
        }

        appender.append(file1, file2)
        assertThat(file1.readBytes(), equalTo(AMR_HEADER + byteArrayOf(1, 2)))
    }

    companion object {
        private val AMR_HEADER = byteArrayOf(0, 0, 0, 0, 0, 0)
    }
}