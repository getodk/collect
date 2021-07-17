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

package org.odk.collect.android.utilities;

import org.junit.Test;
import org.odk.collect.android.externaldata.ExternalAppsUtils;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ExternalAppUtilsTest {

    @Test
    public void extractIntentNameTest() {
        assertEquals("org.opendatakit.counter", ExternalAppsUtils.extractIntentName("org.opendatakit.counter(form_id='counter-form', form_name='Counter Form', question_id='1', question_name='Counter 1', increment=true())"));
        assertEquals("org.opendatakit.counter", ExternalAppsUtils.extractIntentName("org.opendatakit.counter()"));
        assertEquals("org.opendatakit.counter", ExternalAppsUtils.extractIntentName("org.opendatakit.counter"));
    }

    @Test
    public void extractParametersTest() {
        // Simple case
        Map<String, String> result = ExternalAppsUtils.extractParameters("org.opendatakit.counter(form_id='counter-form', form_name='Counter Form', question_id='1', question_name='Counter 1', increment=true())");
        assertCounterAppParameters(result);

        // No spaces at all
        result = ExternalAppsUtils.extractParameters("org.opendatakit.counter(form_id='counter-form',form_name='Counter Form',question_id='1',question_name='Counter 1',increment=true())");
        assertCounterAppParameters(result);

        // Spaces everywhere
        result = ExternalAppsUtils.extractParameters("   org.opendatakit.counter ( form_id = 'counter-form' , form_name = 'Counter Form' , question_id = '1' , question_name = 'Counter 1' , increment = true()   )   ");
        assertCounterAppParameters(result);

        // Commas in values
        result = ExternalAppsUtils.extractParameters("org.companyX.appX(parameter1='value1', parameter2='value2a, value2b'");
        assertEquals(2, result.size());
        assertTrue(result.keySet().contains("parameter1"));
        assertTrue(result.keySet().contains("parameter2"));

        assertEquals("'value1'", result.get("parameter1"));
        assertEquals("'value2a, value2b'", result.get("parameter2"));

        // Regex with commas
        result = ExternalAppsUtils.extractParameters("org.companyX.appX(regex1='/^([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$/', regex2='/^[a-z0-9_-]{6,18}$/', regex3='/^(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?$/'");
        assertEquals(3, result.size());
        assertTrue(result.keySet().contains("regex1"));
        assertTrue(result.keySet().contains("regex2"));
        assertTrue(result.keySet().contains("regex3"));

        assertEquals("'/^([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$/'", result.get("regex1"));
        assertEquals("'/^[a-z0-9_-]{6,18}$/'", result.get("regex2"));
        assertEquals("'/^(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?$/'", result.get("regex3"));

        // Values populated from a form
        result = ExternalAppsUtils.extractParameters("android.intent.action.SENDTO(sms_body= /send-sms-or-email-in-form/message , uri_data= /send-sms-or-email-in-form/send_to )");
        assertEquals(2, result.size());
        assertTrue(result.keySet().contains("sms_body"));
        assertTrue(result.keySet().contains("uri_data"));

        assertEquals("/send-sms-or-email-in-form/message", result.get("sms_body"));
        assertEquals("/send-sms-or-email-in-form/send_to", result.get("uri_data"));

        // Real sample from a user: https://forum.getodk.org/t/external-app-with-intent-parameters/20125/5?u=grzesiek2010
        result = ExternalAppsUtils.extractParameters("ex:ch.novelt.odkcompanion.OPEN(current_value=/afp_report/group_mini_cif/cn, match='^[0-9]{4}W[0-9]{2}-[0-9]{1,5}$', filter='^AFP:([0-9]{4}W[0-9]{2}-[0-9]{1,5})')");
        assertEquals(3, result.size());
        assertTrue(result.keySet().contains("current_value"));
        assertTrue(result.keySet().contains("match"));
        assertTrue(result.keySet().contains("filter"));

        assertEquals("/afp_report/group_mini_cif/cn", result.get("current_value"));
        assertEquals("'^[0-9]{4}W[0-9]{2}-[0-9]{1,5}$'", result.get("match"));
        assertEquals("'^AFP:([0-9]{4}W[0-9]{2}-[0-9]{1,5})'", result.get("filter"));
    }

    @Test
    public void asStringDataTest() {
        assertNull(ExternalAppsUtils.asStringData(null));
        assertEquals("Test Value", ExternalAppsUtils.asStringData("Test Value").getValue().toString());
        assertEquals("TestValue", ExternalAppsUtils.asStringData("TestValue").getValue().toString());
        assertEquals("Test Value 3", ExternalAppsUtils.asStringData("Test Value 3").getValue().toString());
        assertEquals(" Test Value 4 ", ExternalAppsUtils.asStringData(" Test Value 4 ").getValue().toString());
    }

    @Test
    public void asIntegerDataTest() {
        assertNull(ExternalAppsUtils.asIntegerData(null));
        assertNull(ExternalAppsUtils.asIntegerData(""));
        assertNull(ExternalAppsUtils.asIntegerData("5.4"));
        assertEquals("5", ExternalAppsUtils.asIntegerData("5").getValue().toString());
        assertEquals("-5", ExternalAppsUtils.asIntegerData("-5").getValue().toString());
        assertEquals("125", ExternalAppsUtils.asIntegerData("125").getValue().toString());
    }

    @Test
    public void asDecimalDataTest() {
        assertNull(ExternalAppsUtils.asDecimalData(null));
        assertNull(ExternalAppsUtils.asDecimalData(""));
        assertNull(ExternalAppsUtils.asDecimalData("5..24"));
        assertNull(ExternalAppsUtils.asDecimalData("5.24c"));
        assertEquals("5.0", ExternalAppsUtils.asDecimalData("5").getValue().toString());
        assertEquals("5.0", ExternalAppsUtils.asDecimalData("5.00").getValue().toString());
        assertEquals("5.0", ExternalAppsUtils.asDecimalData("05").getValue().toString());
        assertEquals("5.24", ExternalAppsUtils.asDecimalData("5.24").getValue().toString());
        assertEquals("5.24", ExternalAppsUtils.asDecimalData("5.240").getValue().toString());
        assertEquals("5.204", ExternalAppsUtils.asDecimalData("5.204").getValue().toString());
        assertEquals("-5.0", ExternalAppsUtils.asDecimalData("-5").getValue().toString());
        assertEquals("-27.333", ExternalAppsUtils.asDecimalData("-27.333").getValue().toString());
    }

    private void assertCounterAppParameters(Map<String, String> result) {
        assertEquals(5, result.size());
        assertTrue(result.keySet().contains("form_id"));
        assertTrue(result.keySet().contains("form_name"));
        assertTrue(result.keySet().contains("question_id"));
        assertTrue(result.keySet().contains("question_name"));
        assertTrue(result.keySet().contains("increment"));

        assertEquals("'counter-form'", result.get("form_id"));
        assertEquals("'Counter Form'", result.get("form_name"));
        assertEquals("'1'", result.get("question_id"));
        assertEquals("'Counter 1'", result.get("question_name"));
        assertEquals("true()", result.get("increment"));
    }
}
