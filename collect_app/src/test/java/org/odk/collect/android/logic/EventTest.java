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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.logic.Event.EventTypes.BACKGROUND_LOCATION_DISABLED;
import static org.odk.collect.android.logic.Event.EventTypes.BACKGROUND_LOCATION_ENABLED;
import static org.odk.collect.android.logic.Event.EventTypes.CONSTRAINT_ERROR;
import static org.odk.collect.android.logic.Event.EventTypes.DELETE_REPEAT;
import static org.odk.collect.android.logic.Event.EventTypes.FINALIZE_ERROR;
import static org.odk.collect.android.logic.Event.EventTypes.FORM_EXIT;
import static org.odk.collect.android.logic.Event.EventTypes.FORM_FINALIZE;
import static org.odk.collect.android.logic.Event.EventTypes.FORM_RESUME;
import static org.odk.collect.android.logic.Event.EventTypes.FORM_SAVE;
import static org.odk.collect.android.logic.Event.EventTypes.FORM_START;
import static org.odk.collect.android.logic.Event.EventTypes.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE;
import static org.odk.collect.android.logic.Event.EventTypes.HIERARCHY;
import static org.odk.collect.android.logic.Event.EventTypes.LOCATION_PERMISSIONS_GRANTED;
import static org.odk.collect.android.logic.Event.EventTypes.LOCATION_PERMISSIONS_NOT_GRANTED;
import static org.odk.collect.android.logic.Event.EventTypes.LOCATION_PROVIDERS_DISABLED;
import static org.odk.collect.android.logic.Event.EventTypes.LOCATION_PROVIDERS_ENABLED;
import static org.odk.collect.android.logic.Event.EventTypes.QUESTION;
import static org.odk.collect.android.logic.Event.EventTypes.SAVE_ERROR;

public class EventTest {
    private static final long START_TIME = 1545392727685L;
    private static final long END_TIME = 1545392728527L;

    @Test
    public void testToString() {
        Event event = new Event(START_TIME, QUESTION, "/data/text1");
        assertNotNull(event);
        assertTrue(event.isIntervalViewEvent());
        assertEquals("question,/data/text1,1545392727685,", event.toString());
        event.setEnd(END_TIME);
        assertEquals("question,/data/text1,1545392727685,1545392728527", event.toString());
    }

    @Test
    public void testToStringWithLocationCoordinates() {
        Event event = new Event(START_TIME, QUESTION, "/data/text1");
        event.setLocationCoordinates("54.35202520000001", "18.64663840000003", "10");
        event.setEnd(END_TIME);
        assertNotNull(event);
        assertTrue(event.isIntervalViewEvent());
        assertEquals("question,/data/text1,1545392727685,1545392728527,54.35202520000001,18.64663840000003,10", event.toString());
    }

    @Test
    public void testToStringNullValues() {
        Event event = new Event(START_TIME, QUESTION, null);
        event.setLocationCoordinates(null, null, null);
        assertNotNull(event);
        assertTrue(event.isIntervalViewEvent());
        event.setEnd(END_TIME);
        assertEquals("question,null,1545392727685,1545392728527", event.toString());
    }

    @Test
    public void testEventTypes() {
        Event event = new Event(START_TIME, QUESTION, "");
        assertNotNull(event);
        assertTrue(event.isIntervalViewEvent());
        assertEquals("question,,1545392727685,", event.toString());

        event = new Event(START_TIME, FORM_START, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("form start,,1545392727685,", event.toString());

        event = new Event(START_TIME, FORM_EXIT, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("form exit,,1545392727685,", event.toString());

        event = new Event(START_TIME, FORM_RESUME, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("form resume,,1545392727685,", event.toString());

        event = new Event(START_TIME, FORM_SAVE, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("form save,,1545392727685,", event.toString());

        event = new Event(START_TIME, FORM_FINALIZE, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("form finalize,,1545392727685,", event.toString());

        event = new Event(START_TIME, HIERARCHY, "");
        assertNotNull(event);
        assertTrue(event.isIntervalViewEvent());
        assertEquals("jump,,1545392727685,", event.toString());

        event = new Event(START_TIME, SAVE_ERROR, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("save error,,1545392727685,", event.toString());

        event = new Event(START_TIME, FINALIZE_ERROR, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("finalize error,,1545392727685,", event.toString());

        event = new Event(START_TIME, CONSTRAINT_ERROR, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("constraint error,,1545392727685,", event.toString());

        event = new Event(START_TIME, DELETE_REPEAT, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("delete repeat,,1545392727685,", event.toString());

        event = new Event(START_TIME, GOOGLE_PLAY_SERVICES_NOT_AVAILABLE, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("google play services not available,,1545392727685,", event.toString());

        event = new Event(START_TIME, LOCATION_PERMISSIONS_GRANTED, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("location permissions granted,,1545392727685,", event.toString());

        event = new Event(START_TIME, LOCATION_PERMISSIONS_NOT_GRANTED,  "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("location permissions not granted,,1545392727685,", event.toString());

        event = new Event(START_TIME, BACKGROUND_LOCATION_ENABLED, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("background location enabled,,1545392727685,", event.toString());

        event = new Event(START_TIME, BACKGROUND_LOCATION_DISABLED, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("background location disabled,,1545392727685,", event.toString());

        event = new Event(START_TIME, LOCATION_PROVIDERS_ENABLED, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("location providers enabled,,1545392727685,", event.toString());

        event = new Event(START_TIME, LOCATION_PROVIDERS_DISABLED, "");
        assertNotNull(event);
        assertFalse(event.isIntervalViewEvent());
        assertEquals("location providers disabled,,1545392727685,", event.toString());
    }
}
