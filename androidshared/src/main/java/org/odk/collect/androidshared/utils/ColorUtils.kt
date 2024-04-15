package org.odk.collect.androidshared.utils

import android.graphics.Color
import androidx.annotation.ColorInt

@ColorInt
fun String.toColorInt() = try {
    var sanitizedColor = if (this.startsWith("#")) {
        this
    } else {
        "#$this"
    }

    if (sanitizedColor.length == 4) {
        sanitizedColor = shorthandToLonghandHexColor(sanitizedColor)
    }

    Color.parseColor(sanitizedColor)
} catch (e: IllegalArgumentException) {
    null
}

private fun shorthandToLonghandHexColor(shorthandColor: String): String {
    var longHandColor = ""
    shorthandColor.substring(1).map {
        longHandColor += it.toString() + it.toString()
    }

    return "#$longHandColor"
}
