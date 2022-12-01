package org.odk.collect.qrcode

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class QRCodeEncodeDecodeTest {

    @Test
    fun `Encoded and decoded text should be unchanged`() {
        val data = "Some random text"

        val qrCodeEncoder = QRCodeEncoderImpl()
        val qrCodeDecoder = QRCodeDecoderImpl()

        val encodedData = qrCodeEncoder.encode(data)

        val decodedData = qrCodeDecoder.decode(toStream(encodedData))

        assertThat(decodedData, equalTo(data))
    }

    @Test(expected = QRCodeEncoder.MaximumCharactersLimitException::class)
    fun `When there are more than 4k characters passed to encode, throw an exception`() {
        val data = List(5000) { ('a'..'z').random() }.joinToString("")

        QRCodeEncoderImpl().encode(data)
    }

    private fun toStream(bitmap: Bitmap): ByteArrayInputStream {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
        val bitmapData = bos.toByteArray()
        return ByteArrayInputStream(bitmapData)
    }
}
