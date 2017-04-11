package org.odk.collect.android.utilities;

import org.junit.Assert;
import org.junit.Test;


/**
 * Created by simran on 4/10/2017.
 */

public class RegexTest {
     @Test
    public void  junitTest()
    {
       Assert.assertTrue("INVALID GOOGLE SHEETS",RegexMatcher.matchesRegex(Regex.VALID_GOOGLE_SHEETS_ID).matches("()("));
        Assert.assertTrue("INVALID GOOGLE SHEETS",RegexMatcher.matchesRegex(Regex.VALID_GOOGLE_SHEETS_ID).matches("googlesheet"));
        Assert.assertTrue("INVALID LOCATION",RegexMatcher.matchesRegex(Regex.GPS_LOCATION).matches("{}{"));
        Assert.assertTrue("INVALID LOCATION",RegexMatcher.matchesRegex(Regex.GPS_LOCATION).matches("28"));
    }
}
