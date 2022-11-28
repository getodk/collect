package org.odk.collect.android.configure.qr

import java.io.InputStream
import java.lang.Exception

interface QRCodeDecoder {
    @Throws(InvalidException::class, NotFoundException::class)
    fun decode(inputStream: InputStream?): String

    class InvalidException : Exception()
    class NotFoundException : Exception()
}
