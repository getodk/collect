package org.odk.collect.android.utilities;

import org.junit.Assert;
import org.junit.Test;

public class UriUtilsTest {
    @Test
    public void testStripLeadingSeparators() {
        Assert.assertEquals("", UriUtils.stripLeadingUriSlashes(""));
        Assert.assertEquals("", UriUtils.stripLeadingUriSlashes("/"));
        Assert.assertEquals("", UriUtils.stripLeadingUriSlashes("///////"));

        Assert.assertEquals("file", UriUtils.stripLeadingUriSlashes("/file"));
        Assert.assertEquals("root/file", UriUtils.stripLeadingUriSlashes("/root/file"));
        Assert.assertEquals("root/folder/", UriUtils.stripLeadingUriSlashes("/root/folder/"));
    }
}
