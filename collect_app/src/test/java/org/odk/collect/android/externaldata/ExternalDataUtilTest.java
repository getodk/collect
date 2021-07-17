package org.odk.collect.android.externaldata;

import org.junit.Assert;
import org.junit.Test;

public class ExternalDataUtilTest {
    @Test
    public void testSafeColumnName() {
        // This is likely bad behavior: the method does not check the input for null or empty strings.
        Assert.assertEquals("c_", ExternalDataUtil.toSafeColumnName(""));

        // casing
        Assert.assertEquals("c_simplename", ExternalDataUtil.toSafeColumnName("simplename"));
        Assert.assertEquals("c_camelcase", ExternalDataUtil.toSafeColumnName("CamelCase"));

        // whitespace
        Assert.assertEquals("c_trailingwhitespace", ExternalDataUtil.toSafeColumnName("trailingwhitespace "));
        Assert.assertEquals("c_leadingwhitespace", ExternalDataUtil.toSafeColumnName(" leadingwhitespace"));
        Assert.assertEquals("c_middle_whitespace", ExternalDataUtil.toSafeColumnName("middle whitespace"));

        // numbers
        Assert.assertEquals("c_0123456789", ExternalDataUtil.toSafeColumnName("0123456789"));

        // specials
        Assert.assertEquals("c_a_b", ExternalDataUtil.toSafeColumnName("a*b"));
        Assert.assertEquals("c_new_line", ExternalDataUtil.toSafeColumnName("new\nline"));
        Assert.assertEquals("c_double_quote", ExternalDataUtil.toSafeColumnName("double\"quote"));
    }
}
