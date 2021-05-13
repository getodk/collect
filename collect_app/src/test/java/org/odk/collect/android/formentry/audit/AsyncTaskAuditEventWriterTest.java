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

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.apache.commons.io.FileUtils;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.TreeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LooperMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.CHANGE_REASON;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.END_OF_FORM;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.FORM_EXIT;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.FORM_FINALIZE;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.FORM_RESUME;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.FORM_SAVE;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.FORM_START;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.HIERARCHY;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.LOCATION_PERMISSIONS_GRANTED;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.LOCATION_PROVIDERS_ENABLED;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.LOCATION_TRACKING_ENABLED;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.PROMPT_NEW_REPEAT;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.QUESTION;

@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.LEGACY)
public class AsyncTaskAuditEventWriterTest {

    private File auditFile;

    @Before
    public void setup() throws Exception {
        auditFile = File.createTempFile("audit", ".csv");
    }

    @Test
    public void saveAuditWithLocation() throws Exception {
        AsyncTaskAuditEventWriter writer = new AsyncTaskAuditEventWriter(auditFile, true, false, false, false);
        writer.writeEvents(getSampleAuditEventsWithLocations());

        String expectedAuditContent = FileUtils.readFileToString(auditFile);
        String expectedData = "event,node,start,end,latitude,longitude,accuracy\n" +
                "form start,,1548106927319,,,,\n" +
                "location tracking enabled,,548108908250,,,,\n" +
                "location permissions granted,,548108908255,,,,\n" +
                "location providers enabled,,548108908259,,,,\n" +
                "question,/data/q1,1548106927323,1548106930112,54.4112062,18.5896652,30.716999053955078\n" +
                "add repeat,/data/g1[1],1548106930118,1548106931611,54.4112062,18.5896652,30.716999053955078\n" +
                "question,/data/g1[1]/q2,1548106931612,1548106937122,54.4112062,18.5896652,30.716999053955078\n" +
                "add repeat,/data/g1[2],1548106937123,1548106938276,54.4112062,18.5896652,30.716999053955078\n" +
                "question,/data/g1[2]/q2,1548106938277,1548106948127,54.4112062,18.5896652,30.716999053955078\n" +
                "add repeat,/data/g1[3],1548106948128,1548106949446,54.4112062,18.5896652,30.716999053955078\n" +
                "end screen,,1548106949448,1548106953601,54.4112062,18.5896652,30.716999053955078\n" +
                "form save,,1548106953600,,54.4112062,18.5896652,30.716999053955078\n" +
                "form exit,,1548106953601,,54.4112062,18.5896652,30.716999053955078\n" +
                "form finalize,,1548106953601,,54.4112062,18.5896652,30.716999053955078\n";
        assertEquals(expectedData, expectedAuditContent);
    }

    @Test
    public void saveAuditWithLocationAndTrackingChanges() throws Exception {
        AsyncTaskAuditEventWriter writer = new AsyncTaskAuditEventWriter(auditFile, true, true, false, false);
        writer.writeEvents(getSampleAuditEventsWithLocationsAndTrackingChanges());

        String expectedAuditContent = FileUtils.readFileToString(auditFile);
        String expectedData = "event,node,start,end,latitude,longitude,accuracy,old-value,new-value\n" +
                "form start,,1548106927319,,,,,,\n" +
                "location tracking enabled,,548108908250,,,,,,\n" +
                "location permissions granted,,548108908255,,,,,,\n" +
                "location providers enabled,,548108908259,,,,,,\n" +
                "question,/data/q1,1548106927323,1548106930112,54.4112062,18.5896652,30.716999053955078,Old value,New Value\n" +
                "add repeat,/data/g1[1],1548106930118,1548106931611,54.4112062,18.5896652,30.716999053955078,,\n" +
                "end screen,,1548106949448,1548106953601,54.4112062,18.5896652,30.716999053955078,,\n" +
                "form save,,1548106953600,,54.4112062,18.5896652,30.716999053955078,,\n" +
                "form exit,,1548106953601,,54.4112062,18.5896652,30.716999053955078,,\n" +
                "form finalize,,1548106953601,,54.4112062,18.5896652,30.716999053955078,,\n";
        assertEquals(expectedData, expectedAuditContent);
    }

    @Test
    public void saveAuditWithUser() throws Exception {
        AsyncTaskAuditEventWriter writer = new AsyncTaskAuditEventWriter(auditFile, false, false, true, false);
        writer.writeEvents(getSampleAuditEventsWithUser());

        String expectedAuditContent = FileUtils.readFileToString(auditFile);
        String expectedData = "event,node,start,end,user\n" +
                "form start,,1548106927319,,User1\n" +
                "question,/data/q1,1548106927323,1548106930112,User1\n" +
                "add repeat,/data/g1[1],1548106930118,1548106931611,User1\n" +
                "question,/data/g1[1]/q2,1548106931612,1548106937122,User1\n" +
                "add repeat,/data/g1[2],1548106937123,1548106938276,User1\n" +
                "question,/data/g1[2]/q2,1548106938277,1548106948127,User1\n" +
                "add repeat,/data/g1[3],1548106948128,1548106949446,User1\n" +
                "end screen,,1548106949448,1548106953601,User1\n" +
                "form save,,1548106953600,,User1\n" +
                "form exit,,1548106953601,,User1\n" +
                "form finalize,,1548106953601,,User1\n";

        assertEquals(expectedData, expectedAuditContent);
    }

    @Test
    public void saveAuditWithChangeReason() throws Exception {
        AsyncTaskAuditEventWriter writer = new AsyncTaskAuditEventWriter(auditFile, false, false, false, true);
        writer.writeEvents(asList(
                new AuditEvent(1548108900606L, FORM_RESUME, null, null, null, null),
                new AuditEvent(1548108900606L, CHANGE_REASON, null, null, null, "A good reason")
        ));

        String auditContent = FileUtils.readFileToString(auditFile);
        String expectedData = "event,node,start,end,change-reason\n" +
                "form resume,,1548108900606,,\n" +
                "change reason,,1548108900606,,A good reason\n";
        assertEquals(expectedData, auditContent);
    }

    @Test
    public void whenChangeReasonHasCommaOrQuotes_escapesThem() throws Exception {
        AsyncTaskAuditEventWriter writer = new AsyncTaskAuditEventWriter(auditFile, false, false, false, true);
        writer.writeEvents(asList(
                new AuditEvent(1548108900606L, FORM_RESUME, null, null, null, null),
                new AuditEvent(1548108900606L, CHANGE_REASON, null, null, null, "A \"good\", reason")
        ));

        String auditContent = FileUtils.readFileToString(auditFile);
        String expectedData = "event,node,start,end,change-reason\n" +
                "form resume,,1548108900606,,\n" +
                "change reason,,1548108900606,,\"A \"\"good\"\", reason\"\n";
        assertEquals(expectedData, auditContent);
    }

    @Test
    public void whenUserHasCommaOrQuotes_escapesThem() throws Exception {
        AsyncTaskAuditEventWriter writer = new AsyncTaskAuditEventWriter(auditFile, false, false, true, false);

        List<AuditEvent> auditEvents = getSampleAuditEventsWithUser().subList(0, 1);
        auditEvents.get(0).setUser("User,\"1\"");
        writer.writeEvents(auditEvents);

        String expectedAuditContent = FileUtils.readFileToString(auditFile);
        String expectedData = "event,node,start,end,user\n" +
                "form start,,1548106927319,,\"User,\"\"1\"\"\"\n";
        assertEquals(expectedData, expectedAuditContent);
    }

    /**
     * A user could update the app and then resume form entry. In this case it would be possible
     * for the form to have an audit config that wasn't supported by the old app. In this case
     * the writer should update the header to account for the new data.
     */
    @Test
    public void whenAppUpdatedBetweenInstances_updatesHeader() throws Exception {
        // Use a form with enabled audit but without location
        AsyncTaskAuditEventWriter writer = new AsyncTaskAuditEventWriter(auditFile, false, false, false, false);
        writer.writeEvents(getSampleAuditEventsWithoutLocations());

        String expectedAuditContent = FileUtils.readFileToString(auditFile);
        String expectedData = "event,node,start,end\n" +
                "form start,,1548106927319,\n" +
                "question,/data/q1,1548106927323,1548106930112\n" +
                "add repeat,/data/g1[1],1548106930118,1548106931611\n" +
                "question,/data/g1[1]/q2,1548106931612,1548106937122\n" +
                "add repeat,/data/g1[2],1548106937123,1548106938276\n" +
                "question,/data/g1[2]/q2,1548106938277,1548106948127\n" +
                "add repeat,/data/g1[3],1548106948128,1548106949446\n" +
                "end screen,,1548106949448,1548106953601\n" +
                "form save,,1548106953600,\n" +
                "form exit,,1548106953601,\n" +
                "form finalize,,1548106953601,\n";
        assertEquals(expectedData, expectedAuditContent);

        // Upgrade a form to use location
        writer = new AsyncTaskAuditEventWriter(auditFile, true, false, false, false);
        writer.writeEvents(getMoreSampleAuditEventsWithLocations());

        expectedAuditContent = FileUtils.readFileToString(auditFile);
        String expectedData2 = "event,node,start,end,latitude,longitude,accuracy\n" +
                "form start,,1548106927319,\n" +
                "question,/data/q1,1548106927323,1548106930112\n" +
                "add repeat,/data/g1[1],1548106930118,1548106931611\n" +
                "question,/data/g1[1]/q2,1548106931612,1548106937122\n" +
                "add repeat,/data/g1[2],1548106937123,1548106938276\n" +
                "question,/data/g1[2]/q2,1548106938277,1548106948127\n" +
                "add repeat,/data/g1[3],1548106948128,1548106949446\n" +
                "end screen,,1548106949448,1548106953601\n" +
                "form save,,1548106953600,\n" +
                "form exit,,1548106953601,\n" +
                "form finalize,,1548106953601,\n" +
                "form resume,,1548108900606,,54.4112062,18.5896652,30.716999053955078\n" +
                "jump,,1548108906276,1548108908206,54.4112062,18.5896652,30.716999053955078\n" +
                "location tracking enabled,,548108908250,,,,\n" +
                "location permissions granted,,548108908255,,,,\n" +
                "location providers enabled,,548108908259,,,,\n" +
                "end screen,,1548108908285,1548108909730,54.4112062,18.5896652,30.716999053955078\n" +
                "form save,,1548108909730,,54.4112062,18.5896652,30.716999053955078\n" +
                "form exit,,1548108909730,,54.4112062,18.5896652,30.716999053955078\n" +
                "form finalize,,1548108909731,,54.4112062,18.5896652,30.716999053955078\n";
        assertEquals(expectedData2, expectedAuditContent);

        // Upgrade a form to use location and tracking changes
        writer = new AsyncTaskAuditEventWriter(auditFile, true, true, false, false);
        writer.writeEvents(getMoreSampleAuditEventsWithLocationsAndTrackingChanges());

        expectedAuditContent = FileUtils.readFileToString(auditFile);
        String expectedData3 = "event,node,start,end,latitude,longitude,accuracy,old-value,new-value\n" +
                "form start,,1548106927319,\n" +
                "question,/data/q1,1548106927323,1548106930112\n" +
                "add repeat,/data/g1[1],1548106930118,1548106931611\n" +
                "question,/data/g1[1]/q2,1548106931612,1548106937122\n" +
                "add repeat,/data/g1[2],1548106937123,1548106938276\n" +
                "question,/data/g1[2]/q2,1548106938277,1548106948127\n" +
                "add repeat,/data/g1[3],1548106948128,1548106949446\n" +
                "end screen,,1548106949448,1548106953601\n" +
                "form save,,1548106953600,\n" +
                "form exit,,1548106953601,\n" +
                "form finalize,,1548106953601,\n" +
                "form resume,,1548108900606,,54.4112062,18.5896652,30.716999053955078\n" +
                "jump,,1548108906276,1548108908206,54.4112062,18.5896652,30.716999053955078\n" +
                "location tracking enabled,,548108908250,,,,\n" +
                "location permissions granted,,548108908255,,,,\n" +
                "location providers enabled,,548108908259,,,,\n" +
                "end screen,,1548108908285,1548108909730,54.4112062,18.5896652,30.716999053955078\n" +
                "form save,,1548108909730,,54.4112062,18.5896652,30.716999053955078\n" +
                "form exit,,1548108909730,,54.4112062,18.5896652,30.716999053955078\n" +
                "form finalize,,1548108909731,,54.4112062,18.5896652,30.716999053955078\n" +
                "form resume,,1548108900606,,54.4112062,18.5896652,30.716999053955078,,\n" +
                "question,,1548108900700,,54.4112062,18.5896652,30.716999053955078,Old value,New value\n" +
                "question,,1548108903100,,54.4112062,18.5896652,30.716999053955078,\"Old value, with comma\",New value\n" +
                "question,,1548108903101,,54.4112062,18.5896652,30.716999053955078,\"Old value \n with linebreak\",\"New value \n with linebreak and \"\"quotes\"\"\"\n" +
                "question,,1548108904200,,54.4112062,18.5896652,30.716999053955078,Old value,\"New value, with comma\"\n" +
                "form save,,1548108909730,,54.4112062,18.5896652,30.716999053955078,,\n" +
                "form exit,,1548108909730,,54.4112062,18.5896652,30.716999053955078,,\n" +
                "form finalize,,1548108909731,,54.4112062,18.5896652,30.716999053955078,,\n";
        assertEquals(expectedData3, expectedAuditContent);

        // Upgrade a form to use location and tracking changes and user
        writer = new AsyncTaskAuditEventWriter(auditFile, true, true, true, false);
        writer.writeEvents(getMoreSampleAuditEventsWithLocationsAndTrackingChangesAndUser());

        expectedAuditContent = FileUtils.readFileToString(auditFile);
        String expectedData4 = "event,node,start,end,latitude,longitude,accuracy,old-value,new-value,user\n" +
                "form start,,1548106927319,\n" +
                "question,/data/q1,1548106927323,1548106930112\n" +
                "add repeat,/data/g1[1],1548106930118,1548106931611\n" +
                "question,/data/g1[1]/q2,1548106931612,1548106937122\n" +
                "add repeat,/data/g1[2],1548106937123,1548106938276\n" +
                "question,/data/g1[2]/q2,1548106938277,1548106948127\n" +
                "add repeat,/data/g1[3],1548106948128,1548106949446\n" +
                "end screen,,1548106949448,1548106953601\n" +
                "form save,,1548106953600,\n" +
                "form exit,,1548106953601,\n" +
                "form finalize,,1548106953601,\n" +
                "form resume,,1548108900606,,54.4112062,18.5896652,30.716999053955078\n" +
                "jump,,1548108906276,1548108908206,54.4112062,18.5896652,30.716999053955078\n" +
                "location tracking enabled,,548108908250,,,,\n" +
                "location permissions granted,,548108908255,,,,\n" +
                "location providers enabled,,548108908259,,,,\n" +
                "end screen,,1548108908285,1548108909730,54.4112062,18.5896652,30.716999053955078\n" +
                "form save,,1548108909730,,54.4112062,18.5896652,30.716999053955078\n" +
                "form exit,,1548108909730,,54.4112062,18.5896652,30.716999053955078\n" +
                "form finalize,,1548108909731,,54.4112062,18.5896652,30.716999053955078\n" +
                "form resume,,1548108900606,,54.4112062,18.5896652,30.716999053955078,,\n" +
                "question,,1548108900700,,54.4112062,18.5896652,30.716999053955078,Old value,New value\n" +
                "question,,1548108903100,,54.4112062,18.5896652,30.716999053955078,\"Old value, with comma\",New value\n" +
                "question,,1548108903101,,54.4112062,18.5896652,30.716999053955078,\"Old value \n with linebreak\",\"New value \n with linebreak and \"\"quotes\"\"\"\n" +
                "question,,1548108904200,,54.4112062,18.5896652,30.716999053955078,Old value,\"New value, with comma\"\n" +
                "form save,,1548108909730,,54.4112062,18.5896652,30.716999053955078,,\n" +
                "form exit,,1548108909730,,54.4112062,18.5896652,30.716999053955078,,\n" +
                "form finalize,,1548108909731,,54.4112062,18.5896652,30.716999053955078,,\n" +
                "form resume,,1548108900606,,54.4112062,18.5896652,30.716999053955078,,,User1\n" +
                "question,,1548108900700,,54.4112062,18.5896652,30.716999053955078,Old value,New value,User1\n" +
                "question,,1548108903100,,54.4112062,18.5896652,30.716999053955078,\"Old value, with comma\",New value,User1\n" +
                "question,,1548108903101,,54.4112062,18.5896652,30.716999053955078,\"Old value \n with linebreak\",\"New value \n with linebreak and \"\"quotes\"\"\",User1\n" +
                "question,,1548108904200,,54.4112062,18.5896652,30.716999053955078,Old value,\"New value, with comma\",User1\n" +
                "form save,,1548108909730,,54.4112062,18.5896652,30.716999053955078,,,User1\n" +
                "form exit,,1548108909730,,54.4112062,18.5896652,30.716999053955078,,,User1\n" +
                "form finalize,,1548108909731,,54.4112062,18.5896652,30.716999053955078,,,User1\n";
        assertEquals(expectedData4, expectedAuditContent);
    }

    private List<AuditEvent> getSampleAuditEventsWithUser() {
        List<AuditEvent> auditEvents = getSampleAuditEventsWithoutLocations();
        for (AuditEvent event : auditEvents) {
            event.setUser("User1");
        }

        return auditEvents;
    }

    private List<AuditEvent> getMoreSampleAuditEventsWithLocationsAndTrackingChangesAndUser() {
        List<AuditEvent> auditEvents = getMoreSampleAuditEventsWithLocationsAndTrackingChanges();
        for (AuditEvent event : auditEvents) {
            event.setUser("User1");
        }

        return auditEvents;
    }

    private List<AuditEvent> getSampleAuditEventsWithoutLocations() {
        AuditEvent event;
        ArrayList<AuditEvent> auditEvents = new ArrayList<>();
        auditEvents.add(new AuditEvent(1548106927319L, FORM_START));
        event = new AuditEvent(1548106927323L, QUESTION, getTestFormIndex("/data/q1"), "", null, null);
        event.setEnd(1548106930112L);
        auditEvents.add(event);
        event = new AuditEvent(1548106930118L, PROMPT_NEW_REPEAT, getTestFormIndex("/data/g1[1]"), "", null, null);
        event.setEnd(1548106931611L);
        auditEvents.add(event);
        event = new AuditEvent(1548106931612L, QUESTION, getTestFormIndex("/data/g1[1]/q2"), "", null, null);
        event.setEnd(1548106937122L);
        auditEvents.add(event);
        event = new AuditEvent(1548106937123L, PROMPT_NEW_REPEAT, getTestFormIndex("/data/g1[2]"), "", null, null);
        event.setEnd(1548106938276L);
        auditEvents.add(event);
        event = new AuditEvent(1548106938277L, QUESTION, getTestFormIndex("/data/g1[2]/q2"), "", null, null);
        event.setEnd(1548106948127L);
        auditEvents.add(event);
        event = new AuditEvent(1548106948128L, PROMPT_NEW_REPEAT, getTestFormIndex("/data/g1[3]"), "", null, null);
        event.setEnd(1548106949446L);
        auditEvents.add(event);
        event = new AuditEvent(1548106949448L, END_OF_FORM);
        event.setEnd(1548106953601L);
        auditEvents.add(event);
        auditEvents.add(new AuditEvent(1548106953600L, FORM_SAVE));
        auditEvents.add(new AuditEvent(1548106953601L, FORM_EXIT));
        auditEvents.add(new AuditEvent(1548106953601L, FORM_FINALIZE));
        return auditEvents;
    }

    private ArrayList<AuditEvent> getMoreSampleAuditEventsWithLocations() {
        AuditEvent event;
        ArrayList<AuditEvent> auditEvents = new ArrayList<>();
        event = new AuditEvent(1548108900606L, FORM_RESUME);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        auditEvents.add(event);
        event = new AuditEvent(1548108906276L, HIERARCHY);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.setEnd(1548108908206L);
        auditEvents.add(event);
        event = new AuditEvent(548108908250L, LOCATION_TRACKING_ENABLED);
        event.setLocationCoordinates("", "", "");
        auditEvents.add(event);
        event = new AuditEvent(548108908255L, LOCATION_PERMISSIONS_GRANTED);
        event.setLocationCoordinates("", "", "");
        auditEvents.add(event);
        event = new AuditEvent(548108908259L, LOCATION_PROVIDERS_ENABLED);
        event.setLocationCoordinates("", "", "");
        auditEvents.add(event);
        event = new AuditEvent(1548108908285L, END_OF_FORM);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.setEnd(1548108909730L);
        auditEvents.add(event);
        event = new AuditEvent(1548108909730L, FORM_SAVE);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        auditEvents.add(event);
        event = new AuditEvent(1548108909730L, FORM_EXIT);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        auditEvents.add(event);
        event = new AuditEvent(1548108909731L, FORM_FINALIZE);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        auditEvents.add(event);
        return auditEvents;
    }

    private ArrayList<AuditEvent> getMoreSampleAuditEventsWithLocationsAndTrackingChanges() {
        AuditEvent event;
        ArrayList<AuditEvent> auditEvents = new ArrayList<>();
        event = new AuditEvent(1548108900606L, FORM_RESUME);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        auditEvents.add(event);
        event = new AuditEvent(1548108900700L, QUESTION, null, "Old value", null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.recordValueChange("New value");
        auditEvents.add(event);
        event = new AuditEvent(1548108903100L, QUESTION, null, "Old value, with comma", null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.recordValueChange("New value");
        auditEvents.add(event);
        event = new AuditEvent(1548108903101L, QUESTION, null, "Old value \n with linebreak", null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.recordValueChange("New value \n with linebreak and \"quotes\"");
        auditEvents.add(event);
        event = new AuditEvent(1548108904200L, QUESTION, null, "Old value", null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.recordValueChange("New value, with comma");
        auditEvents.add(event);
        event = new AuditEvent(1548108909730L, FORM_SAVE);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        auditEvents.add(event);
        event = new AuditEvent(1548108909730L, FORM_EXIT);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        auditEvents.add(event);
        event = new AuditEvent(1548108909731L, FORM_FINALIZE);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        auditEvents.add(event);
        return auditEvents;
    }

    private ArrayList<AuditEvent> getSampleAuditEventsWithLocations() {
        ArrayList<AuditEvent> auditEvents = new ArrayList<>();
        AuditEvent event;
        event = new AuditEvent(1548106927319L, FORM_START);
        event.setLocationCoordinates("", "", "");
        auditEvents.add(event);
        event = new AuditEvent(548108908250L, LOCATION_TRACKING_ENABLED);
        event.setLocationCoordinates("", "", "");
        auditEvents.add(event);
        event = new AuditEvent(548108908255L, LOCATION_PERMISSIONS_GRANTED);
        event.setLocationCoordinates("", "", "");
        auditEvents.add(event);
        event = new AuditEvent(548108908259L, LOCATION_PROVIDERS_ENABLED);
        event.setLocationCoordinates("", "", "");
        auditEvents.add(event);
        event = new AuditEvent(1548106927323L, QUESTION, getTestFormIndex("/data/q1"), "", null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.setEnd(1548106930112L);
        auditEvents.add(event);
        event = new AuditEvent(1548106930118L, PROMPT_NEW_REPEAT, getTestFormIndex("/data/g1[1]"), "", null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.setEnd(1548106931611L);
        auditEvents.add(event);
        event = new AuditEvent(1548106931612L, QUESTION, getTestFormIndex("/data/g1[1]/q2"), "", null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.setEnd(1548106937122L);
        auditEvents.add(event);
        event = new AuditEvent(1548106937123L, PROMPT_NEW_REPEAT, getTestFormIndex("/data/g1[2]"), "", null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.setEnd(1548106938276L);
        auditEvents.add(event);
        event = new AuditEvent(1548106938277L, QUESTION, getTestFormIndex("/data/g1[2]/q2"), "", null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.setEnd(1548106948127L);
        auditEvents.add(event);
        event = new AuditEvent(1548106948128L, PROMPT_NEW_REPEAT, getTestFormIndex("/data/g1[3]"), "", null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.setEnd(1548106949446L);
        auditEvents.add(event);
        event = new AuditEvent(1548106949448L, END_OF_FORM);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.setEnd(1548106953601L);
        auditEvents.add(event);
        event = new AuditEvent(1548106953600L, FORM_SAVE);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        auditEvents.add(event);
        event = new AuditEvent(1548106953601L, FORM_EXIT);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        auditEvents.add(event);
        event = new AuditEvent(1548106953601L, FORM_FINALIZE);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        auditEvents.add(event);
        return auditEvents;
    }

    private ArrayList<AuditEvent> getSampleAuditEventsWithLocationsAndTrackingChanges() {
        ArrayList<AuditEvent> auditEvents = new ArrayList<>();
        AuditEvent event;
        event = new AuditEvent(1548106927319L, FORM_START);
        event.setLocationCoordinates("", "", "");
        auditEvents.add(event);
        event = new AuditEvent(548108908250L, LOCATION_TRACKING_ENABLED);
        event.setLocationCoordinates("", "", "");
        auditEvents.add(event);
        event = new AuditEvent(548108908255L, LOCATION_PERMISSIONS_GRANTED);
        event.setLocationCoordinates("", "", "");
        auditEvents.add(event);
        event = new AuditEvent(548108908259L, LOCATION_PROVIDERS_ENABLED);
        event.setLocationCoordinates("", "", "");
        auditEvents.add(event);
        event = new AuditEvent(1548106927323L, QUESTION, getTestFormIndex("/data/q1"), "Old value", null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.recordValueChange("New Value");
        event.setEnd(1548106930112L);
        auditEvents.add(event);
        event = new AuditEvent(1548106930118L, PROMPT_NEW_REPEAT, getTestFormIndex("/data/g1[1]"), null, null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.setEnd(1548106931611L);
        auditEvents.add(event);
        event = new AuditEvent(1548106949448L, END_OF_FORM, null, null, null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        event.setEnd(1548106953601L);
        auditEvents.add(event);
        event = new AuditEvent(1548106953600L, FORM_SAVE, null, null, null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        auditEvents.add(event);
        event = new AuditEvent(1548106953601L, FORM_EXIT, null, null, null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        auditEvents.add(event);
        event = new AuditEvent(1548106953601L, FORM_FINALIZE, null, null, null, null);
        event.setLocationCoordinates("54.4112062", "18.5896652", "30.716999053955078");
        auditEvents.add(event);
        return auditEvents;
    }

    /**
     * Given an XPath path, generate a corresponding {@link TreeReference} and a fake
     * {@link FormIndex} that doesn't correspond to any real form definition. The only thing we care
     * about for the {@link FormIndex} are the instance indexes at every level. Everything else can
     * be faked.
     * <p>
     * TODO: once {@link AuditEvent}'s getXPathPath moves to FormIndex, just use a mock
     */
    private FormIndex getTestFormIndex(String xpathPath) {
        String[] nodes = xpathPath.split("/");
        TreeReference treeReference = new TreeReference();
        nodes = Arrays.copyOfRange(nodes, 1, nodes.length); // take care of leading /
        ArrayList<Integer> positions = new ArrayList<>();

        for (String node : nodes) {
            String[] parts = node.split("\\[");

            String nodeName = parts[0];
            int position = 0;
            if (parts.length > 1) {
                position = Integer.parseInt(parts[1].replace("]", "")) - 1;
                positions.add(position);
            } else {
                positions.add(-1);
            }
            treeReference.add(nodeName, position);
        }

        FormIndex formIndex = null;
        for (int i = nodes.length - 1; i > 0; i--) { // exclude the root node
            formIndex = new FormIndex(formIndex, -1, positions.get(i), treeReference);
        }

        return formIndex;
    }
}
