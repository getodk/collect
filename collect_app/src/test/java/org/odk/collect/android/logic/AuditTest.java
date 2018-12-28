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

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.location.client.LocationClient.Priority.PRIORITY_BALANCED_POWER_ACCURACY;
import static org.odk.collect.android.location.client.LocationClient.Priority.PRIORITY_HIGH_ACCURACY;
import static org.odk.collect.android.location.client.LocationClient.Priority.PRIORITY_LOW_POWER;
import static org.odk.collect.android.location.client.LocationClient.Priority.PRIORITY_NO_POWER;

public class AuditTest {

    @Test
    public void testParameters() {
        Audit audit = new Audit("high-accuracy", "10", "60");

        assertTrue(audit.collectLocationCoordinates());
        Assert.assertEquals(PRIORITY_HIGH_ACCURACY, audit.getLocationPriority());
        Assert.assertEquals(10, audit.getLocationInterval().intValue());
        Assert.assertEquals(60000, audit.getLocationAge().intValue());

        audit = new Audit("high-accuracy", "0", "60");

        assertTrue(audit.collectLocationCoordinates());
        Assert.assertEquals(PRIORITY_HIGH_ACCURACY, audit.getLocationPriority());
        Assert.assertEquals(1, audit.getLocationInterval().intValue());
        Assert.assertEquals(60000, audit.getLocationAge().intValue());
    }

    @Test
    public void logLocationCoordinatesOnlyIfAllParametersAreSet() {
        Audit audit = new Audit("high-accuracy", "10", "60");
        assertTrue(audit.collectLocationCoordinates());
        audit = new Audit(null, "10", "60");
        assertFalse(audit.collectLocationCoordinates());
        audit = new Audit(null, null, "60");
        assertFalse(audit.collectLocationCoordinates());
        audit = new Audit(null, null, null);
        assertFalse(audit.collectLocationCoordinates());
        audit = new Audit("balanced", null, null);
        assertFalse(audit.collectLocationCoordinates());
        audit = new Audit("balanced", "10", null);
        assertFalse(audit.collectLocationCoordinates());
        audit = new Audit("balanced", null, "60");
        assertFalse(audit.collectLocationCoordinates());
        audit = new Audit(null, null, "60");
        assertFalse(audit.collectLocationCoordinates());
    }

    @Test
    public void testPriorities() {
        Audit audit = new Audit("high_accuracy", null, null);
        Assert.assertEquals(PRIORITY_HIGH_ACCURACY, audit.getLocationPriority());
        audit = new Audit("high-accuracy", null, null);
        Assert.assertEquals(PRIORITY_HIGH_ACCURACY, audit.getLocationPriority());
        audit = new Audit("HIGH_ACCURACY", null, null);
        Assert.assertEquals(PRIORITY_HIGH_ACCURACY, audit.getLocationPriority());
        audit = new Audit("balanced", null, null);
        Assert.assertEquals(PRIORITY_BALANCED_POWER_ACCURACY, audit.getLocationPriority());
        audit = new Audit("BALANCED", null, null);
        Assert.assertEquals(PRIORITY_BALANCED_POWER_ACCURACY, audit.getLocationPriority());
        audit = new Audit("low_power", null, null);
        Assert.assertEquals(PRIORITY_LOW_POWER, audit.getLocationPriority());
        audit = new Audit("low-power", null, null);
        Assert.assertEquals(PRIORITY_LOW_POWER, audit.getLocationPriority());
        audit = new Audit("low_POWER", null, null);
        Assert.assertEquals(PRIORITY_LOW_POWER, audit.getLocationPriority());
        audit = new Audit("no_power", null, null);
        Assert.assertEquals(PRIORITY_NO_POWER, audit.getLocationPriority());
        audit = new Audit("no-power", null, null);
        Assert.assertEquals(PRIORITY_NO_POWER, audit.getLocationPriority());
        audit = new Audit("NO_power", null, null);
        Assert.assertEquals(PRIORITY_NO_POWER, audit.getLocationPriority());
        audit = new Audit("qwerty", null, null);
        Assert.assertEquals(PRIORITY_HIGH_ACCURACY, audit.getLocationPriority());
        audit = new Audit("", null, null);
        Assert.assertEquals(PRIORITY_HIGH_ACCURACY, audit.getLocationPriority());
        audit = new Audit(null, null, null);
        assertNull(audit.getLocationPriority());
    }
}
