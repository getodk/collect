package org.odk.collect.shared.files

import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

object FileExt {
    /**
     * Original File.getCanonicalPath() may return paths with inconsistent letter casing for the
     * /Android/data/ part of the path, such as /storage/emulated/0/android/data/... or /storage/emulated/0/Android/Data/...
     * instead of the expected /storage/emulated/0/Android/data/...
     * Since the Android file system is case-sensitive, this behavior appears to be a bug.
     *
     * For more details, see the discussion on Stack Overflow:
     * https://stackoverflow.com/questions/78965720/file-getcanonicalpath-returns-inconsistent-letter-casing-in-path
     */
    fun File.sanitizedCanonicalPath(): String {
        val androidDataSegment = "/Android/data/"
        val regex = Regex(androidDataSegment, RegexOption.IGNORE_CASE)

        return canonicalPath.replace(regex, androidDataSegment)
    }

    fun File.listFilesRecursively(): List<File> {
        val listFiles = listFiles() ?: emptyArray()
        return listFiles.flatMap {
            if (it.isDirectory) {
                it.listFilesRecursively()
            } else {
                listOf(it)
            }
        }
    }

    @JvmStatic
    fun File.deleteDirectory() {
        deleteRecursively()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun File.saveToFile(inputStream: InputStream) {
        if (exists() && !delete()) {
            throw IOException("Cannot overwrite $absolutePath. Perhaps the file is locked?")
        }

        inputStream.use { input ->
            outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
