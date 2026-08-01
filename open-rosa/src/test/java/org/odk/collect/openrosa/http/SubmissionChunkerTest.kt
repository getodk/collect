package org.odk.collect.openrosa.http

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SubmissionChunkerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `chunks with no attachments into a single complete chunk`() {
        val chunks = SubmissionChunker(10, emptyList(), 100).chunk()

        assertThat(chunks, hasSize(1))
        assertThat(describe(chunks), equalTo(listOf("0-0-complete")))
    }

    @Test
    fun `chunks everything into a single complete chunk when it all fits in one request`() {
        val files = listOf(file(10), file(10), file(10))

        val chunks = SubmissionChunker(10, files, 10_000).chunk()

        assertThat(describe(chunks), equalTo(listOf("0-3-complete")))
    }

    @Test
    fun `splits when the content length would be exceeded and marks all but the last chunk incomplete`() {
        // xml = 0 bytes; three 100-byte files; budget 250: files 0+1 fit (200), file 2 would push to
        // 300 > 250 so it starts a second chunk.
        val files = listOf(file(100), file(100), file(100))

        val chunks = SubmissionChunker(0, files, 250).chunk()

        assertThat(describe(chunks), equalTo(listOf("0-2-incomplete", "2-3-complete")))
    }

    @Test
    fun `counts the submission xml length toward every chunk's budget`() {
        // The two 100-byte files (200) would fit in a 250 budget on their own, but the 100-byte xml
        // is re-sent in every chunk, so only one file fits per chunk.
        val files = listOf(file(100), file(100))

        val chunks = SubmissionChunker(100, files, 250).chunk()

        assertThat(describe(chunks), equalTo(listOf("0-1-incomplete", "1-2-complete")))
    }

    @Test
    fun `produces the same chunks for the same input`() {
        val files = listOf(file(100), file(90), file(100), file(70), file(100))

        val first = describe(SubmissionChunker(50, files, 250).chunk())
        val second = describe(SubmissionChunker(50, files, 250).chunk())

        assertThat(second, equalTo(first))
    }

    private fun file(length: Int): File {
        return tempFolder.newFile().apply { writeBytes(ByteArray(length)) }
    }

    private fun describe(chunks: List<SubmissionChunker.Chunk>): List<String> {
        return chunks.map {
            "${it.startIndex}-${it.endIndex}-${if (it.isIncomplete) "incomplete" else "complete"}"
        }
    }
}
