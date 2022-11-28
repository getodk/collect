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
import org.odk.collect.android.configure.qr.QRCodeDecoder.InvalidException
import org.odk.collect.android.utilities.CompressionUtils
import java.io.IOException
import java.io.InputStream
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE
import java.util.zip.DataFormatException
import kotlin.Any
import kotlin.Exception
import kotlin.IllegalArgumentException
import kotlin.IntArray
import kotlin.String
import kotlin.Throws

class QRCodeDecoderImpl : QRCodeDecoder {
    override fun decode(inputStream: InputStream?): String {
        val bitmap = BitmapFactory.decodeStream(inputStream)

        val tmpHintsMap: Map<DecodeHintType, Any> = mapOf(
            DecodeHintType.TRY_HARDER to TRUE,
            DecodeHintType.POSSIBLE_FORMATS to BarcodeFormat.QR_CODE,
            DecodeHintType.PURE_BARCODE to FALSE
        )

        return try {
            val decodedQrCode = QRCodeMultiReader().decode(getBinaryBitmap(bitmap), tmpHintsMap)
            val decompressedQrCode = CompressionUtils.decompress(decodedQrCode.text) ?: throw InvalidException()
            decompressedQrCode
        } catch (e: DataFormatException) {
            throw InvalidException()
        } catch (e: IOException) {
            throw InvalidException()
        } catch (e: IllegalArgumentException) {
            throw InvalidException()
        } catch (e: FormatException) {
            throw QRCodeDecoder.NotFoundException()
        } catch (e: NotFoundException) {
            throw QRCodeDecoder.NotFoundException()
        } catch (e: ChecksumException) {
            throw QRCodeDecoder.NotFoundException()
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
    @Throws(InvalidException::class, NotFoundException::class)
    fun decode(inputStream: InputStream?): String

    class InvalidException : Exception()
    class NotFoundException : Exception()
}
