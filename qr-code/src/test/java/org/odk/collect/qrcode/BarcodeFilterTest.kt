package org.odk.collect.qrcode

import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BarcodeFilterTest {

    @Test
    fun `only returns barcode after threshold met`() {
        val barcodeFilter = BarcodeFilter(Rect(0, 0, 100, 100), 2)

        val candidate = BarcodeCandidate(byteArrayOf(0), "blah", Rect(50, 50, 50, 50), 0)
        assertThat(barcodeFilter.filter(listOf(candidate)), equalTo(null))
        assertThat(barcodeFilter.filter(listOf(candidate)), not(nullValue()))
    }

    @Test
    fun `requires threshold to be met in sequence`() {
        val barcodeFilter = BarcodeFilter(Rect(0, 0, 100, 100), 2)

        val candidate = BarcodeCandidate(byteArrayOf(0), "blah", Rect(50, 50, 50, 50), 0)
        assertThat(barcodeFilter.filter(listOf(candidate)), equalTo(null))

        val other = BarcodeCandidate(byteArrayOf(1), "blah", Rect(50, 50, 50, 50), 0)
        assertThat(barcodeFilter.filter(listOf(other)), equalTo(null))

        assertThat(barcodeFilter.filter(listOf(candidate)), equalTo(null))
        assertThat(
            barcodeFilter.filter(listOf(candidate))!!.bytes.contentEquals(candidate.bytes),
            equalTo(true)
        )
    }
}
