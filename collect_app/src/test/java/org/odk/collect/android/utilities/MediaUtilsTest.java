package org.odk.collect.android.utilities;

import org.junit.Assert;
import org.junit.Test;

public class MediaUtilsTest {
    @Test
    public void testEscapePath() {
        Assert.assertEquals("", MediaUtils.escapePathForLikeSQLClause(""));
        Assert.assertEquals("path/name", MediaUtils.escapePathForLikeSQLClause("path/name"));
        Assert.assertEquals("escaped underscore !_", MediaUtils.escapePathForLikeSQLClause("escaped underscore _"));
        Assert.assertEquals("escaped default wildcard !%", MediaUtils.escapePathForLikeSQLClause("escaped default wildcard %"));
        Assert.assertEquals("escaped bang!!", MediaUtils.escapePathForLikeSQLClause("escaped bang!"));

        Assert.assertEquals("repeated!_!_escape", MediaUtils.escapePathForLikeSQLClause("repeated__escape"));
    }
}

