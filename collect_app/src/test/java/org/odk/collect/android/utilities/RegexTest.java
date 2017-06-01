package org.odk.collect.android.utilities;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.odk.collect.android.tasks.GoogleSheetsAbstractUploader;
import org.odk.collect.android.tasks.GoogleSheetsAbstractUploaderTest;


public class RegexTest {
     @Test
    public void  googleSheetRegexTests()
    {
        assertFalse(GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.VALID_GOOGLE_SHEETS_ID).matches("()("));
        assertTrue( GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.VALID_GOOGLE_SHEETS_ID).matches("googlesheet"));
        assertFalse( GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.VALID_GOOGLE_SHEETS_ID).matches("-@123"));
        assertFalse( GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.VALID_GOOGLE_SHEETS_ID).matches(";'[@%2789"));
    }
    @Test
    public void  gpsLocationRegexTests()
    {
        assertFalse(GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.GPS_LOCATION).matches("{}{"));
        assertFalse( GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.GPS_LOCATION).matches("28"));
        assertFalse( GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.GPS_LOCATION).matches("-@123"));
        assertFalse( GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.GPS_LOCATION).matches(";'[@123"));
    }

}
