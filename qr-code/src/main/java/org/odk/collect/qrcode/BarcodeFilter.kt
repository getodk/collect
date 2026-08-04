package org.odk.collect.qrcode

import android.graphics.Rect

class BarcodeFilter(private val bounds: Rect, private val threshold: Int = 1) {

    private var potentialContents: String? = null
    private var potentialOccurrences = 0

    fun filter(barcodeCandidates: List<BarcodeCandidate>): DetectedState {
        val candidate = barcodeCandidates.firstOrNull()
        return if (candidate?.boundingBox != null && bounds.contains(candidate.boundingBox)) {
            val contents = candidate.contents
            if (contents != potentialContents) {
                potentialContents = contents
                potentialOccurrences = 0
            }

            potentialOccurrences++
            if (potentialOccurrences == threshold) {
                if (contents.isEmpty()) {
                    DetectedState.None
                } else {
                    DetectedState.Full(contents)
                }
            } else {
                DetectedState.Potential
            }
        } else {
            potentialContents = null
            potentialOccurrences = 0
            DetectedState.None
        }
    }
}

class BarcodeCandidate(
    val bytes: ByteArray?,
    val utfContents: String?,
    val boundingBox: Rect?
) {

    /**
     * Barcodes that aren't valid UTF-8 have no [utfContents] from MLKit, so they need decoding as
     * Latin, which is the default encoding for barcodes that don't declare one. This provides
     * parity with the Zxing implementation.
     */
    val contents: String
        get() = utfContents?.takeUnless { it.isEmpty() }
            ?: bytes?.let { String(it, Charsets.ISO_8859_1) }
            ?: ""
}
