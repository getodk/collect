package org.odk.collect.maps

import android.graphics.Color
import org.odk.collect.shared.strings.StringUtils
import java.util.Locale

class MarkerIconDescription @JvmOverloads constructor(
    val iconDrawableId: Int,
    private val color: String? = null,
    private val symbol: String? = null
) {
    fun getColor(): Int? = try {
        color?.let {
            var sanitizedColor = if (color.startsWith("#")) {
                color
            } else {
                "#$color"
            }

            if (sanitizedColor.length == 4) {
                sanitizedColor = shorthandToLonghandHexColor(sanitizedColor)
            }

            Color.parseColor(sanitizedColor)
        }
    } catch (e: Throwable) {
        null
    }

    fun getSymbol(): String? = symbol?.let {
        if (it.isBlank()) {
            null
        } else {
            StringUtils.firstCharacterOrEmoji(it).uppercase(Locale.US)
        }
    }

    override fun hashCode(): Int {
        var result = iconDrawableId.hashCode()
        result = result * 31 + color.hashCode()
        result = result * 31 + symbol.hashCode()
        return result
    }

    private fun shorthandToLonghandHexColor(shorthandColor: String): String {
        var longHandColor = ""
        shorthandColor.substring(1).map {
            longHandColor += it.toString() + it.toString()
        }

        return "#$longHandColor"
    }
}
