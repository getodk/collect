package org.odk.collect.shared

import java.io.File

object PathUtils {

    @JvmStatic
    fun getRelativeFilePath(dirPath: String, filePath: String): String {
        return if (filePath.startsWith(dirPath)) filePath.substring(dirPath.length + 1) else filePath
    }

    @JvmStatic
    fun getAbsoluteFilePath(dirPath: String, filePath: String): String {
        return if (filePath.startsWith(dirPath)) filePath else dirPath + File.separator + filePath
    }
}
