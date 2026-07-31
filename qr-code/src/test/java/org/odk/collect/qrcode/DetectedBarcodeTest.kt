package org.odk.collect.qrcode

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class DetectedBarcodeTest {

    @Test
    fun `Bytes contents are decoded as Latin`() {
        val bytes = byteArrayOf(0x53, 0x65, 0x63, 0x63, 0x69, 0xF3.toByte(), 0x6E)
        val barcode = DetectedBarcode.Bytes(bytes)

        assertThat(barcode.contents, equalTo("Sección"))
    }

    @Test
    fun `Bytes contents are empty when there are no bytes`() {
        val barcode = DetectedBarcode.Bytes(byteArrayOf())

        assertThat(barcode.contents, equalTo(""))
    }
}
