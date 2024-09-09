package org.odk.collect.shared.files

import java.io.File

object FileExt {
    /**
     * Original File.getCanonicalPath() may return paths with inconsistent letter casing for the
     * /Android/data/ part of the path, such as /storage/emulated/0/android/data/... or /storage/emulated/0/Android/Data/...
     * instead of the expected /storage/emulated/0/Android/data/...
     * Since the Android file system is case-sensitive, this behavior appears to be a bug.
     */
    fun File.sanitizedCanonicalPath(): String {
        val androidDataSegment = "/Android/data/"

        val canonicalPath = canonicalPath

        if (canonicalPath.contains(androidDataSegment, true)) {
            val regex = Regex(androidDataSegment, RegexOption.IGNORE_CASE)
            return canonicalPath.replace(regex, androidDataSegment)
        }

        return canonicalPath
    }
}
