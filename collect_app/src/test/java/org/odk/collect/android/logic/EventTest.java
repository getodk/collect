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

import org.junit.Test;

import static org.javarosa.form.api.FormEntryController.EVENT_QUESTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.utilities.EventLogger.EventTypes.CONSTRAINT_ERROR;
import static org.odk.collect.android.utilities.EventLogger.EventTypes.DELETE_REPEAT;
import static org.odk.collect.android.utilities.EventLogger.EventTypes.FEC;
import static org.odk.collect.android.utilities.EventLogger.EventTypes.FINALIZE_ERROR;
import static org.odk.collect.android.utilities.EventLogger.EventTypes.FORM_EXIT;
import static org.odk.collect.android.utilities.EventLogger.EventTypes.FORM_FINALIZE;
import static org.odk.collect.android.utilities.EventLogger.EventTypes.FORM_RESUME;
import static org.odk.collect.android.utilities.EventLogger.EventTypes.FORM_SAVE;
import static org.odk.collect.android.utilities.EventLogger.EventTypes.FORM_START;
import static org.odk.collect.android.utilities.EventLogger.EventTypes.HIERARCHY;
import static org.odk.collect.android.utilities.EventLogger.EventTypes.SAVE_ERROR;

public class EventTest {
    private static final long START_TIME = 1545392727685L;
    private static final long END_TIME = 1545392728527L;

    @Test
    public void testToString() {
        Event event = new Event(START_TIME, FEC, EVENT_QUESTION, "/data/text1");
        assertNotNull(event);
        assertTrue(event.isIntervalViewEvent());
        assertEquals("question,/data/text1,1545392727685,", event.toString());
        event.setEnd(END_TIME);
        assertEquals("question,/data/text1,1545392727685,1545392728527", event.toString());
    }

    @Test
    public void testToStringWithLocationCoordinates() {
        Event event = new Event(START_TIME, FEC, EVENT_QUESTION, "/data/text1");
        event.setLocationCoordinates("54.35202520000001", "18.64663840000003", "10");
        event.setEnd(END_TIME);
        assertNotNull(event);
        assertTrue(event.isIntervalViewEvent());
        assertEquals("question,/data/text1,1545392727685,1545392728527,54.35202520000001,18.64663840000003,10", event.toString());
    }

    @Test
    public void testToStringNullValues() {
        Event event = new Event(START_TIME, FEC, EVENT_QUESTION, null);
        event.setLocationCoordinates(null, null, null);
        assertNotNull(event);
        assertTrue(event.isIntervalViewEvent());
        event.setEnd(END_TIME);
        assertEquals("question,null,1545392727685,1545392728527,null,null,null", event.toString());
    }

    @Test
    public void testEventTypes() {
        Event event = new Event(START_TIME, FEC, EVENT_QUESTION, "");
        assertNotNull(event);
        assertTrue(event.isIntervalViewEvent());
        assertEquals("question,,1545392727685,", event.toString());

        event = new Event(START_TIME, FORM_START, 0, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("form start,,1545392727685,", event.toString());

        event = new Event(START_TIME, FORM_EXIT, 0, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("form exit,,1545392727685,", event.toString());

        event = new Event(START_TIME, FORM_RESUME, 0, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("form resume,,1545392727685,", event.toString());

        event = new Event(START_TIME, FORM_SAVE, 0, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("form save,,1545392727685,", event.toString());

        event = new Event(START_TIME, FORM_FINALIZE, 0, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("form finalize,,1545392727685,", event.toString());

        event = new Event(START_TIME, HIERARCHY, 0, "");
        assertNotNull(event);
        assertTrue(event.isIntervalViewEvent());
        assertEquals("jump,,1545392727685,", event.toString());

        event = new Event(START_TIME, SAVE_ERROR, 0, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("save error,,1545392727685,", event.toString());

        event = new Event(START_TIME, FINALIZE_ERROR, 0, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("finalize error,,1545392727685,", event.toString());

        event = new Event(START_TIME, CONSTRAINT_ERROR, 0, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("constraint error,,1545392727685,", event.toString());

        event = new Event(START_TIME, DELETE_REPEAT, 0, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("delete repeat,,1545392727685,", event.toString());
    }
}
