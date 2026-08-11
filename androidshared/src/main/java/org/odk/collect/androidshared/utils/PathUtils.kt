package org.odk.collect.androidshared.utils

import org.odk.collect.shared.files.FileExt.sanitizedCanonicalPath
import timber.log.Timber
import java.io.File
import java.io.IOException

object PathUtils {
    @JvmStatic
    fun getAbsoluteFilePath(dirPath: String, filePath: String): String {
        val absoluteFilePath =
            if (filePath.startsWith(dirPath)) filePath else dirPath + File.separator + filePath

        val absoluteFile = File(absoluteFilePath)
        return try {
            val canonicalFilePath = absoluteFile.sanitizedCanonicalPath()
            val canonicalDirPath = File(dirPath).sanitizedCanonicalPath()

            if (canonicalFilePath.startsWith(canonicalDirPath)) {
                absoluteFilePath
            } else {
                throw SecurityException(
                    "Contact support@getodk.org. Attempt to access file outside of Collect directory: $absoluteFilePath\n" +
                        "dirPath: $dirPath\n" +
                        "filePath: $filePath\n" +
                        "canonicalFilePath: $canonicalFilePath\n" +
                        "canonicalDirPath: $canonicalDirPath\n"
                )
            }
        } catch (_: IOException) {
            val message = "Failed attempt to access canonicalPath:\n" +
                "dirPath: $dirPath\n" +
                "filePath: $filePath\n" +
                "absoluteFilePath: $absoluteFilePath\n" +
                "absoluteFilePath exists: ${absoluteFile.exists()}\n"

            Timber.e(Error(message))
            absoluteFilePath
        }
    }
}
