package org.odk.collect.android.formentry.audit;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.TreeReference;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.BEGINNING_OF_FORM;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.CONSTRAINT_ERROR;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.DELETE_REPEAT;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.END_OF_FORM;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.FINALIZE_ERROR;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.FORM_EXIT;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.FORM_FINALIZE;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.FORM_RESUME;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.FORM_SAVE;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.FORM_START;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.GROUP;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.HIERARCHY;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.LOCATION_PERMISSIONS_GRANTED;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.LOCATION_PERMISSIONS_NOT_GRANTED;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.LOCATION_PROVIDERS_DISABLED;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.LOCATION_PROVIDERS_ENABLED;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.LOCATION_TRACKING_DISABLED;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.LOCATION_TRACKING_ENABLED;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.PROMPT_NEW_REPEAT;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.QUESTION;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.REPEAT;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.SAVE_ERROR;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.UNKNOWN_EVENT_TYPE;
import static org.odk.collect.android.formentry.audit.AuditEventCSVLine.toCSVLine;

public class AuditEventCSVLineTest {

    private static final long START_TIME = 1545392727685L;
    private static final long END_TIME = 1545392728527L;

    //region CSV spec (https://tools.ietf.org/html/rfc4180)
    @Test
    public void commas_shouldBeSurroundedByQuotes() {
        AuditEvent auditEvent = new AuditEvent(1L, QUESTION, getTestFormIndex(), "a, b", "c, d", "e, f");
        auditEvent.recordValueChange("g, h");
        auditEvent.setEnd(2L);
        String csvLine = toCSVLine(auditEvent, false, true, true);
        assertThat(csvLine, is("question,/data/text1,1,2,\"a, b\",\"g, h\",\"c, d\",\"e, f\""));
    }

    @Test
    public void newlines_shouldBeSurroundedByQuotes() {
        AuditEvent auditEvent = new AuditEvent(1L, QUESTION, getTestFormIndex(), "a\nb", "c\nd", "e\nf");
        auditEvent.recordValueChange("g\nh");
        auditEvent.setEnd(2L);
        String csvLine = toCSVLine(auditEvent, false, true, true);
        assertThat(csvLine, is("question,/data/text1,1,2,\"a\nb\",\"g\nh\",\"c\nd\",\"e\nf\""));
    }

    @Test
    public void quotes_shouldBeEscaped_andSurroundedByQuotes() {
        AuditEvent auditEvent = new AuditEvent(1L, QUESTION, getTestFormIndex(), "a\"b", "c\"d", "e\"f");
        auditEvent.recordValueChange("g\"h");
        auditEvent.setEnd(2L);
        String csvLine = toCSVLine(auditEvent, false, true, true);
        assertThat(csvLine, is("question,/data/text1,1,2,\"a\"\"b\",\"g\"\"h\",\"c\"\"d\",\"e\"\"f\""));
    }
    //endregion

    @Test
    public void toString_() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, QUESTION, getTestFormIndex(), "", null, null);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertEquals("question,/data/text1,1545392727685,", toCSVLine(auditEvent, false, false, false));
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertFalse(auditEvent.isLocationAlreadySet());
        assertEquals("question,/data/text1,1545392727685,1545392728527", toCSVLine(auditEvent, false, false, false));
    }

    @Test
    public void toString_withLocationCoordinates() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, QUESTION, getTestFormIndex(), "", null, null);
        assertNotNull(auditEvent);
        auditEvent.setLocationCoordinates("54.35202520000001", "18.64663840000003", "10");
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertTrue(auditEvent.isLocationAlreadySet());
        assertEquals("question,/data/text1,1545392727685,1545392728527,54.35202520000001,18.64663840000003,10", toCSVLine(auditEvent, true, false, false));
    }

    @Test
    public void toString_withTrackingChanges() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, QUESTION, getTestFormIndex(), "First answer", null, null);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertFalse(auditEvent.isLocationAlreadySet());
        auditEvent.recordValueChange("Second answer");
        assertEquals("question,/data/text1,1545392727685,1545392728527,First answer,Second answer", toCSVLine(auditEvent, false, true, false));
    }

    @Test
    public void toString_withLocationCoordinates_andTrackingChanges() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, QUESTION, getTestFormIndex(), "First answer", null, null);
        assertNotNull(auditEvent);
        auditEvent.setLocationCoordinates("54.35202520000001", "18.64663840000003", "10");
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertTrue(auditEvent.isLocationAlreadySet());
        auditEvent.recordValueChange("Second, answer");
        assertEquals("question,/data/text1,1545392727685,1545392728527,54.35202520000001,18.64663840000003,10,First answer,\"Second, answer\"", toCSVLine(auditEvent, true, true, false));
    }

    @Test
    public void toStringNullValues() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, QUESTION, getTestFormIndex(), "Old value", null, null);
        assertNotNull(auditEvent);
        auditEvent.setLocationCoordinates("", "", "");
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertFalse(auditEvent.isLocationAlreadySet());
        auditEvent.recordValueChange("New value");
        assertEquals("question,/data/text1,1545392727685,1545392728527,,,,Old value,New value", toCSVLine(auditEvent, true, true, false));
    }

    @Test
    public void testEventTypes() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, QUESTION);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertEquals("question,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, FORM_START);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("form start,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, END_OF_FORM);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertEquals("end screen,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, REPEAT);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("repeat,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, PROMPT_NEW_REPEAT);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertEquals("add repeat,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, GROUP);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertEquals("group questions,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, BEGINNING_OF_FORM);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("beginning of form,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, FORM_EXIT);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("form exit,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, FORM_RESUME);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("form resume,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, FORM_SAVE);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("form save,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, FORM_FINALIZE);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("form finalize,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, HIERARCHY);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertEquals("jump,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, SAVE_ERROR);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("save error,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, FINALIZE_ERROR);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("finalize error,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, CONSTRAINT_ERROR);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("constraint error,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, DELETE_REPEAT);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("delete repeat,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, GOOGLE_PLAY_SERVICES_NOT_AVAILABLE);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("google play services not available,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, LOCATION_PERMISSIONS_GRANTED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("location permissions granted,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, LOCATION_PERMISSIONS_NOT_GRANTED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("location permissions not granted,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, LOCATION_TRACKING_ENABLED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("location tracking enabled,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, LOCATION_TRACKING_DISABLED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("location tracking disabled,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, LOCATION_PROVIDERS_ENABLED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("location providers enabled,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, LOCATION_PROVIDERS_DISABLED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("location providers disabled,,1545392727685,", toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, UNKNOWN_EVENT_TYPE);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("Unknown AuditEvent Type,,1545392727685,", toCSVLine(auditEvent, false, false, false));
    }

    private FormIndex getTestFormIndex() {
        TreeReference treeReference = new TreeReference();
        treeReference.add("data", 0);
        treeReference.add("text1", 0);

        return new FormIndex(0, treeReference);
    }
}