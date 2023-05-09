package org.odk.collect.shared

import org.odk.collect.shared.strings.RandomString
import java.io.File

object TempFiles {

    @JvmStatic
    fun createTempFile(name: String, extension: String): File {
        val tmpDir = getTempDir()
        return File(tmpDir, name + getRandomName(tmpDir) + extension).also {
            it.createNewFile()
            it.deleteOnExit()
        }
    }

    @JvmStatic
    fun createTempFile(parent: File, name: String): File {
        return File(parent, name + getRandomName(parent)).also {
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
        return File(parent, getRandomName(parent)).also {
            it.createNewFile()
            it.deleteOnExit()
        }
    }

    @JvmStatic
    fun getPathInTempDir(name: String, extension: String): String {
        val tmpDir = getTempDir()
        val file = File(tmpDir, name + extension)
        file.deleteOnExit()
        return file.absolutePath
    }

    @JvmStatic
    fun getPathInTempDir(): String {
        val tmpDir = getTempDir()
        return File(tmpDir, getRandomName(tmpDir)).absolutePath
    }

    @JvmStatic
    @JvmOverloads
    fun createTempDir(parent: File? = null): File {
        val dir = if (parent != null) {
            File(parent, getRandomName(parent))
        } else {
            File(getPathInTempDir())
        }

        dir.mkdir()
        return dir
    }

    private fun getTempDir(): File {
        val tmpDir = File(System.getProperty("java.io.tmpdir", "."), " org.odk.collect.shared.TempFiles")
        if (!tmpDir.exists()) {
            tmpDir.mkdir()
        }

        return tmpDir
    }

    private fun getRandomName(parent: File): String {
        val existing = parent.listFiles()

        var candiate = RandomString.randomString(16)
        while (existing!!.any { it.name.contains(candiate) }) {
            candiate = RandomString.randomString(16)
        }

        return candiate
    }
}
