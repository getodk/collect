package org.odk.collect.testshared

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.nio.file.Files

object TempFiles {

    @JvmStatic
    fun createTempFile(name: String, extension: String): File {
        val tempFile = File.createTempFile(name, extension)
        tempFile.deleteOnExit()
        return tempFile
    }

    @JvmStatic
    fun createTempFile(parent: File, name: String, extension: String): File {
        val tempFile = File.createTempFile(name, extension, parent)
        tempFile.deleteOnExit()
        return tempFile
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.O)
    fun createTempDir(): File {
        return Files.createTempDirectory(null as String?).toFile()!!
    }
}
