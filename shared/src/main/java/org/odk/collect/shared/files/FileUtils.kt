package org.odk.collect.shared.files

import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

object FileUtils {
    @Throws(IOException::class)
    @JvmStatic
    fun saveToFile(inputStream: InputStream, filePath: String) {
        val file = File(filePath)
        if (file.exists() && !file.delete()) {
            throw IOException("Cannot overwrite $filePath. Perhaps the file is locked?")
        }

        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
