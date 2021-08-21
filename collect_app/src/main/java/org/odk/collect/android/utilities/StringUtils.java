/*
 * Copyright (C) 2017 University of Washington
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

import androidx.annotation.NonNull;

import com.google.common.base.CharMatcher;

import java.util.Iterator;
import java.util.regex.MatchResult;

import static java.lang.Character.isWhitespace;

public class StringUtils {

    private static ReplaceCallback.Callback createHeader = match -> {
        int level = match.group(1).length();
        return "<h" + level + ">" + match.group(2).replaceAll("#+$", "").trim() + "</h" + level
                + ">";
    };

    private static ReplaceCallback.Callback createParagraph = match -> {
        String trimmed = match.group(1).trim();
        if (trimmed.matches("(?i)^<\\/?(h|p|bl)")) {
            return match.group(1);
        }
        return "<p>" + trimmed + "</p>";
    };

    private static ReplaceCallback.Callback createSpan = new ReplaceCallback.Callback() {
        public String matchFound(MatchResult match) {
            String attributes = sanitizeAttributes(match.group(1));
            return "<font" + attributes + ">" + match.group(2).trim() + "</font>";
        }

        // throw away all styles except for color and font-family
        private String sanitizeAttributes(String attributes) {

            String stylesText = attributes.replaceAll("style=[\"'](.*?)[\"']", "$1");
            String[] styles = stylesText.trim().split(";");
            StringBuffer stylesOutput = new StringBuffer();

            for (String style : styles) {
                String[] stylesAttributes = style.trim().split(":");
                if (stylesAttributes[0].equals("color")) {
                    stylesOutput.append(" color=\"" + stylesAttributes[1] + "\"");
                }
                if (stylesAttributes[0].equals("font-family")) {
                    stylesOutput.append(" face=\"" + stylesAttributes[1] + "\"");
                }
            }

            return stylesOutput.toString();
        }
    };

    private StringUtils() {

    }

    protected static String markdownToHtml(String text) {

        text = text.replaceAll("<([^a-zA-Z/])", "&lt;$1");
        // https://github.com/enketo/enketo-transformer/blob/master/src/markdown.js

        // span - replaced &lt; and &gt; with <>
        text = ReplaceCallback.replace("(?s)<\\s?span([^\\/\n]*)>((?:(?!<\\/).)+)<\\/\\s?span\\s?>",
                text, createSpan);

        //intermediary replacements keys for special characters, N/B: These symbols are not meant to be interpreted as markdown
        text = text.replaceAll("(?s)\\\\#", "&#35;");
        text = text.replaceAll("(?s)\\\\\\\\", "&#92;");
        text = text.replaceAll("(?s)\\\\_", "&#95;");
        text = text.replaceAll("(?s)\\\\\\*", "&#42;");

        // strong
        text = text.replaceAll("(?s)__(.*?)__", "<strong>$1</strong>");
        text = text.replaceAll("(?s)\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");

        // emphasis
        text = text.replaceAll("(?s)_([^\\s][^_\n]*)_", "<em>$1</em>");
        text = text.replaceAll("(?s)\\*([^\\s][^\\*\n]*)\\*", "<em>$1</em>");

        // links
        text = text.replaceAll("(?s)\\[([^\\]]*)\\]\\(([^\\)]+)\\)",
                "<a href=\"$2\" target=\"_blank\">$1</a>");
        // headers - requires ^ or breaks <font color="#f58a1f">color</font>
        text = ReplaceCallback.replace("(?s)^(#+)([^\n]*)$", text, createHeader);
        // paragraphs
        text = ReplaceCallback.replace("(?s)([^\n]+)\n", text, createParagraph);

        // replacing intermediary keys with the proper markdown symbols
        text = text.replaceAll("(?s)&#35;", "#");
        text = text.replaceAll("(?s)&#42;", "*");
        text = text.replaceAll("(?s)&#95;", "_");
        text = text.replaceAll("(?s)&#92;", "\\\\");
        return text;
    }

    public static CharSequence textToHtml(String text) {
        return text == null ? "" : trim(Html.fromHtml(markdownToHtml(text)));
    }

    public static String ellipsizeBeginning(String text) {
        return text.length() <= 100
                ? text
                : "..." + text.substring(text.length() - 97, text.length());
    }

    /**
     * Copyright (C) 2006 The Android Open Source Project
     *
     * Copied from Android project for testing.
     * TODO: replace with String.join when minSdk goes to 26
     *
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param delimiter a CharSequence that will be inserted between the tokens. If null, the string
     *     "null" will be used as the delimiter.
     * @param tokens an array objects to be joined. Strings will be formed from the objects by
     *     calling object.toString(). If tokens is null, a NullPointerException will be thrown. If
     *     tokens is empty, an empty string will be returned.
     */
    public static String join(@NonNull CharSequence delimiter, @NonNull Iterable tokens) {
        final Iterator<?> it = tokens.iterator();
        if (!it.hasNext()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(delimiter);
            sb.append(it.next());
        }
        return sb.toString();
    }

    public static boolean isBlank(String string) {
        char[] chars = string.toCharArray();

        for (char character : chars) {
            if (!isWhitespace(character)) {
                return false;
            }
        }

        return true;
    }

    public static CharSequence trim(CharSequence text) {
        if (text == null || text.length() == 0) {
            return text;
        }

        int len = text.length();
        int start = 0;
        int end = len - 1;
        while (start < len && Character.isWhitespace(text.charAt(start))) {
            start++;
        }
        while (Character.isWhitespace(text.charAt(end)) && end > 0) {
            end--;
        }
        if (end >= start) {
            return text.subSequence(start, end + 1);
        }
        return text;
    }

    public static String removeEnd(String str, String remove) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        if (str.endsWith(remove)) {
            return str.substring(0, str.length() - remove.length());
        }

        return str;
    }

    public static String getFilenameError(String filename) {
        String possiblyRestricted = "?:\"*|/\\<>\u0000";
        boolean containsAt = filename.contains("@");
        boolean containsNonAscii = CharMatcher.ascii().matchesAllOf(filename);
        boolean containsPossiblyRestricted = CharMatcher.anyOf(possiblyRestricted).matchesAnyOf(possiblyRestricted);
        return "Problem with project name file. Contains @: " + containsAt + ", Contains non-ascii: " + containsNonAscii + ", Contains restricted: " + containsPossiblyRestricted;
    }
}

