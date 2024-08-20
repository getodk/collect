package org.odk.collect.shared

import java.io.File

object PathUtils {

    @JvmStatic
    fun getRelativeFilePath(dirPath: String, filePath: String): String {
        return if (filePath.startsWith(dirPath)) filePath.substring(dirPath.length + 1) else filePath
    }

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

    // https://stackoverflow.com/questions/2679699/what-characters-allowed-in-file-names-on-android
    @JvmStatic
    fun getPathSafeFileName(fileName: String) = fileName.replace("[\"*/:<>?\\\\|]".toRegex(), "_")
}
