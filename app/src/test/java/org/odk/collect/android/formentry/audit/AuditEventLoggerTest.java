/*
 * Copyright 2019 Nafundi
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

package org.odk.collect.android.formentry.audit;

import android.location.Location;

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.BEGINNING_OF_FORM;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.CHANGE_REASON;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.CONSTRAINT_ERROR;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.DELETE_REPEAT;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.END_OF_FORM;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.FINALIZE_ERROR;
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

public class AuditEventLoggerTest {

    // All values are set so location coordinates should be collected
    private final AuditConfig testAuditConfig = new AuditConfig.Builder().setMode("high-priority").setLocationMinInterval("10").setLocationMaxAge("60").setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
    // At least one value is not set so location coordinates shouldn't be collected
    private final AuditConfig testAuditConfigWithNullValues = new AuditConfig.Builder().setMode("high-priority").setLocationMinInterval("10").setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();

    private final TestWriter testWriter = new TestWriter();
    private final FormController formController = mock(FormController.class);

    @Before
    public void setup() {
        FormEntryPrompt prompt = new MockFormEntryPromptBuilder()
                .withAnswerDisplayText("The answer")
                .build();
        when(formController.getQuestionPrompt(any())).thenReturn(prompt);
    }

    @Test
    public void whenAuditConfigIsNull_doesntWriteEvents() {
        AuditEventLogger auditEventLogger = new AuditEventLogger(null, testWriter, formController);

        auditEventLogger.logEvent(END_OF_FORM, false, 0);
        auditEventLogger.flush(); // Triggers event writing
        assertEquals(0, testWriter.auditEvents.size());
    }

    @Test
    public void usesMostAccurateLocationForEvents() {
        final AuditEventLogger auditEventLogger = new AuditEventLogger(testAuditConfig, testWriter, formController);

        Location location1 = mock(Location.class);
        when(location1.getLatitude()).thenReturn(54.380746599999995);
        when(location1.getLongitude()).thenReturn(18.606523);
        when(location1.getAccuracy()).thenReturn(5f);
        when(location1.getTime()).thenReturn(1548156641000L);
        auditEventLogger.addLocation(location1);

        Location location2 = mock(Location.class);
        when(location2.getLatitude()).thenReturn(54.37971080550665);
        when(location2.getLongitude()).thenReturn(18.612874470947304);
        when(location2.getAccuracy()).thenReturn(10f);
        when(location2.getTime()).thenReturn(1548156655000L);
        auditEventLogger.addLocation(location2);

        Location location3 = mock(Location.class);
        when(location3.getLatitude()).thenReturn(54.3819102504987);
        when(location3.getLongitude()).thenReturn(18.62025591015629);
        when(location3.getAccuracy()).thenReturn(12f);
        when(location3.getTime()).thenReturn(1548156665000L);
        auditEventLogger.addLocation(location3);

        Location location4 = mock(Location.class);
        when(location4.getLatitude()).thenReturn(54.38620882544086);
        when(location4.getLongitude()).thenReturn(18.62523409008793);
        when(location4.getAccuracy()).thenReturn(7f);
        when(location4.getTime()).thenReturn(1548156688000L);
        auditEventLogger.addLocation(location4);

        Location location5 = mock(Location.class);
        when(location5.getLatitude()).thenReturn(54.39070685202294);
        when(location5.getLongitude()).thenReturn(18.617166005371132);
        when(location5.getAccuracy()).thenReturn(20f);
        when(location5.getTime()).thenReturn(1548156710000L);
        auditEventLogger.addLocation(location5);

        auditEventLogger.logEvent(END_OF_FORM, false, 1548156712000L);
        auditEventLogger.flush();

        AuditEvent auditEvent = testWriter.auditEvents.get(0);
        assertEquals(String.valueOf(location4.getLatitude()), auditEvent.getLatitude());
        assertEquals(String.valueOf(location4.getLongitude()), auditEvent.getLongitude());
        assertEquals(String.valueOf(location4.getAccuracy()), auditEvent.getAccuracy());
    }

    @Test
    public void expiresLocationsOlderThan60Seconds() {
        final AuditEventLogger auditEventLogger = new AuditEventLogger(testAuditConfig, testWriter, formController);

        Location location1 = mock(Location.class);
        when(location1.getLatitude()).thenReturn(54.380746599999995);
        when(location1.getLongitude()).thenReturn(18.606523);
        when(location1.getAccuracy()).thenReturn(2f);
        when(location1.getTime()).thenReturn(0L);
        auditEventLogger.addLocation(location1);

        Location location2 = mock(Location.class);
        when(location2.getLatitude()).thenReturn(54.37971080550665);
        when(location2.getLongitude()).thenReturn(18.612874470947304);
        when(location2.getAccuracy()).thenReturn(1f);
        when(location2.getTime()).thenReturn(61 * 1000L);
        auditEventLogger.addLocation(location2);

        auditEventLogger.logEvent(END_OF_FORM, false, 120 * 1000L);
        auditEventLogger.flush();

        AuditEvent auditEvent = testWriter.auditEvents.get(0);
        assertEquals(String.valueOf(location2.getLatitude()), auditEvent.getLatitude());
        assertEquals(String.valueOf(location2.getLongitude()), auditEvent.getLongitude());
        assertEquals(String.valueOf(location2.getAccuracy()), auditEvent.getAccuracy());
    }

    @Test
    public void whenNoLocationSet_doesntAddedLocationToEvents() {
        AuditEventLogger auditEventLogger = new AuditEventLogger(testAuditConfigWithNullValues, testWriter, formController);

        auditEventLogger.logEvent(END_OF_FORM, false, 0);
        auditEventLogger.flush(); // Triggers event writing

        assertFalse(testWriter.auditEvents.get(0).isLocationAlreadySet());
    }

    @Test
    public void isDuplicateOfLastAuditEventTest() {
        AuditEventLogger auditEventLogger = new AuditEventLogger(testAuditConfig, testWriter, formController);
        auditEventLogger.logEvent(LOCATION_PROVIDERS_ENABLED, false, 0);
        assertTrue(auditEventLogger.isDuplicateOfLastLocationEvent(LOCATION_PROVIDERS_ENABLED));
        auditEventLogger.logEvent(LOCATION_PROVIDERS_DISABLED, false, 0);
        assertTrue(auditEventLogger.isDuplicateOfLastLocationEvent(LOCATION_PROVIDERS_DISABLED));
        assertFalse(auditEventLogger.isDuplicateOfLastLocationEvent(LOCATION_PROVIDERS_ENABLED));

        auditEventLogger.flush(); // Triggers event writing
        assertEquals(2, testWriter.auditEvents.size());
    }

    @Test
    public void withUserSet_addsUserToEvents() {
        AuditEventLogger auditEventLogger = new AuditEventLogger(new AuditConfig.Builder().setMode(null).setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(true).setIsTrackChangesReasonEnabled(false).createAuditConfig(), testWriter, formController);
        auditEventLogger.setUser("Riker");

        auditEventLogger.logEvent(END_OF_FORM, false, 0);
        auditEventLogger.flush(); // Triggers event writing

        assertEquals("Riker", testWriter.auditEvents.get(0).getUser());
    }

    @Test
    public void logEvent_WithChangeReason_addsChangeReasonToEvent() {
        AuditEventLogger auditEventLogger = new AuditEventLogger(new AuditConfig.Builder().setMode(null).setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(true).createAuditConfig(), testWriter, formController);

        auditEventLogger.logEvent(CHANGE_REASON, null, false, null, 123L, "Blah");
        auditEventLogger.flush(); // Triggers event writing

        assertEquals("Blah", testWriter.auditEvents.get(0).getChangeReason());
    }

    @Test
    public void testEventTypes() {
        AuditEventLogger auditEventLogger = new AuditEventLogger(testAuditConfig, testWriter, formController);

        auditEventLogger.logEvent(BEGINNING_OF_FORM, false, 0); //shouldn't be logged
        auditEventLogger.logEvent(QUESTION, false, 0);
        auditEventLogger.logEvent(GROUP, false, 0);
        auditEventLogger.logEvent(PROMPT_NEW_REPEAT, false, 0);
        auditEventLogger.logEvent(REPEAT, false, 0); //shouldn't be logged
        auditEventLogger.logEvent(END_OF_FORM, false, 0);
        auditEventLogger.logEvent(FORM_START, false, 0);
        auditEventLogger.logEvent(FORM_RESUME, false, 0);
        auditEventLogger.logEvent(FORM_SAVE, false, 0);
        auditEventLogger.logEvent(FORM_FINALIZE, false, 0);
        auditEventLogger.logEvent(HIERARCHY, false, 0);
        auditEventLogger.logEvent(SAVE_ERROR, false, 0);
        auditEventLogger.logEvent(FINALIZE_ERROR, false, 0);
        auditEventLogger.logEvent(CONSTRAINT_ERROR, false, 0);
        auditEventLogger.logEvent(DELETE_REPEAT, false, 0);
        auditEventLogger.logEvent(GOOGLE_PLAY_SERVICES_NOT_AVAILABLE, false, 0);
        auditEventLogger.logEvent(LOCATION_PERMISSIONS_GRANTED, false, 0);
        auditEventLogger.logEvent(LOCATION_PERMISSIONS_NOT_GRANTED, false, 0);
        auditEventLogger.logEvent(LOCATION_TRACKING_ENABLED, false, 0);
        auditEventLogger.logEvent(LOCATION_TRACKING_DISABLED, false, 0);
        auditEventLogger.logEvent(LOCATION_PROVIDERS_ENABLED, false, 0);
        auditEventLogger.logEvent(LOCATION_PROVIDERS_DISABLED, false, 0);
        auditEventLogger.logEvent(UNKNOWN_EVENT_TYPE, false, 0);

        auditEventLogger.flush(); // Triggers event writing
        assertEquals(21, testWriter.auditEvents.size());
    }

    private static class TestWriter implements AuditEventLogger.AuditEventWriter {

        List<AuditEvent> auditEvents = new ArrayList<>();

        @Override
        public void writeEvents(List<AuditEvent> auditEvents) {
            this.auditEvents.addAll(auditEvents);
        }

        @Override
        public boolean isWriting() {
            return false;
        }
    }
}
