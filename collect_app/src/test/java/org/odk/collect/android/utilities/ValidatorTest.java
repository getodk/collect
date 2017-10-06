/*
 * Copyright 2017 Nafundi
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
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ValidatorTest {

    @Test
    public void emailValidationTestCase() {
        // https://en.wikipedia.org/wiki/Email_address
        assertTrue(Validator.isEmailAddressValid("prettyandsimple@example.com"));
        assertTrue(Validator.isEmailAddressValid("very.common@example.com"));
        assertTrue(Validator.isEmailAddressValid("disposable.style.email.with+symbol@example.com"));
        assertTrue(Validator.isEmailAddressValid("other.email-with-dash@example.com"));
        assertTrue(Validator.isEmailAddressValid("fully-qualified-domain@example.com."));
        assertTrue(Validator.isEmailAddressValid("x@example.com"));
        assertTrue(Validator.isEmailAddressValid("\"very.unusual.@.unusual.com\"@example.com"));
        assertTrue(Validator.isEmailAddressValid("\"very.(),:;<>[]\\\".VERY.\\\"very@\\\\ \\\"very\\\".unusual\"@strange.example.com"));
        assertTrue(Validator.isEmailAddressValid("example-indeed@strange-example.com"));
        assertTrue(Validator.isEmailAddressValid("admin@mailserver1"));
        assertTrue(Validator.isEmailAddressValid("#!$%&'*+-/=?^_`{}|~@example.org"));
        assertTrue(Validator.isEmailAddressValid("\"()<>[]:,;@\\\\\\\"!#$%&'-/=?^_`{}| ~.a\"@example.org"));
        assertTrue(Validator.isEmailAddressValid("\" \"@example.org"));
        assertTrue(Validator.isEmailAddressValid("example@s.solutions"));
        assertTrue(Validator.isEmailAddressValid("user@localserver"));
        assertTrue(Validator.isEmailAddressValid("user@[IPv6:2001:DB8::1"));

        assertFalse(Validator.isEmailAddressValid("plainaddress"));
        assertFalse(Validator.isEmailAddressValid("@domain.com"));
        assertFalse(Validator.isEmailAddressValid("@"));
        assertFalse(Validator.isEmailAddressValid("email@"));
        assertFalse(Validator.isEmailAddressValid("email.domain.com"));
    }
}
