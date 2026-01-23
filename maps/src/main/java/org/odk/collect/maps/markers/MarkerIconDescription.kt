package org.odk.collect.maps.markers

import org.odk.collect.androidshared.utils.sanitizeToColorInt
import org.odk.collect.shared.strings.StringUtils
import java.util.Locale

sealed interface MarkerIconDescription {
    class DrawableResource @JvmOverloads constructor(
        val drawable: Int,
        private val color: String? = null,
        private val symbol: String? = null
    ) : MarkerIconDescription {
        fun getColor(): Int? = color?.sanitizeToColorInt()

        fun getSymbol(): String? = symbol?.let {
            if (it.isBlank()) {
                null
            } else {
                StringUtils.firstCharacterOrEmoji(it).uppercase(Locale.US)
            }
        }
    }

    class LinePoint(val lineSize: Float, val color: Int) : MarkerIconDescription
}
