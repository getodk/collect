package org.odk.collect.androidshared.utils

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt

@ColorInt
fun String.sanitizeToColorInt() = try {
    var sanitizedColor = if (this.startsWith("#")) {
        this
    } else {
        "#$this"
    }

    if (sanitizedColor.length == 4) {
        sanitizedColor = shorthandToLonghandHexColor(sanitizedColor)
    }

    sanitizedColor.toColorInt()
} catch (e: IllegalArgumentException) {
    null
}

fun Int.opaque(): Int {
    return ColorUtils.setAlphaComponent(this, 100)
}

private fun shorthandToLonghandHexColor(shorthandColor: String): String {
    return shorthandColor.substring(1).fold("#") { accum, char ->
        "$accum$char$char"
    }
}
