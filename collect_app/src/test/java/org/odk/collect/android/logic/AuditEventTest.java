/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.logic;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.BEGINNING_OF_FORM;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.CONSTRAINT_ERROR;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.DELETE_REPEAT;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.END_OF_FORM;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.FINALIZE_ERROR;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.FORM_EXIT;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.FORM_FINALIZE;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.FORM_RESUME;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.FORM_SAVE;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.FORM_START;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.GROUP;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.HIERARCHY;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.LOCATION_PERMISSIONS_GRANTED;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.LOCATION_PERMISSIONS_NOT_GRANTED;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.LOCATION_PROVIDERS_DISABLED;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.LOCATION_PROVIDERS_ENABLED;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.LOCATION_TRACKING_DISABLED;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.LOCATION_TRACKING_ENABLED;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.PROMPT_NEW_REPEAT;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.QUESTION;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.REPEAT;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.SAVE_ERROR;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.UNKNOWN_EVENT_TYPE;

public class AuditEventTest {
    private static final long START_TIME = 1545392727685L;
    private static final long END_TIME = 1545392728527L;

    @Test
    public void testToString() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, QUESTION, false, false, getTestFormIndex(), "");
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertEquals("question,/data/text1,1545392727685,", auditEvent.toString());
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertFalse(auditEvent.isLocationAlreadySet());
        assertEquals("question,/data/text1,1545392727685,1545392728527", auditEvent.toString());
    }

    @Test
    public void testToStringWithLocationCoordinates() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, QUESTION, true, false, getTestFormIndex(), "");
        assertNotNull(auditEvent);
        auditEvent.setLocationCoordinates("54.35202520000001", "18.64663840000003", "10");
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertTrue(auditEvent.isLocationAlreadySet());
        assertEquals("question,/data/text1,1545392727685,1545392728527,54.35202520000001,18.64663840000003,10", auditEvent.toString());
    }

    @Test
    public void testToStringWithTrackingChanges() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, QUESTION, false, true, getTestFormIndex(), "First answer");
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertFalse(auditEvent.isLocationAlreadySet());
        auditEvent.recordValueChange("Second answer");
        assertEquals("question,/data/text1,1545392727685,1545392728527,First answer,Second answer", auditEvent.toString());
    }

    @Test
    public void testToStringWithLocationCoordinatesAndTrackingChanges() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, QUESTION, true, true, getTestFormIndex(), "First answer");
        assertNotNull(auditEvent);
        auditEvent.setLocationCoordinates("54.35202520000001", "18.64663840000003", "10");
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertTrue(auditEvent.isLocationAlreadySet());
        auditEvent.recordValueChange("Second, answer");
        assertEquals("question,/data/text1,1545392727685,1545392728527,54.35202520000001,18.64663840000003,10,First answer,\"Second, answer\"", auditEvent.toString());
    }

    @Test
    public void testToStringNullValues() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, QUESTION, true, true, getTestFormIndex(), "Old value");
        assertNotNull(auditEvent);
        auditEvent.setLocationCoordinates("", "", "");
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertFalse(auditEvent.isLocationAlreadySet());
        auditEvent.recordValueChange("New value");
        assertEquals("question,/data/text1,1545392727685,1545392728527,,,,Old value,New value", auditEvent.toString());
    }

    @Test
    public void testEventTypes() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, QUESTION);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertEquals("question,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, FORM_START);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("form start,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, END_OF_FORM);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertEquals("end screen,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, REPEAT);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("repeat,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, PROMPT_NEW_REPEAT);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertEquals("add repeat,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, GROUP);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertEquals("group questions,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, BEGINNING_OF_FORM);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("beginning of form,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, FORM_EXIT);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("form exit,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, FORM_RESUME);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("form resume,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, FORM_SAVE);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("form save,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, FORM_FINALIZE);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("form finalize,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, HIERARCHY);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertEquals("jump,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, SAVE_ERROR);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("save error,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, FINALIZE_ERROR);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("finalize error,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, CONSTRAINT_ERROR);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("constraint error,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, DELETE_REPEAT);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("delete repeat,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, GOOGLE_PLAY_SERVICES_NOT_AVAILABLE);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("google play services not available,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, LOCATION_PERMISSIONS_GRANTED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("location permissions granted,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, LOCATION_PERMISSIONS_NOT_GRANTED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("location permissions not granted,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, LOCATION_TRACKING_ENABLED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("location tracking enabled,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, LOCATION_TRACKING_DISABLED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("location tracking disabled,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, LOCATION_PROVIDERS_ENABLED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("location providers enabled,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, LOCATION_PROVIDERS_DISABLED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("location providers disabled,,1545392727685,", auditEvent.toString());

        auditEvent = new AuditEvent(START_TIME, UNKNOWN_EVENT_TYPE);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        assertEquals("Unknown AuditEvent Type,,1545392727685,", auditEvent.toString());
    }

    @Test
    public void getAuditEventTypeFromFecTypeTest() {
        assertEquals(BEGINNING_OF_FORM, AuditEvent.getAuditEventTypeFromFecType(FormEntryController.EVENT_BEGINNING_OF_FORM));
        assertEquals(GROUP, AuditEvent.getAuditEventTypeFromFecType(FormEntryController.EVENT_GROUP));
        assertEquals(REPEAT, AuditEvent.getAuditEventTypeFromFecType(FormEntryController.EVENT_REPEAT));
        assertEquals(PROMPT_NEW_REPEAT, AuditEvent.getAuditEventTypeFromFecType(FormEntryController.EVENT_PROMPT_NEW_REPEAT));
        assertEquals(END_OF_FORM, AuditEvent.getAuditEventTypeFromFecType(FormEntryController.EVENT_END_OF_FORM));
        assertEquals(UNKNOWN_EVENT_TYPE, AuditEvent.getAuditEventTypeFromFecType(100));
    }

    private FormIndex getTestFormIndex() {
        TreeReference treeReference = new TreeReference();
        treeReference.add("data", 0);
        treeReference.add("text1", 0);

        return new FormIndex(0, treeReference);
    }
}
