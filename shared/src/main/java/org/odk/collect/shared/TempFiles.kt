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
    fun createTempFile(parent: File, name: String): File {
        return File(parent, name).also {
            it.createNewFile()
            it.deleteOnExit()
        }
    }

    @JvmStatic
    fun createTempFile(parent: File, name: String, extension: String): File {
        return File(parent, name + extension).also {
            it.createNewFile()
            it.deleteOnExit()
        }
    }

    @JvmStatic
    fun createTempFile(parent: File): File {
        val tempFile = File.createTempFile(getRandomString(), ".temp", parent)
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
        return File(tmpDir, getRandomString()).absolutePath
    }

    @JvmStatic
    @JvmOverloads
    fun createTempDir(parent: File? = null): File {
        val dir = if (parent != null) {
            File(parent, getRandomString())
        } else {
            File(getPathInTempDir())
        }

        dir.mkdir()
        return dir
    }

    private fun getRandomString() = RandomString.randomString(16)
}
