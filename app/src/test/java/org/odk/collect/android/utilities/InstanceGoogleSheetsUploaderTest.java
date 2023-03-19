package org.odk.collect.android.utilities;

import static org.javarosa.core.model.Constants.DATATYPE_BARCODE;
import static org.javarosa.core.model.Constants.DATATYPE_BINARY;
import static org.javarosa.core.model.Constants.DATATYPE_BOOLEAN;
import static org.javarosa.core.model.Constants.DATATYPE_CHOICE;
import static org.javarosa.core.model.Constants.DATATYPE_DATE;
import static org.javarosa.core.model.Constants.DATATYPE_DATE_TIME;
import static org.javarosa.core.model.Constants.DATATYPE_DECIMAL;
import static org.javarosa.core.model.Constants.DATATYPE_GEOPOINT;
import static org.javarosa.core.model.Constants.DATATYPE_GEOSHAPE;
import static org.javarosa.core.model.Constants.DATATYPE_GEOTRACE;
import static org.javarosa.core.model.Constants.DATATYPE_INTEGER;
import static org.javarosa.core.model.Constants.DATATYPE_LONG;
import static org.javarosa.core.model.Constants.DATATYPE_MULTIPLE_ITEMS;
import static org.javarosa.core.model.Constants.DATATYPE_TEXT;
import static org.javarosa.core.model.Constants.DATATYPE_TIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeElement;
import org.junit.Test;
import org.odk.collect.android.gdrive.InstanceGoogleSheetsUploader;

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
    public void getFormattingResistantAnswerTests() {
        String originalAnswer = "Test answer";
        String formattingResistantAnswer = "'" + originalAnswer;

        TreeElement treeElement = new TreeElement();
        treeElement.setAnswer(new StringData(originalAnswer));

        treeElement.setDataType(DATATYPE_TEXT);
        assertEquals(formattingResistantAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));
        treeElement.setDataType(DATATYPE_MULTIPLE_ITEMS);
        assertEquals(formattingResistantAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));
        treeElement.setDataType(DATATYPE_BARCODE);
        assertEquals(formattingResistantAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));

        treeElement.setDataType(DATATYPE_INTEGER);
        assertEquals(originalAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));
        treeElement.setDataType(DATATYPE_DECIMAL);
        assertEquals(originalAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));
        treeElement.setDataType(DATATYPE_DATE);
        assertEquals(originalAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));
        treeElement.setDataType(DATATYPE_TIME);
        assertEquals(originalAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));
        treeElement.setDataType(DATATYPE_DATE_TIME);
        assertEquals(originalAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));
        treeElement.setDataType(DATATYPE_CHOICE);
        assertEquals(originalAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));
        treeElement.setDataType(DATATYPE_BOOLEAN);
        assertEquals(originalAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));
        treeElement.setDataType(DATATYPE_GEOPOINT);
        assertEquals(originalAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));
        treeElement.setDataType(DATATYPE_BINARY);
        assertEquals(originalAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));
        treeElement.setDataType(DATATYPE_LONG);
        assertEquals(originalAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));
        treeElement.setDataType(DATATYPE_GEOSHAPE);
        assertEquals(originalAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));
        treeElement.setDataType(DATATYPE_GEOTRACE);
        assertEquals(originalAnswer, InstanceGoogleSheetsUploader.getFormattingResistantAnswer(treeElement));

    }
}