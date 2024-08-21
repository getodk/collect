package org.odk.collect.androidshared.utils

import java.io.File

object PathUtils {
    @JvmStatic
    fun getAbsoluteFilePath(dirPath: String, filePath: String): String {
        val absolutePath =
            if (filePath.startsWith(dirPath)) filePath else dirPath + File.separator + filePath

        if (File(absolutePath).canonicalPath.startsWith(File(dirPath).canonicalPath)) {
            return absolutePath
        } else {
            throw SecurityException("Contact support@getodk.org. Attempt to access file outside of Collect directory: $absolutePath")
        }
    }
}
