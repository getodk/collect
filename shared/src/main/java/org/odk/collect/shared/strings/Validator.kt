/*
 * Copyright 2016 Nafundi
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
package org.odk.collect.shared.strings

import java.util.regex.Pattern

object Validator {
    /*
    There are lots of ways to validate email addresses and it's hard to find one perfect.
    That's why we use here a very simple approach just to confirm that passed string contains:
        *any number of characters before @ (at least one)
        *one @ char
        *any number of characters after @ (at least one)
     */
    @JvmStatic
    fun isEmailAddressValid(emailAddress: String): Boolean {
        return Pattern
            .compile(".+@.+")
            .matcher(emailAddress)
            .matches()
    }

    @JvmStatic
    fun isUrlValid(url: String): Boolean {
        return Pattern
            .compile("^https?://.+$", Pattern.CASE_INSENSITIVE)
            .matcher(url)
            .matches()
    }
}
