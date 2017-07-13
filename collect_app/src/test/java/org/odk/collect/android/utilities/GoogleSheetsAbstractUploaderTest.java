package org.odk.collect.android.utilities;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.odk.collect.android.tasks.GoogleSheetsAbstractUploader;


public class GoogleSheetsAbstractUploaderTest {
    @Test
    public void googleSheetRegexTests() {
        assertFalse(GoogleSheetsAbstractUploader.isValidGoogleSheetsString("()("));
        assertFalse(GoogleSheetsAbstractUploader.isValidGoogleSheetsString("-@123"));
        assertFalse(GoogleSheetsAbstractUploader.isValidGoogleSheetsString(";'[@%2789"));
        assertFalse(GoogleSheetsAbstractUploader.isValidGoogleSheetsString("&googlesheets"));
        assertTrue(GoogleSheetsAbstractUploader.isValidGoogleSheetsString("1234"));
        assertTrue(GoogleSheetsAbstractUploader.isValidGoogleSheetsString("googlesheet"));
        assertTrue(GoogleSheetsAbstractUploader.isValidGoogleSheetsString("Google"));
    }

    @Test
    public void gpsLocationRegexTests() {
        assertFalse(GoogleSheetsAbstractUploader.isValidLocation("{}{"));
        assertFalse(GoogleSheetsAbstractUploader.isValidLocation("28"));
        assertFalse(GoogleSheetsAbstractUploader.isValidLocation("-@123"));
        assertFalse(GoogleSheetsAbstractUploader.isValidLocation(";'[@123"));
        assertFalse(GoogleSheetsAbstractUploader.isValidLocation("*&1w345"));
        assertFalse(GoogleSheetsAbstractUploader.isValidLocation("41 24.2028, 2 10.4418"));
        assertFalse(GoogleSheetsAbstractUploader.isValidLocation("41.40338"));
        assertTrue(GoogleSheetsAbstractUploader.isValidLocation("-9.9 -9.9 -9.9 9.9"));
        assertTrue(GoogleSheetsAbstractUploader.isValidLocation("-0.0 0.8 -9.7 9.9"));
        assertTrue(GoogleSheetsAbstractUploader.isValidLocation("8.0 0.8 8.7 8.9"));
    }

}