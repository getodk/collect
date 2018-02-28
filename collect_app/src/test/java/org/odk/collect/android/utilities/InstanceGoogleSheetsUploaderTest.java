package org.odk.collect.android.utilities;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.odk.collect.android.tasks.InstanceGoogleSheetsUploader;


public class InstanceGoogleSheetsUploaderTest {
    @Test
    public void googleSheetRegexTests() {
        assertFalse(InstanceGoogleSheetsUploader.isGoogleSheetsStringValid("()("));
        assertFalse(InstanceGoogleSheetsUploader.isGoogleSheetsStringValid("-@123"));
        assertFalse(InstanceGoogleSheetsUploader.isGoogleSheetsStringValid(";'[@%2789"));
        assertFalse(InstanceGoogleSheetsUploader.isGoogleSheetsStringValid("&googlesheets"));
        assertTrue(InstanceGoogleSheetsUploader.isGoogleSheetsStringValid("1234"));
        assertTrue(InstanceGoogleSheetsUploader.isGoogleSheetsStringValid("googlesheet"));
        assertTrue(InstanceGoogleSheetsUploader.isGoogleSheetsStringValid("Google"));
    }

    @Test
    public void gpsLocationRegexTests() {
        assertFalse(InstanceGoogleSheetsUploader.isLocationValid("{}{"));
        assertFalse(InstanceGoogleSheetsUploader.isLocationValid("28"));
        assertFalse(InstanceGoogleSheetsUploader.isLocationValid("-@123"));
        assertFalse(InstanceGoogleSheetsUploader.isLocationValid(";'[@123"));
        assertFalse(InstanceGoogleSheetsUploader.isLocationValid("*&1w345"));
        assertFalse(InstanceGoogleSheetsUploader.isLocationValid("41 24.2028, 2 10.4418"));
        assertFalse(InstanceGoogleSheetsUploader.isLocationValid("41.40338"));
        assertTrue(InstanceGoogleSheetsUploader.isLocationValid("-9.9 -9.9 -9.9 9.9"));
        assertTrue(InstanceGoogleSheetsUploader.isLocationValid("-0.0 0.8 -9.7 9.9"));
        assertTrue(InstanceGoogleSheetsUploader.isLocationValid("8.0 0.8 8.7 8.9"));
    }

}