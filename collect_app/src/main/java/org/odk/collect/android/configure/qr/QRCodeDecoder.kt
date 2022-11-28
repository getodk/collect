package org.odk.collect.android.configure.qr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.ChecksumException
import com.google.zxing.DecodeHintType
import com.google.zxing.FormatException
import com.google.zxing.LuminanceSource
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import org.odk.collect.android.configure.qr.QRCodeDecoder.QRCodeInvalidException
import org.odk.collect.android.configure.qr.QRCodeDecoder.QRCodeNotFoundException
import org.odk.collect.android.utilities.CompressionUtils
import java.io.IOException
import java.io.InputStream
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE
import java.util.zip.DataFormatException
import kotlin.Exception
import kotlin.IllegalArgumentException
import kotlin.IntArray
import kotlin.String
import kotlin.Throws

class QRCodeDecoderImpl : QRCodeDecoder {
    @Throws(QRCodeInvalidException::class, QRCodeNotFoundException::class)
    override fun decode(inputStream: InputStream?): String {
        return try {
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val decodedQrCode = QRCodeMultiReader()
                .decode(
                    getBinaryBitmap(bitmap),
                    mapOf(
                        DecodeHintType.TRY_HARDER to TRUE,
                        DecodeHintType.POSSIBLE_FORMATS to BarcodeFormat.QR_CODE,
                        DecodeHintType.PURE_BARCODE to FALSE
                    )
                )

            CompressionUtils.decompress(decodedQrCode.text) ?: throw QRCodeInvalidException()
        } catch (e: DataFormatException) {
            throw QRCodeInvalidException()
        } catch (e: IOException) {
            throw QRCodeInvalidException()
        } catch (e: IllegalArgumentException) {
            throw QRCodeInvalidException()
        } catch (e: FormatException) {
            throw QRCodeNotFoundException()
        } catch (e: NotFoundException) {
            throw QRCodeNotFoundException()
        } catch (e: ChecksumException) {
            throw QRCodeNotFoundException()
        } catch (e: Throwable) {
            throw QRCodeNotFoundException()
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
