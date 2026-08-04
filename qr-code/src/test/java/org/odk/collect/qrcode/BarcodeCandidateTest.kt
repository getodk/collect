package org.odk.collect.qrcode

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class BarcodeCandidateTest {

    @Test
    fun `contents are the UTF8 contents when they are not empty`() {
        val candidate = BarcodeCandidate("not the contents".toByteArray(), "Sección", null)

        assertThat(candidate.contents, equalTo("Sección"))
    }

    @Test
    fun `contents are decoded as Latin when there are no UTF8 contents`() {
        val bytes = "Sección".toByteArray(Charsets.ISO_8859_1)
        val candidate = BarcodeCandidate(bytes, null, null)

        assertThat(candidate.contents, equalTo("Sección"))
    }

    @Test
    fun `contents are decoded as Latin when UTF8 contents are empty`() {
        val bytes = "Sección".toByteArray(Charsets.ISO_8859_1)
        val candidate = BarcodeCandidate(bytes, "", null)

        assertThat(candidate.contents, equalTo("Sección"))
    }

    @Test
    fun `contents are decoded as Latin even when the bytes are in another encoding`() {
        val bytes = "Sección".toByteArray(Charsets.UTF_8)
        val candidate = BarcodeCandidate(bytes, null, null)

        assertThat(candidate.contents, equalTo("SecciÃ³n"))
    }

    @Test
    fun `contents are the UTF8 contents when there are no bytes`() {
        val candidate = BarcodeCandidate(null, "blah", null)

        assertThat(candidate.contents, equalTo("blah"))
    }

    @Test
    fun `contents are empty when both UTF8 contents and bytes are null or empty`() {
        var candidate = BarcodeCandidate(byteArrayOf(), "", null)
        assertThat(candidate.contents, equalTo(""))

        candidate = BarcodeCandidate(byteArrayOf(), null, null)
        assertThat(candidate.contents, equalTo(""))

        candidate = BarcodeCandidate(null, "", null)
        assertThat(candidate.contents, equalTo(""))

        candidate = BarcodeCandidate(null, null, null)
        assertThat(candidate.contents, equalTo(""))
    }
}
