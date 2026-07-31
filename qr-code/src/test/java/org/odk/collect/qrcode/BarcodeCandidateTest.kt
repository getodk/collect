package org.odk.collect.qrcode

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class BarcodeCandidateTest {

    @Test
    fun `contents are the UTF8 contents when MLKit decoded them`() {
        val candidate = BarcodeCandidate("Sección".toByteArray(), "Sección", null)

        assertThat(candidate.contents, equalTo("Sección"))
    }

    @Test
    fun `contents are decoded as Latin when there are no UTF8 contents`() {
        val bytes = byteArrayOf(0x53, 0x65, 0x63, 0x63, 0x69, 0xF3.toByte(), 0x6E)
        val candidate = BarcodeCandidate(bytes, null, null)

        assertThat(candidate.contents, equalTo("Sección"))
    }

    @Test
    fun `contents are decoded as Latin when UTF8 contents are empty`() {
        val bytes = byteArrayOf(0x53, 0x65, 0x63, 0x63, 0x69, 0xF3.toByte(), 0x6E)
        val candidate = BarcodeCandidate(bytes, "", null)

        assertThat(candidate.contents, equalTo("Sección"))
    }

    @Test
    fun `contents are the UTF8 contents when there are no bytes`() {
        val candidate = BarcodeCandidate(null, "blah", null)

        assertThat(candidate.contents, equalTo("blah"))
    }

    @Test
    fun `contents are empty when there are no bytes or UTF8 contents`() {
        val candidate = BarcodeCandidate(null, null, null)

        assertThat(candidate.contents, equalTo(""))
    }
}
