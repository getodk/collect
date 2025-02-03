package org.odk.collect.androidshared.utils

import org.odk.collect.shared.files.FileExt.sanitizedCanonicalPath
import java.io.File

object PathUtils {
    @JvmStatic
    fun getAbsoluteFilePath(dirPath: String, filePath: String): String {
        val absoluteFilePath =
            if (filePath.startsWith(dirPath)) filePath else dirPath + File.separator + filePath

        if (File(absoluteFilePath).sanitizedCanonicalPath().startsWith(File(dirPath).sanitizedCanonicalPath())) {
            return absoluteFilePath
        } else {
            throw SecurityException("Contact support@getodk.org. Attempt to access file outside of Collect directory: $absoluteFilePath")
        }
    }
}
