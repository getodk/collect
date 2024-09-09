package org.odk.collect.shared.files

import java.io.File

object DirectoryUtils {

    @JvmStatic
    fun deleteDirectory(directory: File) {
        directory.deleteRecursively()
    }
}
