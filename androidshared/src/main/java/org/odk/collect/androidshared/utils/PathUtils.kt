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
            if (absoluteFile.sanitizedCanonicalPath().startsWith(File(dirPath).sanitizedCanonicalPath())) {
                absoluteFilePath
            } else {
                throw SecurityException("Contact support@getodk.org. Attempt to access file outside of Collect directory: $absoluteFilePath")
            }
        } catch (e: IOException) {
            Timber.e(
                "Failed attempt to access canonicalPath:\n" +
                    "dirPath: $dirPath\n" +
                    "filePath: $filePath\n" +
                    "absoluteFilePath: $absoluteFilePath\n" +
                    "absoluteFilePath exists: ${absoluteFile.exists()}\n"
            )
            absoluteFilePath
        }
    }
}
