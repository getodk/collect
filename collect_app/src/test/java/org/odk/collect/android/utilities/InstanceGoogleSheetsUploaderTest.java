package org.odk.collect.android.utilities;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.odk.collect.android.tasks.InstanceGoogleSheetsUploader;


public class InstanceGoogleSheetsUploaderTest {
    @Test
    public void googleSheetRegexTests() {
        assertFalse(InstanceGoogleSheetsUploader.isValidGoogleSheetsString("()("));
        assertFalse(InstanceGoogleSheetsUploader.isValidGoogleSheetsString("-@123"));
        assertFalse(InstanceGoogleSheetsUploader.isValidGoogleSheetsString(";'[@%2789"));
        assertFalse(InstanceGoogleSheetsUploader.isValidGoogleSheetsString("&googlesheets"));
        assertTrue(InstanceGoogleSheetsUploader.isValidGoogleSheetsString("1234"));
        assertTrue(InstanceGoogleSheetsUploader.isValidGoogleSheetsString("googlesheet"));
        assertTrue(InstanceGoogleSheetsUploader.isValidGoogleSheetsString("Google"));
    }

    @Test
    public void gpsLocationRegexTests() {
        assertFalse(InstanceGoogleSheetsUploader.isValidLocation("{}{"));
        assertFalse(InstanceGoogleSheetsUploader.isValidLocation("28"));
        assertFalse(InstanceGoogleSheetsUploader.isValidLocation("-@123"));
        assertFalse(InstanceGoogleSheetsUploader.isValidLocation(";'[@123"));
        assertFalse(InstanceGoogleSheetsUploader.isValidLocation("*&1w345"));
        assertFalse(InstanceGoogleSheetsUploader.isValidLocation("41 24.2028, 2 10.4418"));
        assertFalse(InstanceGoogleSheetsUploader.isValidLocation("41.40338"));
        assertTrue(InstanceGoogleSheetsUploader.isValidLocation("-9.9 -9.9 -9.9 9.9"));
        assertTrue(InstanceGoogleSheetsUploader.isValidLocation("-0.0 0.8 -9.7 9.9"));
        assertTrue(InstanceGoogleSheetsUploader.isValidLocation("8.0 0.8 8.7 8.9"));
    }

}