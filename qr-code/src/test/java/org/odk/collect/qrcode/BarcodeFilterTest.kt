package org.odk.collect.qrcode

import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.isA
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BarcodeFilterTest {

    @Test
    fun `only returns barcode after threshold met`() {
        val barcodeFilter = BarcodeFilter(Rect(0, 0, 100, 100), 2)

        val candidate =
            BarcodeCandidate(byteArrayOf(0), "blah", Rect(50, 50, 50, 50))
        assertThat(barcodeFilter.filter(listOf(candidate)), equalTo(DetectedState.Potential))
        assertThat(barcodeFilter.filter(listOf(candidate)), isA(DetectedState.Full::class.java))
    }

    @Test
    fun `requires threshold to be met in sequence`() {
        val barcodeFilter = BarcodeFilter(Rect(0, 0, 100, 100), 2)

        val candidate =
            BarcodeCandidate(byteArrayOf(0), "blah", Rect(50, 50, 50, 50))
        assertThat(barcodeFilter.filter(listOf(candidate)), equalTo(DetectedState.Potential))

        val other =
            BarcodeCandidate(byteArrayOf(1), "other", Rect(50, 50, 50, 50))
        assertThat(barcodeFilter.filter(listOf(other)), equalTo(DetectedState.Potential))

        assertThat(barcodeFilter.filter(listOf(candidate)), equalTo(DetectedState.Potential))

        assertThat(
            barcodeFilter.filter(listOf(candidate)),
            equalTo(DetectedState.Full("blah"))
        )
    }

    @Test
    fun `requires threshold to be met in sequence when candidates have no bytes`() {
        val barcodeFilter = BarcodeFilter(Rect(0, 0, 100, 100), 2)

        val candidate =
            BarcodeCandidate(null, "blah", Rect(50, 50, 50, 50))
        assertThat(barcodeFilter.filter(listOf(candidate)), equalTo(DetectedState.Potential))

        val other =
            BarcodeCandidate(null, "other", Rect(50, 50, 50, 50))
        assertThat(barcodeFilter.filter(listOf(other)), equalTo(DetectedState.Potential))

        assertThat(barcodeFilter.filter(listOf(candidate)), equalTo(DetectedState.Potential))

        assertThat(
            barcodeFilter.filter(listOf(candidate)),
            equalTo(DetectedState.Full("blah"))
        )
    }

    @Test
    fun `an empty list of candidates clears sequence`() {
        val barcodeFilter = BarcodeFilter(Rect(0, 0, 100, 100), 2)

        val candidate =
            BarcodeCandidate(byteArrayOf(0), "blah", Rect(50, 50, 50, 50))
        assertThat(barcodeFilter.filter(listOf(candidate)), equalTo(DetectedState.Potential))

        assertThat(barcodeFilter.filter(emptyList()), equalTo(DetectedState.None))

        assertThat(barcodeFilter.filter(listOf(candidate)), equalTo(DetectedState.Potential))

        assertThat(
            barcodeFilter.filter(listOf(candidate)),
            equalTo(DetectedState.Full("blah"))
        )
    }

    @Test
    fun `returns contents of candidate`() {
        val barcodeFilter = BarcodeFilter(Rect(0, 0, 100, 100))
        val candidate =
            BarcodeCandidate(byteArrayOf(0), "blah", Rect(50, 50, 50, 50))
        assertThat(
            barcodeFilter.filter(listOf(candidate)),
            equalTo(DetectedState.Full("blah"))
        )
    }

    @Test
    fun `returns contents of candidate when it has no bytes`() {
        val barcodeFilter = BarcodeFilter(Rect(0, 0, 100, 100))
        val candidate = BarcodeCandidate(null, "blah", Rect(50, 50, 50, 50))
        assertThat(
            barcodeFilter.filter(listOf(candidate)),
            equalTo(DetectedState.Full("blah"))
        )
    }

    @Test
    fun `returns None when candidate has no contents`() {
        val barcodeFilter = BarcodeFilter(Rect(0, 0, 100, 100))
        val candidate = BarcodeCandidate(null, null, Rect(50, 50, 50, 50))
        assertThat(barcodeFilter.filter(listOf(candidate)), equalTo(DetectedState.None))
    }

    @Test
    fun `returns None when candidate has empty contents`() {
        val barcodeFilter = BarcodeFilter(Rect(0, 0, 100, 100))
        val candidate = BarcodeCandidate(byteArrayOf(), "", Rect(50, 50, 50, 50))
        assertThat(barcodeFilter.filter(listOf(candidate)), equalTo(DetectedState.None))
    }
}
