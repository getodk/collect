package org.odk.collect.android.utilities;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.odk.collect.android.utilities.CSVUtils.getEscapedValueForCsv;

public class CSVUtilsTest {
    @Test
    public void null_shouldBePassedThrough() {
        assertThat(getEscapedValueForCsv(null), is(nullValue()));
    }

    @Test
    public void stringsWithoutQuotesCommasOrNewlines_shouldBePassedThrough() {
        assertThat(getEscapedValueForCsv("a b c d e"), is("a b c d e"));
    }

    @Test
    public void quotes_shouldBeEscaped_andSurroundedByQuotes() {
        assertThat(getEscapedValueForCsv("a\"b\""), is("\"a\"\"b\"\"\""));
    }

    @Test
    public void commas_shouldBeSurroundedByQuotes() {
        assertThat(getEscapedValueForCsv("a,b"), is("\"a,b\""));
    }

    @Test
    public void newlines_shouldBeSurroundedByQuotes() {
        assertThat(getEscapedValueForCsv("a\nb"), is("\"a\nb\""));
    }
}
