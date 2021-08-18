package org.odk.collect.shared.strings

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class StringUtilsTest {

    @Test
    fun firstCharacterOrEmoji_whenStringIsOneCharacter_returnsString() {
        assertThat(StringUtils.firstCharacterOrEmoji("N"), equalTo("N"))
    }

    @Test
    fun firstCharacterOrEmoji_whenStringIsMoreThanOneCharacter_returnsFirstCharacter() {
        assertThat(StringUtils.firstCharacterOrEmoji("Nosferatu"), equalTo("N"))
    }

    @Test
    fun firstCharacterOrEmoji_whenStringIsEmoji_returnString() {
        assertThat(StringUtils.firstCharacterOrEmoji(VAMPIRE_EMOJI), equalTo(VAMPIRE_EMOJI))
    }

    @Test
    fun firstCharacterOrEmoji_whenStringIsComplexEmoji_returnString() {
        assertThat(StringUtils.firstCharacterOrEmoji(MAN_VAMPIRE_EMOJI), equalTo(MAN_VAMPIRE_EMOJI))
    }

    @Test
    fun firstCharacterOrEmoji_whenStringIsMultipleComplexEmojis_returnsFirstEmoji() {
        assertThat(StringUtils.firstCharacterOrEmoji(MAN_VAMPIRE_EMOJI + VAMPIRE_EMOJI), equalTo(MAN_VAMPIRE_EMOJI))
    }

    @Test
    fun firstCharacterOrEmoji_whenStringIsTextFollowedByEmoji_returnsEmoji() {
        assertThat(StringUtils.firstCharacterOrEmoji("Dracula$MAN_VAMPIRE_EMOJI"), equalTo("D"))
    }

    @Test
    fun firstCharacterOrEmoji_whenStringIsRepeatedEmojis_returnsEmoji() {
        assertThat(StringUtils.firstCharacterOrEmoji(VAMPIRE_EMOJI + VAMPIRE_EMOJI), equalTo(VAMPIRE_EMOJI))
    }

    companion object {
        // Unicode for https://emojipedia.org/vampire/
        private const val VAMPIRE_EMOJI = "\ud83e\udddb"

        // Unicode for https://emojipedia.org/man-vampire/
        private const val MAN_VAMPIRE_EMOJI = "\ud83e\udddb\u200d\u2642\ufe0f"
    }
}
