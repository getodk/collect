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

import java.util.regex.MatchResult;

public class TextUtils {

    private static ReplaceCallback.Callback createHeader = new ReplaceCallback.Callback() {
        public String matchFound(MatchResult match) {
            int level = match.group(1).length();
            return "<h" + level + ">" + match.group(2).replaceAll("#+$", "").trim() + "</h" + level
                    + ">";
        }
    };

    private static ReplaceCallback.Callback createParagraph = new ReplaceCallback.Callback() {
        public String matchFound(MatchResult match) {
            String trimmed = match.group(1).trim();
            if (trimmed.matches("(?i)^<\\/?(h|p|bl)")) {
                return match.group(1);
            }
            return "<p>" + trimmed + "</p>";
        }
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

    private TextUtils() {

    }

    public static String markdownToHtml(String text) {

        text = text.replaceAll("<([^a-zA-Z/])", "&gm;$1");
        // https://github.com/enketo/enketo-transformer/blob/master/src/markdown.js

        // span - replaced &lt; and &gt; with <>
        text = ReplaceCallback.replace("/&lt;\\s?span([^/\\n]*)&gt;((?:(?!&lt;\\/).)+)&lt;\\/\\s?span\\s?&gt;/gm _createSpan >",
                text, createSpan);
        // strong
        text = text.replaceAll("/__(.*?)__/gm", "<strong>$1</strong>");
        text = text.replaceAll("/\\*\\*(.*?)\\*\\*/gm", "<strong>$1</strong>");
        // emphasis
        text = text.replaceAll("/_([^\\s][^_\\n]*)_/gm", "<em>$1</em>");
        text = text.replaceAll("/\\*([^\\s][^*\\n]*)\\*/gm", "<em>$1</em>");
        // links
        text = text.replaceAll("/\\[([^\\]]*)\\]\\(([^)]+)\\)/gm",
                "<a href=\"$2\" target=\"_blank\">$1</a>");
        // headers - requires ^ or breaks <font color="#f58a1f">color</font>
        text = ReplaceCallback.replace("(?s)^(#+)([^\n]*)$", text, createHeader);
        // paragraphs
        text = ReplaceCallback.replace("(?s)([^\n]+)\n", text, createParagraph);

        // "\" will be used as escape character for *, _
        text = text.replaceAll(" /&/gm", "'&amp;'");
        text = text.replaceAll("/\\\\/gm", "'&92;'");
        text = text.replaceAll(" /\\\\*/gm", "'&42;'");
        text = text.replaceAll(" /\\_/gm", "'&95;'");
        text = text.replaceAll(" /\\#/gm", "'&35;'");
        return text;
    }

    public static CharSequence textToHtml(String text) {

        if (text == null) {
            return null;
        }

        return Html.fromHtml(markdownToHtml(text));
    }
} 
