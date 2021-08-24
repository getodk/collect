package org.odk.collect.shared.strings

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.odk.collect.shared.strings.StringUtils.ellipsizeBeginning
import org.odk.collect.shared.strings.StringUtils.firstCharacterOrEmoji
import org.odk.collect.shared.strings.StringUtils.isBlank
import org.odk.collect.shared.strings.StringUtils.trim

class StringUtilsTest {

    @Test
    fun firstCharacterOrEmoji_whenStringIsOneCharacter_returnsString() {
        assertThat(firstCharacterOrEmoji("N"), equalTo("N"))
    }

    @Test
    fun firstCharacterOrEmoji_whenStringIsMoreThanOneCharacter_returnsFirstCharacter() {
        assertThat(firstCharacterOrEmoji("Nosferatu"), equalTo("N"))
    }

    @Test
    fun firstCharacterOrEmoji_whenStringIsEmoji_returnString() {
        assertThat(firstCharacterOrEmoji(VAMPIRE_EMOJI), equalTo(VAMPIRE_EMOJI))
    }

    @Test
    fun firstCharacterOrEmoji_whenStringIsComplexEmoji_returnString() {
        assertThat(firstCharacterOrEmoji(MAN_VAMPIRE_EMOJI), equalTo(MAN_VAMPIRE_EMOJI))
    }

    @Test
    fun firstCharacterOrEmoji_whenStringIsMultipleComplexEmojis_returnsFirstEmoji() {
        assertThat(
            firstCharacterOrEmoji(MAN_VAMPIRE_EMOJI + VAMPIRE_EMOJI),
            equalTo(MAN_VAMPIRE_EMOJI)
        )
    }

    @Test
    fun firstCharacterOrEmoji_whenStringIsTextFollowedByEmoji_returnsEmoji() {
        assertThat(firstCharacterOrEmoji("Dracula$MAN_VAMPIRE_EMOJI"), equalTo("D"))
    }

    @Test
    fun firstCharacterOrEmoji_whenStringIsRepeatedEmojis_returnsEmoji() {
        assertThat(firstCharacterOrEmoji(VAMPIRE_EMOJI + VAMPIRE_EMOJI), equalTo(VAMPIRE_EMOJI))
    }

    @Test
    fun firstCharacterOrEmoji_whenStringIsEmpty_returnsEmptyString() {
        assertThat(firstCharacterOrEmoji(""), equalTo(""))
    }

    @Test
    fun ellipsizeBeginningTest() {
        // 50 chars
        assertEquals(
            "Lorem ipsum dolor sit amet, consectetur massa nunc",
            ellipsizeBeginning("Lorem ipsum dolor sit amet, consectetur massa nunc")
        )

        // 100 chars
        assertEquals(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer tempus, risus ac cursus turpis duis",
            ellipsizeBeginning("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer tempus, risus ac cursus turpis duis")
        )

        // 101 chars
        assertEquals(
            "...m ipsum dolor sit amet, consectetur adipiscing elit. Cras finibus, augue a imperdiet orci aliquam",
            ellipsizeBeginning("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras finibus, augue a imperdiet orci aliquam")
        )

        // 150 chars
        assertEquals(
            "...it. Donec cursus condimentum sagittis. Ut condimentum efficitur libero, vitae volutpat dui nullam",
            ellipsizeBeginning("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec cursus condimentum sagittis. Ut condimentum efficitur libero, vitae volutpat dui nullam")
        )
    }

    @Test
    fun whenStringIsJustWhitespace_returnsTrue() {
        assertTrue(isBlank(" "))
    }

    @Test
    fun whenStringContainsWhitespace_returnsFalse() {
        assertFalse(isBlank(" hello "))
    }

    @Test
    fun whenCharSequenceContainWhitespaces_shouldTrimReturnTrimmedCharSequence() {
        val result = trim("\n\t <p style=\"text-align:center\">Text</p> \t\n")
        assertThat(result, equalTo("<p style=\"text-align:center\">Text</p>"))
    }

    @Test
    fun whenCharSequenceContainOnlyWhitespaces_shouldTrimReturnOriginalCharSequence() {
        val result = trim("\n\t \t\n")
        assertThat(result, equalTo("\n\t \t\n"))
    }

    @Test
    fun whenCharSequenceIsNull_shouldTrimReturnNull() {
        val result = trim(null)
        assertThat(result, equalTo(null))
    }

    @Test
    fun whenCharSequenceIsEmpty_shouldTrimReturnEmptyCharSequence() {
        val result = trim("")
        assertThat(result, equalTo(""))
    }

    companion object {
        // Unicode for https://emojipedia.org/vampire/
        private const val VAMPIRE_EMOJI = "\ud83e\udddb"

        // Unicode for https://emojipedia.org/man-vampire/
        private const val MAN_VAMPIRE_EMOJI = "\ud83e\udddb\u200d\u2642\ufe0f"
    }
}
