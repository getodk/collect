package org.odk.collect.android.instancemanagement.send

import org.odk.collect.openrosa.http.SubmissionUploadProgressTracker
import org.odk.collect.shared.strings.Md5.getMd5Hash
import timber.log.Timber
import java.io.File

/**
 * File-backed [SubmissionUploadProgressTracker] that lets an interrupted submission resume from the
 * last chunk the server accepted instead of restarting from the first one.
 *
 * Progress is stored in a hidden `.upload_progress` file inside the instance directory. It is hidden
 * so that [OpenRosaServerInstanceUploader]'s own directory listing (which skips dot-files) never
 * uploads it. The file records two things:
 *  1. a [computeUploadFingerprint] of the whole upload (submission XML + every attachment), and
 *  2. the index of the last chunk the server accepted.
 *
 * When [start] is called, if the stored fingerprint matches the current upload the tracker resumes
 * from `lastChunk + 1`; otherwise (the upload content changed, or there is no stored progress) it
 * starts from the first chunk and (re)writes the fingerprint.
 *
 * Resuming relies on the server retaining the previously uploaded chunks of an unfinished submission
 * — the OpenRosa `*isIncomplete*` contract keeps the submission open until a chunk without that flag
 * is received. If the server discards partial submissions, resuming is still safe because every
 * chunk re-sends the submission XML (identifying the submission) and the server merges attachments
 * by name; the worst case is that a discarded earlier chunk is missing, which the fingerprint check
 * cannot detect. Progress is therefore best-effort and only ever skips re-uploading data.
 */
class InstanceUploadProgressTracker(instanceDir: File) : SubmissionUploadProgressTracker {

    private val progressFile = File(instanceDir, PROGRESS_FILE_NAME)
    private var fingerprint: String = ""
    private var resumeFromChunkIndex: Int = 0

    /**
     * Loads any stored progress for [uploadFingerprint] and decides where the upload should resume.
     * Must be called before the upload starts.
     */
    fun start(uploadFingerprint: String) {
        fingerprint = uploadFingerprint

        val stored = read()
        resumeFromChunkIndex = if (stored != null && stored.fingerprint == uploadFingerprint) {
            // Same upload as last time: continue from the chunk after the last one that succeeded.
            stored.lastChunk + 1
        } else {
            // No progress, or the upload content changed since last time: start over.
            write(uploadFingerprint, NO_CHUNK_UPLOADED)
            0
        }
    }

    override fun getResumeFromChunkIndex(): Int = resumeFromChunkIndex

    override fun onChunkUploaded(chunkIndex: Int) {
        write(fingerprint, chunkIndex)
    }

    /** Removes stored progress. Call once the whole submission has been accepted. */
    fun clear() {
        progressFile.delete()
    }

    private fun read(): StoredProgress? {
        if (!progressFile.exists()) {
            return null
        }

        return try {
            val lines = progressFile.readLines()
            val storedFingerprint = lines.getOrNull(0)
            val lastChunk = lines.getOrNull(1)?.toIntOrNull()
            if (storedFingerprint.isNullOrEmpty() || lastChunk == null) {
                null
            } else {
                StoredProgress(storedFingerprint, lastChunk)
            }
        } catch (e: Exception) {
            Timber.w(e, "Could not read upload progress; the upload will restart from the first chunk")
            null
        }
    }

    private fun write(fingerprint: String, lastChunk: Int) {
        try {
            progressFile.writeText("$fingerprint\n$lastChunk")
        } catch (e: Exception) {
            // Progress tracking is best-effort: a failed write just means the next retry restarts.
            Timber.w(e, "Could not persist upload progress")
        }
    }

    private data class StoredProgress(val fingerprint: String, val lastChunk: Int)

    companion object {
        const val PROGRESS_FILE_NAME = ".upload_progress"
        private const val NO_CHUNK_UPLOADED = -1

        /**
         * Fingerprint of the whole upload: the MD5 of a manifest built from each file's name and its
         * own MD5 (the submission XML first, then the attachments in the exact order they will be
         * uploaded). Any change to the set, order or content of the uploaded files changes the
         * fingerprint, which invalidates resume progress and forces a restart from the first chunk.
         */
        @JvmStatic
        fun computeUploadFingerprint(submissionFile: File, orderedFiles: List<File>): String {
            val manifest = StringBuilder()
            manifest.append(submissionFile.name).append(':').append(submissionFile.getMd5Hash()).append('\n')
            for (file in orderedFiles) {
                manifest.append(file.name).append(':').append(file.getMd5Hash()).append('\n')
            }
            return manifest.toString().getMd5Hash() ?: ""
        }
    }
}
