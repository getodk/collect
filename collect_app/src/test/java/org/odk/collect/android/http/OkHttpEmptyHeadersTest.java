package org.odk.collect.android.http;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.odk.collect.android.http.openrosa.okhttp.OkHttpCaseInsensitiveHeaders;
import org.odk.collect.android.http.openrosa.okhttp.OkHttpEmptyHeaders;

import okhttp3.Headers;

public class OkHttpEmptyHeadersTest {
    private CaseInsensitiveHeaders headers = new OkHttpEmptyHeaders();

    @Test
    public void testGetHeaders() {
        Assert.assertTrue(headers.getHeaders().size() == 0);
    }

    @Test
    public void testContainsHeader() {
        Assert.assertFalse(headers.containsHeader(""));
    }

    @Test
    public void testGetAnyValue() {
        Assert.assertNull(headers.getAnyValue(""));
    }

    @Test
    public void testGetValues() {
        Assert.assertNull(headers.getValues(""));
    }
}

