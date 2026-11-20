package org.odk.collect.android.utilities

import java.util.regex.Pattern

object FormNameUtils {
    private val CONTROL_CHAR_PATTERN = Pattern.compile("\\p{Cntrl}")
    private val NON_ALPHANUMERIC_REGEX = "[^\\p{L}\\d]".toRegex()
    private val WHITESPACE_REGEX = "\\p{javaWhitespace}+".toRegex()

    @JvmStatic
    fun normalizeFormName(formName: String?, returnNullIfNothingChanged: Boolean): String? {
        if (formName == null) {
            return null
        }

        val matcher = CONTROL_CHAR_PATTERN.matcher(formName)
        return if (matcher.find()) {
            matcher.replaceAll(" ")
        } else if (returnNullIfNothingChanged) {
            null
        } else {
            formName
        }
    }

    // Create a sanitized filename prefix from the given form name. No extension is added.
    @JvmStatic
    fun formatFilenameFromFormName(formName: String?): String? {
        if (formName == null) {
            return null
        }
        // Keep alphanumerics, replace others with a space
        var fileName = formName.replace(NON_ALPHANUMERIC_REGEX, " ")
        // Replace consecutive whitespace characters with single space
        fileName = fileName.replace(WHITESPACE_REGEX, " ")
        return truncateToMaxFilenameBytes(fileName.trim()).trim()
    }

    // Truncate so the UTF-8 encoded text fits in maxFilenameBytes. File name limits are in bytes, not
    // characters, and many scripts use more than one byte per character.
    private fun truncateToMaxFilenameBytes(text: String): String {
        // File names are limited to 255 bytes, so leave room for the suffixes we add to form and instance files
        val maxFilenameBytes = 200
        var end = 0
        var bytes = 0
        while (end < text.length) {
            val codePoint = text.codePointAt(end)
            bytes += String(Character.toChars(codePoint)).toByteArray().size
            if (bytes > maxFilenameBytes) {
                break
            }
            end += Character.charCount(codePoint)
        }
        return text.substring(0, end)
    }
}
