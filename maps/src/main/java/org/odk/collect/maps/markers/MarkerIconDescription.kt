package org.odk.collect.maps.markers

import org.odk.collect.androidshared.utils.sanitizeToColorInt
import org.odk.collect.shared.strings.StringUtils
import java.util.Locale

sealed interface MarkerIconDescription {
    data class DrawableResource @JvmOverloads constructor(
        val drawable: Int,
        private val color: Int? = null,
        private val symbol: String? = null
    ) : MarkerIconDescription {

        constructor(drawable: Int, color: String?, symbol: String?) : this(drawable, color?.sanitizeToColorInt(), symbol)

        fun getColor(): Int? = color

        fun getSymbol(): String? = symbol?.let {
            if (it.isBlank()) {
                null
            } else {
                StringUtils.firstCharacterOrEmoji(it).uppercase(Locale.US)
            }
        }
    }

    data class TracePoint(val lineSize: Float, val color: Int) : MarkerIconDescription
}
