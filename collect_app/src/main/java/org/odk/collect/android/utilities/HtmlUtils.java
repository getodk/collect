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

import static org.odk.collect.shared.strings.StringUtils.trim;

import android.text.Html;

import java.util.regex.MatchResult;

public final class HtmlUtils {

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

    private HtmlUtils() {

    }

    static String markdownToHtml(String text) {

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
}

