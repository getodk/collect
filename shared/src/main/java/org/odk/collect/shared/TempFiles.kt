package org.odk.collect.shared

import org.odk.collect.shared.strings.RandomString
import java.io.File

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
    fun getPathInTempDir(name: String, extension: String): String {
        val tmpDir = System.getProperty("java.io.tmpdir", ".")
        val file = File(tmpDir, name + extension)
        file.deleteOnExit()
        return file.absolutePath
    }

    @JvmStatic
    fun getPathInTempDir(): String {
        val tmpDir = System.getProperty("java.io.tmpdir", ".")
        return File(tmpDir, RandomString.randomString(16)).absolutePath
    }

    @JvmStatic
    fun createTempDir(): File {
        val dir = File(getPathInTempDir())
        dir.mkdir()
        return dir
    }
}
