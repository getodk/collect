package org.odk.collect.android.audio

import java.io.File
import java.io.IOException

class AMRAppender : AudioFileAppender {
    @Throws(IOException::class)
    override fun append(one: File, two: File) {
        val twoContents = two.readBytes()
        if (twoContents.size > AMR_HEADER_BYTES) {
            one.appendBytes(twoContents.copyOfRange(AMR_HEADER_BYTES, twoContents.size))
        }
    }

    companion object {
        private const val AMR_HEADER_BYTES: Int = 6
    }
}
