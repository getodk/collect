package org.odk.collect.android.http;

import org.junit.Assert;
import org.junit.Test;
import org.odk.collect.android.http.openrosa.okhttp.OkHttpEmptyHeaders;

public class OkHttpEmptyHeadersTest {
    private final CaseInsensitiveHeaders headers = new OkHttpEmptyHeaders();

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

