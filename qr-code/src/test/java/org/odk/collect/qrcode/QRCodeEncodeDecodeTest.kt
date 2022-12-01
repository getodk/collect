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
    fun encodeDecodeTest() {
        val data = "Some random text"

        val qrCodeEncoder = QRCodeEncoderImpl()
        val qrCodeDecoder = QRCodeDecoderImpl()

        val encodedData = qrCodeEncoder.encode(data)

        val decodedData = qrCodeDecoder.decode(toStream(encodedData))

        assertThat(decodedData, equalTo(data))
    }

    @Test
    fun `When there are no more than 4k characters passed to encode, should not throw an exception`() {
        val data = "Q".repeat(4000)

        QRCodeEncoderImpl().encode(data)
    }

    @Test(expected = QRCodeEncoder.MaximumCharactersLimitException::class)
    fun `When there are more than 4k characters passed to encode, throw an exception`() {
        val data = "Q".repeat(4001)

        QRCodeEncoderImpl().encode(data)
    }

    private fun toStream(bitmap: Bitmap): ByteArrayInputStream {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
        val bitmapData = bos.toByteArray()
        return ByteArrayInputStream(bitmapData)
    }
}
