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

package org.odk.collect.android.formentry.audit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.location.LocationClient.Priority.PRIORITY_BALANCED_POWER_ACCURACY;
import static org.odk.collect.location.LocationClient.Priority.PRIORITY_HIGH_ACCURACY;
import static org.odk.collect.location.LocationClient.Priority.PRIORITY_LOW_POWER;
import static org.odk.collect.location.LocationClient.Priority.PRIORITY_NO_POWER;

public class AuditConfigTest {

    @Test
    public void testParameters() {
        AuditConfig auditConfig = new AuditConfig.Builder().setMode("high-accuracy").setLocationMinInterval("10").setLocationMaxAge("60").setIsTrackingChangesEnabled(true).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();

        assertTrue(auditConfig.isTrackingChangesEnabled());
        assertTrue(auditConfig.isLocationEnabled());
        assertEquals(PRIORITY_HIGH_ACCURACY, auditConfig.getLocationPriority());
        assertEquals(10000, auditConfig.getLocationMinInterval().intValue());
        assertEquals(60000, auditConfig.getLocationMaxAge().intValue());

        auditConfig = new AuditConfig.Builder().setMode("high-accuracy").setLocationMinInterval("0").setLocationMaxAge("60").setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();

        assertFalse(auditConfig.isTrackingChangesEnabled());
        assertTrue(auditConfig.isLocationEnabled());
        assertEquals(PRIORITY_HIGH_ACCURACY, auditConfig.getLocationPriority());
        assertEquals(1000, auditConfig.getLocationMinInterval().intValue());
        assertEquals(60000, auditConfig.getLocationMaxAge().intValue());
    }

    @Test
    public void logLocationCoordinatesOnlyIfAllParametersAreSet() {
        AuditConfig auditConfig = new AuditConfig.Builder().setMode("high-accuracy").setLocationMinInterval("10").setLocationMaxAge("60").setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertTrue(auditConfig.isLocationEnabled());
        auditConfig = new AuditConfig.Builder().setMode(null).setLocationMinInterval("10").setLocationMaxAge("60").setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertFalse(auditConfig.isLocationEnabled());
        auditConfig = new AuditConfig.Builder().setMode(null).setLocationMinInterval(null).setLocationMaxAge("60").setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertFalse(auditConfig.isLocationEnabled());
        auditConfig = new AuditConfig.Builder().setMode(null).setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertFalse(auditConfig.isLocationEnabled());
        auditConfig = new AuditConfig.Builder().setMode("balanced").setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertFalse(auditConfig.isLocationEnabled());
        auditConfig = new AuditConfig.Builder().setMode("balanced").setLocationMinInterval("10").setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertFalse(auditConfig.isLocationEnabled());
        auditConfig = new AuditConfig.Builder().setMode("balanced").setLocationMinInterval(null).setLocationMaxAge("60").setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertFalse(auditConfig.isLocationEnabled());
        auditConfig = new AuditConfig.Builder().setMode(null).setLocationMinInterval(null).setLocationMaxAge("60").setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertFalse(auditConfig.isLocationEnabled());
    }

    @Test
    public void testPriorities() {
        AuditConfig auditConfig = new AuditConfig.Builder().setMode("high_accuracy").setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertEquals(PRIORITY_HIGH_ACCURACY, auditConfig.getLocationPriority());
        auditConfig = new AuditConfig.Builder().setMode("high-accuracy").setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertEquals(PRIORITY_HIGH_ACCURACY, auditConfig.getLocationPriority());
        auditConfig = new AuditConfig.Builder().setMode("HIGH_ACCURACY").setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertEquals(PRIORITY_HIGH_ACCURACY, auditConfig.getLocationPriority());
        auditConfig = new AuditConfig.Builder().setMode("balanced").setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertEquals(PRIORITY_BALANCED_POWER_ACCURACY, auditConfig.getLocationPriority());
        auditConfig = new AuditConfig.Builder().setMode("BALANCED").setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertEquals(PRIORITY_BALANCED_POWER_ACCURACY, auditConfig.getLocationPriority());
        auditConfig = new AuditConfig.Builder().setMode("low_power").setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertEquals(PRIORITY_LOW_POWER, auditConfig.getLocationPriority());
        auditConfig = new AuditConfig.Builder().setMode("low-power").setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertEquals(PRIORITY_LOW_POWER, auditConfig.getLocationPriority());
        auditConfig = new AuditConfig.Builder().setMode("low_POWER").setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertEquals(PRIORITY_LOW_POWER, auditConfig.getLocationPriority());
        auditConfig = new AuditConfig.Builder().setMode("no_power").setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertEquals(PRIORITY_NO_POWER, auditConfig.getLocationPriority());
        auditConfig = new AuditConfig.Builder().setMode("no-power").setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertEquals(PRIORITY_NO_POWER, auditConfig.getLocationPriority());
        auditConfig = new AuditConfig.Builder().setMode("NO_power").setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertEquals(PRIORITY_NO_POWER, auditConfig.getLocationPriority());
        auditConfig = new AuditConfig.Builder().setMode("qwerty").setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertEquals(PRIORITY_HIGH_ACCURACY, auditConfig.getLocationPriority());
        auditConfig = new AuditConfig.Builder().setMode("").setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertEquals(PRIORITY_HIGH_ACCURACY, auditConfig.getLocationPriority());
        auditConfig = new AuditConfig.Builder().setMode(null).setLocationMinInterval(null).setLocationMaxAge(null).setIsTrackingChangesEnabled(false).setIsIdentifyUserEnabled(false).setIsTrackChangesReasonEnabled(false).createAuditConfig();
        assertNull(auditConfig.getLocationPriority());
    }
}
