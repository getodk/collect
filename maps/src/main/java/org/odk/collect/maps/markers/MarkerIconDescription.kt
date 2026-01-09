package org.odk.collect.maps.markers

import org.odk.collect.androidshared.utils.toColorInt
import org.odk.collect.shared.strings.StringUtils
import java.util.Locale

sealed interface MarkerIconDescription {
    class Resource @JvmOverloads constructor(
        val icon: Int,
        private val color: String? = null,
        private val symbol: String? = null
    ) : MarkerIconDescription {
        fun getColor(): Int? = color?.toColorInt()

        fun getSymbol(): String? = symbol?.let {
            if (it.isBlank()) {
                null
            } else {
                StringUtils.firstCharacterOrEmoji(it).uppercase(Locale.US)
            }
        }
    }

    class LinePoint(val lineSize: Float) : MarkerIconDescription
}
