package org.odk.collect.android.utilities;

import org.junit.Assert;
import org.junit.Test;



public class RegexTest {
     @Test
    public void  junitTest()
    {
        Assert.assertTrue("Invalid Google Sheets",RegexMatcher.matchesRegex(Regex.VALID_GOOGLE_SHEETS_ID).matches("()("));
        Assert.assertTrue("Invalid Google Sheets",RegexMatcher.matchesRegex(Regex.VALID_GOOGLE_SHEETS_ID).matches("googlesheet"));
        Assert.assertTrue("Invalid Location",RegexMatcher.matchesRegex(Regex.GPS_LOCATION).matches("{}{"));
        Assert.assertTrue("Invalid Location",RegexMatcher.matchesRegex(Regex.GPS_LOCATION).matches("28"));
    }
}
