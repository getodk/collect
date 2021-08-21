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
package org.odk.collect.shared

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.odk.collect.shared.strings.Validator

class ValidatorTest {
    @Test
    fun emailValidationTestCase() {
        // https://en.wikipedia.org/wiki/Email_address
        assertTrue(Validator.isEmailAddressValid("prettyandsimple@example.com"))
        assertTrue(Validator.isEmailAddressValid("very.common@example.com"))
        assertTrue(Validator.isEmailAddressValid("disposable.style.email.with+symbol@example.com"))
        assertTrue(Validator.isEmailAddressValid("other.email-with-dash@example.com"))
        assertTrue(Validator.isEmailAddressValid("fully-qualified-domain@example.com."))
        assertTrue(Validator.isEmailAddressValid("x@example.com"))
        assertTrue(Validator.isEmailAddressValid("\"very.unusual.@.unusual.com\"@example.com"))
        assertTrue(Validator.isEmailAddressValid("\"very.(),:;<>[]\\\".VERY.\\\"very@\\\\ \\\"very\\\".unusual\"@strange.example.com"))
        assertTrue(Validator.isEmailAddressValid("example-indeed@strange-example.com"))
        assertTrue(Validator.isEmailAddressValid("admin@mailserver1"))
        assertTrue(Validator.isEmailAddressValid("#!$%&'*+-/=?^_`{}|~@example.org"))
        assertTrue(Validator.isEmailAddressValid("\"()<>[]:,;@\\\\\\\"!#$%&'-/=?^_`{}| ~.a\"@example.org"))
        assertTrue(Validator.isEmailAddressValid("\" \"@example.org"))
        assertTrue(Validator.isEmailAddressValid("example@s.solutions"))
        assertTrue(Validator.isEmailAddressValid("user@localserver"))
        assertTrue(Validator.isEmailAddressValid("user@[IPv6:2001:DB8::1"))
        assertFalse(Validator.isEmailAddressValid("plainaddress"))
        assertFalse(Validator.isEmailAddressValid("@domain.com"))
        assertFalse(Validator.isEmailAddressValid("@"))
        assertFalse(Validator.isEmailAddressValid("email@"))
        assertFalse(Validator.isEmailAddressValid("email.domain.com"))
    }

    @Test
    fun urlValidationTest() {
        //  https://en.wikipedia.org/wiki/Template:URL/testcases
        assertTrue(Validator.isUrlValid("http://www.example.com"))
        assertTrue(Validator.isUrlValid("https://www.example.com"))
        assertTrue(Validator.isUrlValid("http://www.example.com/"))
        assertTrue(Validator.isUrlValid("https://www.EXAMPLE.cOm"))
        assertTrue(Validator.isUrlValid("HTTPS://www.EXAMPLE.cOm/"))
        assertTrue(Validator.isUrlValid("https://www.example.com/"))
        assertTrue(Validator.isUrlValid("http://example.com"))
        assertTrue(Validator.isUrlValid("https://example.com"))
        assertTrue(Validator.isUrlValid("http://www.example.com/foo/bar"))
        assertTrue(Validator.isUrlValid("http://www.example.com/foo/bar/"))
        assertTrue(Validator.isUrlValid("http://www.example.com/foO/BaR"))
        assertTrue(Validator.isUrlValid("https://www.example.com/foo/bar"))
        assertTrue(Validator.isUrlValid("http://example.com/foo/bar"))
        assertTrue(Validator.isUrlValid("https://example.com/foo/bar"))
        assertTrue(Validator.isUrlValid("http://example.com/foo/bar"))
        assertTrue(Validator.isUrlValid("http://www.example.com?foo=BaR"))
        assertTrue(Validator.isUrlValid("http://www.example.com#fooBaR"))
        assertTrue(Validator.isUrlValid("http://www.example.com"))
        assertTrue(Validator.isUrlValid("http://www.example.com:8080"))
        assertTrue(Validator.isUrlValid("http://www.example.com:8080/foo/bar"))
        assertTrue(Validator.isUrlValid("http://www.example.com/#"))
        assertTrue(Validator.isUrlValid("http://www.example.com/?"))
        assertTrue(Validator.isUrlValid("http://www.example2.com"))
        assertTrue(Validator.isUrlValid("http://www.sho.com/site/dexter/home.sho"))
        assertTrue(Validator.isUrlValid("http://www.example.com/foo%20bar"))
        assertTrue(Validator.isUrlValid("http://example.com/foo/bar"))
        assertTrue(Validator.isUrlValid("https://example.com/foo/bar"))
        assertTrue(Validator.isUrlValid("http://عمان.icom.museum"))
        assertTrue(Validator.isUrlValid("http://www.example.com/foo/bar?a=b&c=d"))
        assertFalse(Validator.isUrlValid("http://"))
        assertFalse(Validator.isUrlValid("example.com"))
        assertFalse(Validator.isUrlValid("EXAMPLE.COM"))
        assertFalse(Validator.isUrlValid("www.example.com"))
        assertFalse(Validator.isUrlValid("WWW.EXAMPLE.COM"))
        assertFalse(Validator.isUrlValid("ftp://example.com"))
        assertFalse(Validator.isUrlValid("ftp://www.example.com"))
        assertFalse(Validator.isUrlValid("example.com/foo/bar"))
        assertFalse(Validator.isUrlValid("www.example.com/foo/bar"))
        assertFalse(Validator.isUrlValid("www.example.com:8080"))
        assertFalse(Validator.isUrlValid("www.example.com:8080/foo/bar"))
        assertFalse(Validator.isUrlValid("www.example.com/foo%20bar"))
        assertFalse(Validator.isUrlValid("example.com/foo/bar"))
        assertFalse(Validator.isUrlValid("www.example.com/foo/bar"))
        assertFalse(Validator.isUrlValid("www.example.com/foo/bar"))
        assertFalse(Validator.isUrlValid("www.example.com/foo/捦挺挎/bar"))
        assertFalse(Validator.isUrlValid("عمان.icom.museum"))
        assertFalse(Validator.isUrlValid("1964thetribute.com"))
        assertFalse(Validator.isUrlValid("http:/www.example.com"))
        assertFalse(Validator.isUrlValid("http:www.example.com"))
    }
}
