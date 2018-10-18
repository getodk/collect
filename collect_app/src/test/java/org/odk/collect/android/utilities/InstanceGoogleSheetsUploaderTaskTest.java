package org.odk.collect.android.utilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.odk.collect.android.tasks.InstanceGoogleSheetsUploaderTask;

public class InstanceGoogleSheetsUploaderTaskTest {
    @Test
    public void gpsLocationRegexTests() {
        assertFalse(InstanceGoogleSheetsUploaderTask.isLocationValid("{}{"));
        assertFalse(InstanceGoogleSheetsUploaderTask.isLocationValid("28"));
        assertFalse(InstanceGoogleSheetsUploaderTask.isLocationValid("-@123"));
        assertFalse(InstanceGoogleSheetsUploaderTask.isLocationValid(";'[@123"));
        assertFalse(InstanceGoogleSheetsUploaderTask.isLocationValid("*&1w345"));
        assertFalse(InstanceGoogleSheetsUploaderTask.isLocationValid("41 24.2028, 2 10.4418"));
        assertFalse(InstanceGoogleSheetsUploaderTask.isLocationValid("41.40338"));
        assertTrue(InstanceGoogleSheetsUploaderTask.isLocationValid("-9.9 -9.9 -9.9 9.9"));
        assertTrue(InstanceGoogleSheetsUploaderTask.isLocationValid("-0.0 0.8 -9.7 9.9"));
        assertTrue(InstanceGoogleSheetsUploaderTask.isLocationValid("8.0 0.8 8.7 8.9"));
    }
}