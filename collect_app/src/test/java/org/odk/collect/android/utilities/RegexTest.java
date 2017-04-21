package org.odk.collect.android.utilities;

import org.junit.Assert;
import org.junit.Test;
import org.odk.collect.android.tasks.GoogleSheetsAbstractUploader;


public class RegexTest {
     @Test
    public void  junitTestGoogleSheets()
    {
        Assert.assertTrue("Invalid Google Sheets",RegexMatcher.matchesRegex(GoogleSheetsAbstractUploader.VALID_GOOGLE_SHEETS_ID).matches("()("));
        Assert.assertTrue("Invalid Google Sheets",RegexMatcher.matchesRegex(GoogleSheetsAbstractUploader.VALID_GOOGLE_SHEETS_ID).matches("googlesheet"));
        Assert.assertTrue("Invalid Google Sheets",RegexMatcher.matchesRegex(GoogleSheetsAbstractUploader.VALID_GOOGLE_SHEETS_ID).matches("-@123"));
        Assert.assertTrue("Invalid Google Sheets",RegexMatcher.matchesRegex(GoogleSheetsAbstractUploader.VALID_GOOGLE_SHEETS_ID).matches(";'[@%2789"));
    }
    @Test
    public void  junitTestLocation()
    {
        Assert.assertTrue("Invalid Location",RegexMatcher.matchesRegex(GoogleSheetsAbstractUploader.GPS_LOCATION).matches("{}{"));
        Assert.assertTrue("Invalid Location",RegexMatcher.matchesRegex(GoogleSheetsAbstractUploader.GPS_LOCATION).matches("28"));
        Assert.assertTrue("Invalid Location",RegexMatcher.matchesRegex(GoogleSheetsAbstractUploader.GPS_LOCATION).matches("-@123"));
        Assert.assertTrue("Invalid Location",RegexMatcher.matchesRegex(GoogleSheetsAbstractUploader.GPS_LOCATION).matches(";'[@123"));
    }

}
