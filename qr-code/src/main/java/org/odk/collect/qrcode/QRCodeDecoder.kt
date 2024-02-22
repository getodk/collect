package org.odk.collect.qrcode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import org.odk.collect.androidshared.utils.CompressionUtils
import java.io.IOException
import java.io.InputStream
import java.lang.Boolean.TRUE
import java.util.zip.DataFormatException
import kotlin.Exception
import kotlin.IllegalArgumentException
import kotlin.IntArray
import kotlin.String
import kotlin.Throws

class QRCodeDecoderImpl : QRCodeDecoder {
    @Throws(QRCodeDecoder.QRCodeInvalidException::class, QRCodeDecoder.QRCodeNotFoundException::class)
    override fun decode(inputStream: InputStream?): String {
        return try {
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val decodedQrCode = try {
                QRCodeMultiReader()
                    .decode(
                        getBinaryBitmap(bitmap),
                        mapOf(
                            DecodeHintType.TRY_HARDER to TRUE,
                            DecodeHintType.POSSIBLE_FORMATS to BarcodeFormat.QR_CODE
                        )
                    )
            } catch (e: NotFoundException) {
                QRCodeMultiReader()
                    .decode(
                        getBinaryBitmap(bitmap),
                        mapOf(
                            DecodeHintType.TRY_HARDER to TRUE,
                            DecodeHintType.POSSIBLE_FORMATS to BarcodeFormat.QR_CODE,
                            DecodeHintType.PURE_BARCODE to false
                        )
                    )
            }

            CompressionUtils.decompress(decodedQrCode.text)
        } catch (e: DataFormatException) {
            throw QRCodeDecoder.QRCodeInvalidException()
        } catch (e: IOException) {
            throw QRCodeDecoder.QRCodeInvalidException()
        } catch (e: IllegalArgumentException) {
            throw QRCodeDecoder.QRCodeInvalidException()
        } catch (e: Throwable) {
            throw QRCodeDecoder.QRCodeNotFoundException()
        }
    }

    private fun getBinaryBitmap(bitmap: Bitmap): BinaryBitmap {
        val intArray = IntArray(bitmap.width * bitmap.height)

        // copy pixel data from bitmap into the array
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source: LuminanceSource = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
        return BinaryBitmap(HybridBinarizer(source))
    }
}

interface QRCodeDecoder {
    @Throws(QRCodeInvalidException::class, QRCodeNotFoundException::class)
    fun decode(inputStream: InputStream?): String

    class QRCodeInvalidException : Exception()
    class QRCodeNotFoundException : Exception()
}
