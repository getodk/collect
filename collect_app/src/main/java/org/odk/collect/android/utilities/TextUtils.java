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

        // Regular expressions match https://github.com/enketo/enketo-transformer/blob/master/src/markdown.js
        html = html.replaceAll("__(.*?)__", "<strong>$1</strong>");
        html = html.replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");
        html = html.replaceAll("_([^\\s][^_\\n]*)_", "<em>$1</em>");
        html = html.replaceAll( "\\*([^\\s][^\\*\\n]*)\\*", "<em>$1</em>");
        html = html.replaceAll("\\[([^\\]]*)\\]\\(([^\\)]+)\\)", "<a href=\"$2\">$1</a>");

        StringBuffer headerOutput = new StringBuffer();
        Matcher headerMatcher = Pattern.compile("(#+)([^\\n]*)\\n").matcher(html);
        while (headerMatcher.find()) {
            headerMatcher.appendReplacement(headerOutput, createHeaderReplacement(headerMatcher));
        }
        html = headerMatcher.appendTail(headerOutput).toString();

        StringBuffer paragraphOutput = new StringBuffer();
        Matcher paragraphMatcher = Pattern.compile("([^\\n]+)\\n").matcher(html);
        while (paragraphMatcher.find()) {
            paragraphMatcher.appendReplacement(paragraphOutput, createParagraphReplacement(paragraphMatcher));
        }
        html =  paragraphMatcher.appendTail(paragraphOutput).toString();

        StringBuffer spanOutput = new StringBuffer();
        Matcher spanMatcher = Pattern.compile("(?i)&lt;\\s?span(.*)&gt;(.+)&lt;\\/\\s?span\\s?&gt;").matcher(html);
        while (spanMatcher.find()) {
            spanMatcher.appendReplacement(spanOutput, createSpanReplacement(spanMatcher));
        }
        html =  spanMatcher.appendTail(spanOutput).toString();

        return html;
    }

    public static String createHeaderReplacement(Matcher matcher) {

        int level = matcher.group(1).length();
        return "<h" + level + ">" + matcher.group(2).replaceAll("#+$", "").trim() + "</h" + level + ">\n";
    }

    public static String createParagraphReplacement(Matcher matcher) {

        String line = matcher.group(1);
        String trimmed = line.trim();
        if (trimmed.matches("(?i)^<\\/?(ul|ol|li|h|p|bl)")) {
            return line;
        }
        return "<p>" + trimmed + "</p>";
    }

    public static String createSpanReplacement(Matcher matcher) {

        String attributes = matcher.group(1);
        attributes = attributes.replaceAll("(?i)style\\s?=\\s?[\"'](.*)\\s?[\"']", "$1");
        attributes = attributes.replaceAll("(?i)([a-z]+)\\s?:\\s?([a-z0-9]+)\\s?;?", "$1=\"$2\"");
        return "<font" + attributes + ">" + matcher.group(2).trim() + "</font>";
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
