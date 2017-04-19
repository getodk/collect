package org.odk.collect.android.utilities;


import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.odk.collect.android.tasks.GoogleSheetsAbstractUploader;
import org.odk.collect.android.tasks.GoogleSheetsAbstractUploaderTest;


public class RegexTest {
     @Test
    public void  junitTestGoogleSheets()
    {
        assertTrue("Invalid Google Sheets", GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.VALID_GOOGLE_SHEETS_ID).matches("()("));
        assertTrue("Invalid Google Sheets", GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.VALID_GOOGLE_SHEETS_ID).matches("googlesheet"));
        assertTrue("Invalid Google Sheets", GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.VALID_GOOGLE_SHEETS_ID).matches("-@123"));
        assertTrue("Invalid Google Sheets", GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.VALID_GOOGLE_SHEETS_ID).matches(";'[@%2789"));
    }
    @Test
    public void  junitTestLocation()
    {
        assertTrue("Invalid Location", GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.GPS_LOCATION).matches("{}{"));
        assertTrue("Invalid Location", GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.GPS_LOCATION).matches("28"));
        assertTrue("Invalid Location", GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.GPS_LOCATION).matches("-@123"));
        assertTrue("Invalid Location", GoogleSheetsAbstractUploaderTest.matchesRegex(GoogleSheetsAbstractUploader.GPS_LOCATION).matches(";'[@123"));
    }

}
