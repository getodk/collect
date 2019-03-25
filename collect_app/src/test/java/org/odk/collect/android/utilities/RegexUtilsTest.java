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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.odk.collect.android.utilities.RegexUtils.normalizeFormName;

public class RegexUtilsTest {

    @Test
    public void normalizeFormNameTest() {
        assertNull(normalizeFormName(null, false));
        assertEquals("Lorem", normalizeFormName("Lorem", false));
        assertEquals("Lorem ipsum", normalizeFormName("Lorem ipsum", false));
        assertEquals("Lorem ipsum", normalizeFormName("Lorem\nipsum", false));
        assertEquals("Lorem  ipsum", normalizeFormName("Lorem\n\nipsum", false));
        assertEquals(" Lorem ipsum ", normalizeFormName("\nLorem\nipsum\n", false));

        assertNull(normalizeFormName(null, true));
        assertNull(normalizeFormName("Lorem", true));
        assertNull(normalizeFormName("Lorem ipsum", true));
        assertEquals("Lorem ipsum", normalizeFormName("Lorem\nipsum", true));
        assertEquals("Lorem  ipsum", normalizeFormName("Lorem\n\nipsum", true));
        assertEquals(" Lorem ipsum ", normalizeFormName("\nLorem\nipsum\n", true));
    }
}
