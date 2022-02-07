package org.odk.collect.shared.strings

import com.vdurmont.emoji.EmojiParser

object StringUtils {

    @JvmStatic
    fun firstCharacterOrEmoji(string: String): String {
        val onlyText = EmojiParser.removeAllEmojis(string)
        val firstCharacterIsNotEmoji = onlyText.isNotEmpty() && onlyText.first() == string.first()
        val onlyEmojis = EmojiParser.extractEmojis(string)

        return if (onlyEmojis.isNotEmpty() && !firstCharacterIsNotEmoji) {
            onlyEmojis[0]
        } else {
            if (string.isEmpty()) "" else string.first().toString()
        }
    }

    @JvmStatic
    fun ellipsizeBeginning(text: String): String {
        return if (text.length <= 100) text else "..." + text.substring(
            text.length - 97,
            text.length
        )
    }

    /**
     * Copyright (C) 2006 The Android Open Source Project
     *
     * Copied from Android project for testing.
     * TODO: replace with String.join when minSdk goes to 26
     *
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param delimiter a CharSequence that will be inserted between the tokens. If null, the string
     * "null" will be used as the delimiter.
     * @param tokens an array objects to be joined. Strings will be formed from the objects by
     * calling object.toString(). If tokens is null, a NullPointerException will be thrown. If
     * tokens is empty, an empty string will be returned.
     */
    @JvmStatic
    fun join(delimiter: CharSequence, tokens: Iterable<*>): String? {
        val it = tokens.iterator()
        if (!it.hasNext()) {
            return ""
        }
        val sb = StringBuilder()
        sb.append(it.next())
        while (it.hasNext()) {
            sb.append(delimiter)
            sb.append(it.next())
        }
        return sb.toString()
    }

    @JvmStatic
    fun isBlank(string: String): Boolean {
        val chars = string.toCharArray()
        for (character in chars) {
            if (!Character.isWhitespace(character)) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun trim(text: CharSequence?): CharSequence? {
        if (text == null || text.length == 0) {
            return text
        }
        val len = text.length
        var start = 0
        var end = len - 1
        while (start < len && Character.isWhitespace(text[start])) {
            start++
        }
        while (Character.isWhitespace(text[end]) && end > 0) {
            end--
        }
        return if (end >= start) {
            text.subSequence(start, end + 1)
        } else text
    }

    @JvmStatic
    fun removeEnd(str: String?, remove: String): String? {
        if (str == null || str.isEmpty()) {
            return str
        }
        return if (str.endsWith(remove)) {
            str.substring(0, str.length - remove.length)
        } else str
    }
}
