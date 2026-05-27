package org.odk.collect.qrcode

import android.graphics.Rect

class BarcodeFilter(private val bounds: Rect, private val threshold: Int = 1) {

    private var potential: BarcodeCandidate? = null
    private var potentialOccurrences = 0

    fun filter(barcodeCandidates: List<BarcodeCandidate>): DetectedState {
        val candidate = barcodeCandidates.firstOrNull()
        return if (candidate?.boundingBox != null && bounds.contains(candidate.boundingBox)) {
            if (!candidate.bytes.contentEquals(potential?.bytes)) {
                potential = candidate
                potentialOccurrences = 0
            }

            potentialOccurrences++
            if (potentialOccurrences == threshold) {
                if (candidate.bytes == null) {
                    DetectedState.None
                } else if (candidate.utfContents.isNullOrEmpty()) {
                    DetectedState.Full(DetectedBarcode.Bytes(candidate.format, candidate.bytes))
                } else {
                    DetectedState.Full(
                        DetectedBarcode.Utf8(
                            candidate.utfContents,
                            candidate.format,
                            candidate.bytes
                        )
                    )
                }
            } else {
                DetectedState.Potential
            }
        } else {
            potential = null
            potentialOccurrences = 0
            DetectedState.None
        }
    }
}

class BarcodeCandidate(
    val bytes: ByteArray?,
    val utfContents: String?,
    val boundingBox: Rect?,
    val format: BarcodeFormat
)

sealed class DetectedBarcode {

    abstract val bytes: ByteArray
    abstract val format: BarcodeFormat

    data class Utf8(
        val contents: String,
        override val format: BarcodeFormat,
        override val bytes: ByteArray
    ) : DetectedBarcode() {
        /**
         * Requires custom [equals] due to [ByteArray] field
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Utf8

            if (contents != other.contents) return false
            if (format != other.format) return false
            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = contents.hashCode()
            result = 31 * result + format.hashCode()
            result = 31 * result + bytes.contentHashCode()
            return result
        }
    }

    data class Bytes(
        override val format: BarcodeFormat,
        override val bytes: ByteArray
    ) : DetectedBarcode() {
        /**
         * Requires custom [equals] due to [ByteArray] field
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Bytes

            if (format != other.format) return false
            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = format.hashCode()
            result = 31 * result + bytes.contentHashCode()
            return result
        }
    }
}

enum class BarcodeFormat {
    PDF417,
    OTHER
}
