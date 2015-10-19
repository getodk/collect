/*
 * Copyright (C) 2015 University of Washington
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

import android.text.Html;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TextUtils {
    private static final String t = "TextUtils";

    private TextUtils() {
        // static methods only
    }

    private static String markdownToHtml(String html) {

        // Regular expressions should match https://github.com/enketo/enketo-transformer/blob/master/src/markdown.js
        String strongRegex1 = "__(.*?)__";
        String strongRegex2 = "\\*\\*(.*?)\\*\\*";
        String strongReplacement = "<strong>$1</strong>";

        String emphasisRegex1 = "_([^\\s][^_\\n]*)_";
        String emphasisRegex2 = "\\*([^\\s][^\\*\\n]*)\\*";
        String emphasisReplacement = "<em>$1</em>";

        String linkRegex = "\\[([^\\]]*)\\]\\(([^\\)]+)\\)";
        String linkReplacement = "<a href=\"$2\">$1</a>";

        html = html.replaceAll(strongRegex1, strongReplacement);
        html = html.replaceAll(strongRegex2, strongReplacement);
        html = html.replaceAll(emphasisRegex1, emphasisReplacement);
        html = html.replaceAll(emphasisRegex2, emphasisReplacement);
        html = html.replaceAll(linkRegex, linkReplacement);

        String headerRegex = "(#+)([^\\n]*)\\n";
        StringBuffer headerOutput = new StringBuffer();
        Pattern headerPattern = Pattern.compile(headerRegex);
        Matcher headerMatcher = headerPattern.matcher(html);
        while (headerMatcher.find()) {
            headerMatcher.appendReplacement(headerOutput, createHeaderReplacement(headerMatcher));
        }
        headerMatcher.appendTail(headerOutput);
        html = headerOutput.toString();

        return html;
    }

    public static String createHeaderReplacement(Matcher matcher) {
        int level = matcher.group(1).length();
        return "<h" + level + ">" + matcher.group(2).replaceAll("#+$", "").trim() + "</h" + level + ">\n";
    }

    public static CharSequence textToHtml(String text) {

        // There's some terrible bug that displays all the text as the
        // opening tag if a tag is the first thing in the string
        // so we hack around it so it begins with something else
        // when we convert it

        // terrible hack, just add some chars
        Spanned brokenHtml = Html.fromHtml("x" + markdownToHtml(text));
        // after we have the good html, remove the chars
        CharSequence fixedHtml = brokenHtml.subSequence(1, brokenHtml.length());

        return fixedHtml;
    }

} 
