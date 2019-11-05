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

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.BEGINNING_OF_FORM;
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

    private File testInstanceFile;
    // All values are set so location coordinates should be collected
    private final AuditConfig testAuditConfig = new AuditConfig("high-priority", "10", "60", false);
    // At least one value is not set so location coordinates shouldn't be collected
    private final AuditConfig testAuditConfigWithNullValues = new AuditConfig("high-priority", "10", null, false);

    @Before
    public void setup() throws Exception {
        testInstanceFile = File.createTempFile("testForm", ".xml");
    }

    @Test
    public void getMostAccurateLocationTest() {
        Location location1 = mock(Location.class);
        when(location1.getLatitude()).thenReturn(54.380746599999995);
        when(location1.getLongitude()).thenReturn(18.606523);
        when(location1.getAccuracy()).thenReturn(5f);
        when(location1.getTime()).thenReturn(1548156641000L);

        AuditEventLogger auditEventLogger = new AuditEventLogger(testInstanceFile, testAuditConfig);
        auditEventLogger.addLocation(location1);
        assertEquals(1, auditEventLogger.getLocations().size());

        Location location2 = mock(Location.class);
        when(location2.getLatitude()).thenReturn(54.37971080550665);
        when(location2.getLongitude()).thenReturn(18.612874470947304);
        when(location2.getAccuracy()).thenReturn(10f);
        when(location2.getTime()).thenReturn(1548156655000L);

        auditEventLogger.addLocation(location2);
        assertEquals(2, auditEventLogger.getLocations().size());

        Location location3 = mock(Location.class);
        when(location3.getLatitude()).thenReturn(54.3819102504987);
        when(location3.getLongitude()).thenReturn(18.62025591015629);
        when(location3.getAccuracy()).thenReturn(12f);
        when(location3.getTime()).thenReturn(1548156665000L);

        auditEventLogger.addLocation(location3);
        assertEquals(3, auditEventLogger.getLocations().size());

        Location location4 = mock(Location.class);
        when(location4.getLatitude()).thenReturn(54.38620882544086);
        when(location4.getLongitude()).thenReturn(18.62523409008793);
        when(location4.getAccuracy()).thenReturn(7f);
        when(location4.getTime()).thenReturn(1548156688000L);

        auditEventLogger.addLocation(location4);
        assertEquals(4, auditEventLogger.getLocations().size());

        Location location5 = mock(Location.class);
        when(location5.getLatitude()).thenReturn(54.39070685202294);
        when(location5.getLongitude()).thenReturn(18.617166005371132);
        when(location5.getAccuracy()).thenReturn(20f);
        when(location5.getTime()).thenReturn(1548156710000L);

        auditEventLogger.addLocation(location5);
        assertEquals(5, auditEventLogger.getLocations().size());

        Location location = auditEventLogger.getMostAccurateLocation(1548156712000L);

        // The first recorded location has been removed because it's expired
        // It's time: 1548156641000, current time: 1548156712000, location-max-age: 60s
        assertEquals(4, auditEventLogger.getLocations().size());

        assertEquals(54.38620882544086, location.getLatitude(), 0);
        assertEquals(18.62523409008793, location.getLongitude(), 0);
        assertEquals(7f, location.getAccuracy(), 0);
    }

    @Test
    public void isAuditEnabledTest() {
        AuditEventLogger auditEventLogger = new AuditEventLogger(testInstanceFile, testAuditConfig);
        assertTrue(auditEventLogger.isAuditEnabled());
        auditEventLogger = new AuditEventLogger(testInstanceFile, null);
        assertFalse(auditEventLogger.isAuditEnabled());
    }

    @Test
    public void isDuplicateOfLastAuditEventTest() {
        AuditEventLogger auditEventLogger = new AuditEventLogger(testInstanceFile, testAuditConfig);
        auditEventLogger.logEvent(LOCATION_PROVIDERS_ENABLED, false, 0);
        assertTrue(auditEventLogger.isDuplicateOfLastLocationEvent(LOCATION_PROVIDERS_ENABLED));
        auditEventLogger.logEvent(LOCATION_PROVIDERS_DISABLED, false, 0);
        assertTrue(auditEventLogger.isDuplicateOfLastLocationEvent(LOCATION_PROVIDERS_DISABLED));
        assertFalse(auditEventLogger.isDuplicateOfLastLocationEvent(LOCATION_PROVIDERS_ENABLED));
        assertEquals(2, auditEventLogger.getAuditEvents().size());
    }

    @Test
    public void addLocationCoordinatesToAuditEventIfNeededTest() {
        AuditEventLogger auditEventLogger = new AuditEventLogger(testInstanceFile, testAuditConfigWithNullValues);

        auditEventLogger.logEvent(END_OF_FORM, false, 0);
        assertFalse(auditEventLogger.getAuditEvents().get(0).isLocationAlreadySet());

        auditEventLogger = new AuditEventLogger(testInstanceFile, testAuditConfig);

        Location location = mock(Location.class);
        when(location.getLatitude()).thenReturn(54.39070685202294);
        when(location.getLongitude()).thenReturn(18.617166005371132);
        when(location.getAccuracy()).thenReturn(20f);
        when(location.getTime()).thenReturn(1548156710000L);

        auditEventLogger.addLocation(location);

        auditEventLogger.logEvent(END_OF_FORM, false, 0);

        assertTrue(auditEventLogger.getAuditEvents().get(0).isLocationAlreadySet());
    }

    @Test
    public void logEventTest() {
        AuditEventLogger auditEventLogger = new AuditEventLogger(testInstanceFile, testAuditConfig);

        auditEventLogger.logEvent(BEGINNING_OF_FORM, false, 0); //shouldn't be logged
        auditEventLogger.logEvent(QUESTION, false, 0);
        auditEventLogger.getAuditEvents().get(auditEventLogger.getAuditEvents().size() - 1).setEnd(1548156710000L);
        auditEventLogger.logEvent(GROUP, false, 0);
        auditEventLogger.getAuditEvents().get(auditEventLogger.getAuditEvents().size() - 1).setEnd(1548156770000L);
        auditEventLogger.logEvent(PROMPT_NEW_REPEAT, false, 0);
        auditEventLogger.getAuditEvents().get(auditEventLogger.getAuditEvents().size() - 1).setEnd(1548156830000L);
        auditEventLogger.logEvent(REPEAT, false, 0); //shouldn't be logged
        auditEventLogger.logEvent(END_OF_FORM, false, 0);
        auditEventLogger.logEvent(FORM_START, false, 0);
        auditEventLogger.logEvent(FORM_RESUME, false, 0);
        auditEventLogger.logEvent(FORM_SAVE, false, 0);
        auditEventLogger.logEvent(FORM_FINALIZE, false, 0);
        auditEventLogger.logEvent(HIERARCHY, false, 0);
        auditEventLogger.getAuditEvents().get(auditEventLogger.getAuditEvents().size() - 1).setEnd(1548156890000L);
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

        assertEquals(21, auditEventLogger.getAuditEvents().size());
    }
}
