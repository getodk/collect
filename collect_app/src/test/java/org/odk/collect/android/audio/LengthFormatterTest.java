package org.odk.collect.android.audio;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.android.audio.LengthFormatter.formatLength;

public class LengthFormatterTest {

    @Test
    public void formatsToMinutesAndSeconds() {
        assertThat(formatLength(1000), equalTo("00:01"));
        assertThat(formatLength(52000), equalTo("00:52"));
        assertThat(formatLength(64000), equalTo("01:04"));
    }

    @Test
    public void whenHours_formatsToHoursMinutesAndSeconds() {
        assertThat(formatLength((60 * 60 * 1000) + 64000), equalTo("01:01:04"));
    }

    @Test
    public void when100Hours_showsDurationInHoursMinutesAndSeconds() {
        assertThat(formatLength(100 * 60 * 60 * 1000), equalTo("100:00:00"));
    }
}
