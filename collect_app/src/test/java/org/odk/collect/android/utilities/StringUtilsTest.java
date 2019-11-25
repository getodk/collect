package org.odk.collect.android.utilities;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.utilities.StringUtils.isBlank;

public class StringUtilsTest {

    @Test
    public void whenStringIsJustWhitespace_returnsTrue() {
        assertTrue(isBlank(" "));
    }

    @Test
    public void whenStringContainsWhitespace_returnsFalse() {
        assertFalse(isBlank(" hello "));
    }
}