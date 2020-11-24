package org.odk.collect.strings.format

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class LengthFormatterTest {

    @Test
    fun formatsToMinutesAndSeconds() {
        assertThat(formatLength(1000), equalTo("00:01"))
        assertThat(formatLength(52000), equalTo("00:52"))
        assertThat(formatLength(64000), equalTo("01:04"))
    }

    @Test
    fun whenHours_formatsToHoursMinutesAndSeconds() {
        assertThat(formatLength(60 * 60 * 1000 + 64000), equalTo("01:01:04"))
    }

    @Test
    fun when100Hours_showsDurationInHoursMinutesAndSeconds() {
        assertThat(formatLength(100 * 60 * 60 * 1000), equalTo("100:00:00"))
    }
}
