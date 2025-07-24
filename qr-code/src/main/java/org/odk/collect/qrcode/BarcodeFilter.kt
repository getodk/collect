package org.odk.collect.qrcode

import android.graphics.Rect

class BarcodeFilter(private val bounds: Rect) {

    fun filter(barcodeCandidates: List<BarcodeCandidate>): BarcodeCandidate? {
        val candidate = barcodeCandidates.firstOrNull()
        return if (candidate != null && candidate.boundingBox != null && bounds.contains(candidate.boundingBox)) {
            candidate
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
