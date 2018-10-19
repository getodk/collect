package org.odk.collect.android.utilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.odk.collect.android.upload.InstanceGoogleSheetsUploader;

public class InstanceGoogleSheetsUploaderTaskTest {
    InstanceGoogleSheetsUploader uploader = new InstanceGoogleSheetsUploader(null);

    @Test
    public void gpsLocationRegexTests() {
        assertFalse(uploader.isLocationValid("{}{"));
        assertFalse(uploader.isLocationValid("28"));
        assertFalse(uploader.isLocationValid("-@123"));
        assertFalse(uploader.isLocationValid(";'[@123"));
        assertFalse(uploader.isLocationValid("*&1w345"));
        assertFalse(uploader.isLocationValid("41 24.2028, 2 10.4418"));
        assertFalse(uploader.isLocationValid("41.40338"));
        assertTrue(uploader.isLocationValid("-9.9 -9.9 -9.9 9.9"));
        assertTrue(uploader.isLocationValid("-0.0 0.8 -9.7 9.9"));
        assertTrue(uploader.isLocationValid("8.0 0.8 8.7 8.9"));
    }
}