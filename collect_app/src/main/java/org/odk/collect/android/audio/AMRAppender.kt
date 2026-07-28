package org.odk.collect.android.audio

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class AMRAppender : AudioFileAppender {
    @Throws(IOException::class)
    override fun append(one: File, two: File) {
        val fos = FileOutputStream(one, true)
        val fis = FileInputStream(two)

        var fileContent = ByteArray(two.length().toInt())
        fis.read(fileContent)

        val headerlessFileContent = ByteArray(fileContent.size - AMR_HEADER_BYTES)
        if (fileContent.size - AMR_HEADER_BYTES >= 0) {
            System.arraycopy(
                fileContent,
                AMR_HEADER_BYTES,
                headerlessFileContent,
                0,
                fileContent.size - AMR_HEADER_BYTES
            )
        }

        fileContent = headerlessFileContent
        fos.write(fileContent)
    }

    companion object {
        const val AMR_HEADER_BYTES: Int = 6
    }
}
