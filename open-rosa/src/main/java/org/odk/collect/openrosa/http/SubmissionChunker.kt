package org.odk.collect.openrosa.http

import java.io.File

/**
 * Splits a submission (the submission XML plus its media attachments) into the same chunks that
 * `OpenRosaHttpInterface#uploadSubmissionAndFiles` posts to an OpenRosa server. A new chunk is
 * started whenever adding the next attachment would push the chunk over 100 attachments or over the
 * server's max content length. Because every chunk re-sends the submission XML, the XML's length is
 * counted against every chunk's byte budget.
 *
 * The partition is a *pure function* of the (ordered) file list and the files' sizes, so for a given
 * ordered file list it is fully deterministic and reproducible across upload attempts. That
 * determinism is what makes it safe to resume an interrupted upload from a specific chunk index: as
 * long as the file list is ordered deterministically (see the caller) and unchanged, the chunk at
 * index `i` contains exactly the same attachments on every attempt.
 */
class SubmissionChunker(
    private val submissionFileLength: Long,
    private val fileList: List<File>,
    private val contentLength: Long
) {

    /**
     * @return the ordered list of chunks. Always contains at least one chunk (a submission with no
     * attachments is a single XML-only chunk).
     */
    fun chunk(): List<Chunk> {
        val chunks = mutableListOf<Chunk>()

        var first = true
        var fileIndex = 0
        while (fileIndex < fileList.size || first) {
            val chunkStart = fileIndex
            first = false
            var byteCount = submissionFileLength
            var incomplete = false

            while (fileIndex < fileList.size) {
                byteCount += fileList[fileIndex].length()

                // we've added at least one attachment to the request...
                if (fileIndex + 1 < fileList.size) {
                    if (fileIndex - chunkStart + 1 > MAX_FILES_PER_CHUNK ||
                        byteCount + fileList[fileIndex + 1].length() > contentLength
                    ) {
                        // the next file would exceed the threshold, so this chunk is incomplete...
                        incomplete = true
                        fileIndex++ // advance over the last attachment added
                        break
                    }
                }
                fileIndex++
            }

            chunks.add(Chunk(chunkStart, fileIndex, incomplete))
        }

        return chunks
    }

    /**
     * A contiguous range of attachments `[startIndex, endIndex)` uploaded in a single POST.
     * [isIncomplete] is true when more chunks follow this one (the `*isIncomplete*` flag is sent).
     */
    class Chunk(
        val startIndex: Int,
        val endIndex: Int,
        val isIncomplete: Boolean
    )

    private companion object {
        const val MAX_FILES_PER_CHUNK = 100
    }
}
