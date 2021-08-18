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
            string.first().toString()
        }
    }
}
