package org.odk.collect.shared

object PathUtils {

    @JvmStatic
    fun getRelativeFilePath(dirPath: String, filePath: String): String {
        return if (filePath.startsWith(dirPath)) filePath.substring(dirPath.length + 1) else filePath
    }

    // https://stackoverflow.com/questions/2679699/what-characters-allowed-in-file-names-on-android
    @JvmStatic
    fun getPathSafeFileName(fileName: String) = fileName.replace("[\"*/:<>?\\\\|]".toRegex(), "_")
}
