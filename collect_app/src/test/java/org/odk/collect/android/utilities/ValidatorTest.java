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
        assertTrue(Validator.isEmailAddressValid("email@domain.com"));
        assertTrue(Validator.isEmailAddressValid("firstname.lastname@domain.com"));
        assertTrue(Validator.isEmailAddressValid("email@subdomain.domain.com"));
        assertTrue(Validator.isEmailAddressValid("firstname+lastname@domain.com"));
        assertTrue(Validator.isEmailAddressValid("femail@123.123.123.123"));
        assertTrue(Validator.isEmailAddressValid("email@[123.123.123.123]"));
        assertTrue(Validator.isEmailAddressValid("\"email\"@domain.com"));
        assertTrue(Validator.isEmailAddressValid("1234567890@domain.com"));
        assertTrue(Validator.isEmailAddressValid("email@domain-one.com"));
        assertTrue(Validator.isEmailAddressValid("emailX@domain-one.com"));
        assertTrue(Validator.isEmailAddressValid("_______@domain.com"));
        assertTrue(Validator.isEmailAddressValid("email@domain.name"));
        assertTrue(Validator.isEmailAddressValid("email@domain.co.jp"));
        assertTrue(Validator.isEmailAddressValid("irstname-lastname@domain.com"));

        assertFalse(Validator.isEmailAddressValid("plainaddress"));
        assertFalse(Validator.isEmailAddressValid("@domain.com"));
        assertFalse(Validator.isEmailAddressValid("@"));
        assertFalse(Validator.isEmailAddressValid("email@"));
        assertFalse(Validator.isEmailAddressValid("email@com"));
        assertFalse(Validator.isEmailAddressValid("email.domain.com"));
        assertFalse(Validator.isEmailAddressValid("email@domain"));
    }
}
