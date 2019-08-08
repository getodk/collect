package org.odk.collect.android.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.odk.collect.android.upload.InstanceGoogleSheetsUploader;

import java.util.HashMap;

public class InstanceGoogleSheetsUploaderTest {
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

    @Test
    public void makeAnswersFormattingResistantTests() {
        HashMap<String, String> answers = new HashMap<>();
        answers.put("testForm-instanceId", "uuid:748c574c-4aa5-4f80-a628-b96d0f052e92");
        answers.put("testForm-text", "test string");
        answers.put("testForm-number", "55");
        answers.put("testForm-photo", "https://drive.google.com/open?id=1g7bWUq-raEAltOI2NX03jX4Ttt0AdAYA");
        answers.put("testForm-time", "12:57");
        answers.put("testForm-newGroup", "=HYPERLINK(\"https://docs.google.com/spreadsheets/d/1PObIXLRDBQjQOnRJLgKQJPOvRpl6cCPRDa99zWFLI60/edit#gid=938236033\", \"data-gr\")");
        answers.put("testForm-date", "08/08/19");
        answers.put("testForm-selectOne", "1");
        answers.put("testForm-selectMultiple", "1, 2, 3");
        answers.put("testForm-decimal", "7.5");
        answers.put("testForm-geopoint", "54.38072481,18.6065263");
        answers.put("testForm-geopoint-altitude", "128");
        answers.put("testForm-geopoint-accuracy", "24");

        HashMap<String, String> fixedAnswers = InstanceGoogleSheetsUploader.makeAnswersFormattingResistant(answers);
        assertEquals(answers.size(), fixedAnswers.size());
        assertEquals("'uuid:748c574c-4aa5-4f80-a628-b96d0f052e92", fixedAnswers.get("testForm-instanceId"));
        assertEquals("'test string", fixedAnswers.get("testForm-text"));
        assertEquals("'55", fixedAnswers.get("testForm-number"));
        assertEquals("https://drive.google.com/open?id=1g7bWUq-raEAltOI2NX03jX4Ttt0AdAYA", fixedAnswers.get("testForm-photo"));
        assertEquals("'12:57", fixedAnswers.get("testForm-time"));
        assertEquals("=HYPERLINK(\"https://docs.google.com/spreadsheets/d/1PObIXLRDBQjQOnRJLgKQJPOvRpl6cCPRDa99zWFLI60/edit#gid=938236033\", \"data-gr\")", fixedAnswers.get("testForm-newGroup"));
        assertEquals("'08/08/19", fixedAnswers.get("testForm-date"));
        assertEquals("'1", fixedAnswers.get("testForm-selectOne"));
        assertEquals("'1, 2, 3", fixedAnswers.get("testForm-selectMultiple"));
        assertEquals("'7.5", fixedAnswers.get("testForm-decimal"));
        assertEquals("'54.38072481,18.6065263", fixedAnswers.get("testForm-geopoint"));
        assertEquals("'128", fixedAnswers.get("testForm-geopoint-altitude"));
        assertEquals("'24", fixedAnswers.get("testForm-geopoint-accuracy"));
    }
}