package org.odk.collect.android.utilities;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.odk.collect.android.tasks.GoogleSheetsAbstractUploader;


public class RegexTest {
    @Test
    public void googleSheetRegexTests() {
        assertFalse(GoogleSheetsAbstractUploader.isValidGoogleSheetsString("()("));
        assertTrue(GoogleSheetsAbstractUploader.isValidGoogleSheetsString("googlesheet"));
        assertFalse(GoogleSheetsAbstractUploader.isValidGoogleSheetsString("-@123"));
        assertFalse(GoogleSheetsAbstractUploader.isValidGoogleSheetsString(";'[@%2789"));
    }

    @Test
    public void gpsLocationRegexTests() {
        assertFalse(GoogleSheetsAbstractUploader.isValidLocation("{}{"));
        assertFalse(GoogleSheetsAbstractUploader.isValidLocation("28"));
        assertFalse(GoogleSheetsAbstractUploader.isValidLocation("-@123"));
        assertFalse(GoogleSheetsAbstractUploader.isValidLocation(";'[@123"));
    }

}