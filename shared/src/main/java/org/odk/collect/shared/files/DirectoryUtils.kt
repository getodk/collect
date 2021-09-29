package org.odk.collect.shared.files

import java.io.File

object DirectoryUtils {

    @JvmStatic
    fun listFilesRecursively(directory: File): List<File> {
        val listFiles = directory.listFiles() ?: emptyArray()
        return listFiles.flatMap {
            if (it.isDirectory) {
                listFilesRecursively(it)
            } else {
                listOf(it)
            }
        }
    }
}
