package org.odk.collect.qrcode

import android.graphics.Rect

class BarcodeFilter(private val bounds: Rect, private val threshold: Int) {

    private var potential: BarcodeCandidate? = null
    private var potentialOccurrences = 0

    fun filter(barcodeCandidates: List<BarcodeCandidate>): BarcodeCandidate? {
        val candidate = barcodeCandidates.firstOrNull()
        return if (candidate != null && candidate.boundingBox != null && bounds.contains(candidate.boundingBox)) {
            if (!candidate.bytes.contentEquals(potential?.bytes)) {
                potential = candidate
                potentialOccurrences = 0
            }

            potentialOccurrences++
            if (potentialOccurrences == threshold) {
                potential
            } else {
                null
            }
        } else {
            null
        }
    }
}

class BarcodeCandidate(
    val bytes: ByteArray?,
    val utfContents: String?,
    val boundingBox: Rect?,
    val format: Int
)
