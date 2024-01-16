package org.odk.collect.qrcode

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.odk.collect.androidshared.utils.CompressionUtils

class QRCodeCreatorImpl : QRCodeCreator {
    @Throws(QRCodeCreator.MaximumCharactersLimitException::class)
    override fun createEncoded(data: String): Bitmap {
        val compressedData = CompressionUtils.compress(data)
        return create(compressedData)
    }

    @Throws(QRCodeCreator.MaximumCharactersLimitException::class)
    override fun create(data: String): Bitmap {
        // Maximum capacity for QR Codes is 4,296 characters (Alphanumeric)
        if (data.length > 4000) {
            throw QRCodeCreator.MaximumCharactersLimitException()
        }

        val hints: Map<EncodeHintType, ErrorCorrectionLevel> = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L)

        val bitMatrix = QRCodeWriter().encode(
            data,
            BarcodeFormat.QR_CODE,
            QR_CODE_SIDE_LENGTH,
            QR_CODE_SIDE_LENGTH,
            hints
        )

        val bmp = Bitmap.createBitmap(
            QR_CODE_SIDE_LENGTH,
            QR_CODE_SIDE_LENGTH,
            Bitmap.Config.RGB_565
        )

        for (x in 0 until QR_CODE_SIDE_LENGTH) {
            for (y in 0 until QR_CODE_SIDE_LENGTH) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bmp
    }

    private companion object {
        private const val QR_CODE_SIDE_LENGTH = 400 // in pixels
    }
}

interface QRCodeCreator {
    @Throws(MaximumCharactersLimitException::class)
    fun create(data: String): Bitmap

    @Throws(MaximumCharactersLimitException::class)
    fun createEncoded(data: String): Bitmap

    class MaximumCharactersLimitException : Exception()
}
