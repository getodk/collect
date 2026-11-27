package org.odk.collect.android.utilities

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.odk.collect.android.utilities.FormNameUtils.formatFilenameFromFormName
import org.odk.collect.android.utilities.FormNameUtils.normalizeFormName

class FormNameUtilsTest {
    @Test
    fun `#normalizeFormName replaces control characters with spaces`() {
        assertThat(normalizeFormName(null, false), nullValue())
        assertThat(normalizeFormName("Lorem", false), equalTo("Lorem"))
        assertThat(normalizeFormName("Lorem ipsum", false), equalTo("Lorem ipsum"))
        assertThat(normalizeFormName("Lorem\nipsum", false), equalTo("Lorem ipsum"))
        assertThat(normalizeFormName("Lorem\n\nipsum", false), equalTo("Lorem  ipsum"))
        assertThat(normalizeFormName("\nLorem\nipsum\n", false), equalTo(" Lorem ipsum "))

        assertThat(normalizeFormName(null, true), nullValue())
        assertThat(normalizeFormName("Lorem", true), nullValue())
        assertThat(normalizeFormName("Lorem ipsum", true), nullValue())
        assertThat(normalizeFormName("Lorem\nipsum", true), equalTo("Lorem ipsum"))
        assertThat(normalizeFormName("Lorem\n\nipsum", true), equalTo("Lorem  ipsum"))
        assertThat(normalizeFormName("\nLorem\nipsum\n", true), equalTo(" Lorem ipsum "))
    }

    @Test
    fun `#formatFilenameFromFormName keeps only letters, digits and single spaces`() {
        assertThat(formatFilenameFromFormName(null, 200), nullValue())
        assertThat(formatFilenameFromFormName("simple", 200), equalTo("simple"))
        assertThat(formatFilenameFromFormName("CamelCase", 200), equalTo("CamelCase"))
        assertThat(formatFilenameFromFormName("01234566789", 200), equalTo("01234566789"))

        assertThat(formatFilenameFromFormName(" trimWhitespace ", 200), equalTo("trimWhitespace"))
        assertThat(formatFilenameFromFormName("keep internal spaces", 200), equalTo("keep internal spaces"))
        assertThat(formatFilenameFromFormName("other\n\twhitespace", 200), equalTo("other whitespace"))
        assertThat(formatFilenameFromFormName("repeated         whitespace", 200), equalTo("repeated whitespace"))

        assertThat(formatFilenameFromFormName("Turkish İ kept", 200), equalTo("Turkish İ kept"))
        assertThat(formatFilenameFromFormName("registered symbol ® stripped", 200), equalTo("registered symbol stripped"))
        assertThat(formatFilenameFromFormName("unicode fragment \ud800 stripped", 200), equalTo("unicode fragment stripped"))
    }

    @Test
    fun `#formatFilenameFromFormName truncates to max file name bytes`() {
        // ASCII characters are 1 byte
        assertThat(formatFilenameFromFormName("a".repeat(200), 200), equalTo("a".repeat(200)))
        assertThat(formatFilenameFromFormName("a".repeat(201), 200), equalTo("a".repeat(200)))

        // Georgian characters are 3 bytes, so only 66 of them fit
        assertThat(formatFilenameFromFormName("ა".repeat(100), 200), equalTo("ა".repeat(66)))

        // Trailing space left after truncating is trimmed
        assertThat(formatFilenameFromFormName("a".repeat(199) + " b", 200), equalTo("a".repeat(199)))
    }
}
