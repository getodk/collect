package org.odk.collect.android.http;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.odk.collect.android.http.openrosa.okhttp.OkHttpCaseInsensitiveHeaders;

import java.util.Set;
import java.util.TreeSet;

import okhttp3.Headers;

public class OkHttpCaseInsensitiveHeadersTest {
    private static CaseInsensitiveHeaders headers;

    @Before
    public void setup() {
        headers = buildTestHeaders();
    }

    @Test
    public void testMixedCaseHeaderLookup() {
        Assert.assertTrue(headers.containsHeader("Mixed-Case"));
        Assert.assertTrue(headers.containsHeader("mixed-case"));
        Assert.assertTrue(headers.containsHeader("MIXED-CASE"));
    }

    @Test
    public void testLowerCaseHeaderLookup() {
        Assert.assertTrue(headers.containsHeader("lower-case"));
        Assert.assertTrue(headers.containsHeader("Lower-Case"));
        Assert.assertTrue(headers.containsHeader("LOWER-CASE"));
    }

    @Test
    public void testUpperCaseHeaderLookup() {
        Assert.assertTrue(headers.containsHeader("UPPER-CASE"));
        Assert.assertTrue(headers.containsHeader("upper-case"));
        Assert.assertTrue(headers.containsHeader("Upper-Case"));
    }

    @Test
    public void testGetAnyForSingleValue() {
        Assert.assertTrue(headers.getValues("Mixed-Case").size() > 0);
        Assert.assertEquals(headers.getAnyValue("Mixed-Case"), "value");
    }

    @Test
    public void testGetAnyForMultipleValue() {
        Assert.assertTrue(headers.getValues("Collision").size() > 1);
        String anyValue = headers.getAnyValue("Collision");
        Set<String> values = new TreeSet<String>();
        values.add("v1");
        values.add("v2");
        values.add("v3");
        Assert.assertTrue(values.contains(anyValue));
    }

    @Test
    public void testCaseInsensitiveNameCollisions() {
        Assert.assertTrue(headers.containsHeader("Collision"));
        Assert.assertTrue(headers.getValues("Collision").size() > 1);
    }

    private static OkHttpCaseInsensitiveHeaders buildTestHeaders() {
        Headers.Builder headerBuilder = new Headers.Builder();

        headerBuilder.add("Mixed-Case", "value");
        headerBuilder.add("lower-case", "value");
        headerBuilder.add("UPPER-CASE", "value");
        headerBuilder.add("collision", "v1");
        headerBuilder.add("Collision", "v2");
        headerBuilder.add("COLLISION", "v3");

        return new OkHttpCaseInsensitiveHeaders(headerBuilder.build());
    }
}
