package org.odk.collect.openrosa.http

/**
 * Tracks the progress of a chunked submission upload so that an interrupted upload can resume from
 * the last chunk the server accepted instead of restarting from the first one.
 *
 * Chunk indexes are only stable across attempts when the file list is ordered deterministically (see
 * [SubmissionChunker]) and its content is unchanged. The implementation is responsible for
 * invalidating stored progress — by returning `0` from [getResumeFromChunkIndex] — whenever the
 * upload's content changes, so that a changed upload restarts from the beginning.
 */
interface SubmissionUploadProgressTracker {

    /**
     * @return the index of the first chunk that still needs to be uploaded; `0` starts from the
     * beginning. A value greater than the number of chunks is clamped by the uploader to the final
     * chunk so that the finalizing request is always re-sent.
     */
    fun getResumeFromChunkIndex(): Int

    /**
     * Called after a chunk has been accepted by the server (HTTP 201/202).
     *
     * @param chunkIndex the zero-based index of the chunk that was just uploaded
     */
    fun onChunkUploaded(chunkIndex: Int)
}
