package org.odk.collect.android.utilities;

import org.junit.Assert;
import org.junit.Test;

public class CSVUtilsTest {
    @Test
    public void testEscapeDoubleQuote() {
        Assert.assertNull(CSVUtils.escapeDoubleQuote(null));
        Assert.assertEquals("", CSVUtils.escapeDoubleQuote(""));
        Assert.assertEquals("no quotes", CSVUtils.escapeDoubleQuote("no quotes"));
        Assert.assertEquals("string with \"\"quotes\"\"", CSVUtils.escapeDoubleQuote("string with \"quotes\""));
    }

    @Test
    public void testQuoteString() {
        Assert.assertNull(CSVUtils.quoteString(null));
        Assert.assertEquals("\"\"", CSVUtils.quoteString(""));
        Assert.assertEquals("\"string\"", CSVUtils.quoteString("string"));
        Assert.assertEquals("\"string with \"quotes\"\"", CSVUtils.quoteString("string with \"quotes\""));
    }

    @Test
    public void testGetEscapedValueForCsv() {
        Assert.assertNull(CSVUtils.getEscapedValueForCsv(null));
        Assert.assertEquals("\"\"", CSVUtils.getEscapedValueForCsv(""));
        Assert.assertEquals("\"string\"", CSVUtils.getEscapedValueForCsv("string"));
        Assert.assertEquals("\"string with \"\"quotes\"\"\"", CSVUtils.getEscapedValueForCsv("string with \"quotes\""));
    }
}
