/*
 * Copyright (C) 2016 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.utilities;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// http://stackoverflow.com/a/2563382/152938

public class ReplaceCallback {

    private ReplaceCallback() {
    }

    /**
     * Replaces with callback, with no limit to the number of replacements.
     * Probably what you want most of the time.
     */
    public static String replace(String pattern, String subject, Callback callback) {
        return replace(pattern, subject, -1, null, callback);
    }

    public static String replace(String pattern, String subject, int limit, Callback callback) {
        return replace(pattern, subject, limit, null, callback);
    }

    /**
     * @param regex    The regular expression pattern to search on.
     * @param subject  The string to be replaced.
     * @param limit    The maximum number of replacements to make. A negative value
     *                 indicates replace all.
     * @param count    If this is not null, it will be set to the number of
     *                 replacements made.
     * @param callback Callback function
     */
    public static String replace(String regex, String subject, int limit,
                                 AtomicInteger count, Callback callback) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = Pattern.compile(regex).matcher(subject);
        int i;
        for (i = 0; (limit < 0 || i < limit) && matcher.find(); i++) {
            String replacement = callback.matchFound(matcher.toMatchResult());
            replacement = Matcher.quoteReplacement(replacement); //probably what you want...
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        if (count != null) {
            count.set(i);
        }
        return sb.toString();
    }

    public interface Callback {
        /**
         * This function is called when a match is made. The string which was matched
         * can be obtained via match.group(), and the individual groupings via
         * match.group(n).
         */
        String matchFound(MatchResult match);
    }
}
