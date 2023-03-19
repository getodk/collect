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

import org.javarosa.form.api.FormEntryController;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.BEGINNING_OF_FORM;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.END_OF_FORM;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.GROUP;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.PROMPT_NEW_REPEAT;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.REPEAT;
import static org.odk.collect.android.formentry.audit.AuditEvent.AuditEventType.UNKNOWN_EVENT_TYPE;

public class AuditEventTest {

    @Test
    public void getAuditEventTypeFromFecTypeTest() {
        assertEquals(BEGINNING_OF_FORM, AuditEvent.getAuditEventTypeFromFecType(FormEntryController.EVENT_BEGINNING_OF_FORM));
        assertEquals(GROUP, AuditEvent.getAuditEventTypeFromFecType(FormEntryController.EVENT_GROUP));
        assertEquals(REPEAT, AuditEvent.getAuditEventTypeFromFecType(FormEntryController.EVENT_REPEAT));
        assertEquals(PROMPT_NEW_REPEAT, AuditEvent.getAuditEventTypeFromFecType(FormEntryController.EVENT_PROMPT_NEW_REPEAT));
        assertEquals(END_OF_FORM, AuditEvent.getAuditEventTypeFromFecType(FormEntryController.EVENT_END_OF_FORM));
        assertEquals(UNKNOWN_EVENT_TYPE, AuditEvent.getAuditEventTypeFromFecType(100));
    }
}
