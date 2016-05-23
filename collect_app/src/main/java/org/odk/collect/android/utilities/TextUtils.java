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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TextUtils {
    private static final String t = "TextUtils";

    private TextUtils() {
        // static methods only
    }

    private static String markdownToHtml(String html) {

        // https://gist.github.com/jbroadway/2836900
        // we try to be as strict as possible
        html = html.replaceAll("(\\*\\*|__)(.*?)(\\*\\*|__)", "<strong>$2</strong>");
        html = html.replaceAll("(\\*|_)(.*?)(\\*|_)", "<em>$2</em>");
        html = html.replaceAll("\\[([^\\[]+)\\]\\(([^\\)]+)\\)", "<a href=\"$2\">$1</a>");

        StringBuffer headerOutput = new StringBuffer();
        Matcher headerMatcher = Pattern.compile("(?m)^(#+)(.*)").matcher(html);
        while (headerMatcher.find()) {
            headerMatcher.appendReplacement(headerOutput, headerMatcher.quoteReplacement(createHeaderReplacement(headerMatcher)));
        }
        html = headerMatcher.appendTail(headerOutput).toString();

        StringBuffer paragraphOutput = new StringBuffer();
        Matcher paragraphMatcher = Pattern.compile("\\n([^\\n]+)\\n").matcher(html);
        while (paragraphMatcher.find()) {
            paragraphMatcher.appendReplacement(paragraphOutput, headerMatcher.quoteReplacement(createParagraphReplacement(paragraphMatcher)));
        }
        html = paragraphMatcher.appendTail(paragraphOutput).toString();

        StringBuffer spanOutput = new StringBuffer();
        Matcher spanMatcher = Pattern.compile("((&lt;)|<)span(.*?)((&gt;)|>)(.*?)((&lt;)|<)/span((&gt;)|>)").matcher(html);
        while (spanMatcher.find()) {
            spanMatcher.appendReplacement(spanOutput, headerMatcher.quoteReplacement(createSpanReplacement(spanMatcher)));
        }
        html = spanMatcher.appendTail(spanOutput).toString();

        return html;
    }

    public static String createHeaderReplacement(Matcher matcher) {

        int level = matcher.group(1).length();
        return "<h" + level + ">" + matcher.group(2).trim() + "</h" + level + ">\n";
    }

    public static String createParagraphReplacement(Matcher matcher) {

        String line = matcher.group(1);
        String trimmed = line.trim();
        if (trimmed.matches("^<\\/?(h|p)")) {
            return "\n" + line + "\n";
        }
        return "\n<p>" + trimmed + "</p>\n";
    }

    public static String createSpanReplacement(Matcher matcher) {

        String stylesText = matcher.group(3);
        stylesText = stylesText.replaceAll("style=[\"'](.*?)[\"']", "$1");

        String[] styles = stylesText.trim().split(";");
        StringBuffer stylesOutput = new StringBuffer();

        for (int i = 0; i < styles.length; i++) {
            String[] stylesAttributes = styles[i].trim().split(":");
            if (stylesAttributes[0].equals("color")) {
                stylesOutput.append(" color=\"" + stylesAttributes[1] + "\"");
            }
            if (stylesAttributes[0].equals("font-family")) {
                stylesOutput.append(" face=\"" + stylesAttributes[1] + "\"");
            }
        }

        return "<font" + stylesOutput + ">" + matcher.group(6).trim() + "</font>";
    }

    // http://stackoverflow.com/a/10187511/152938
    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i+1);
    }

    public static CharSequence textToHtml(String text) {

        if ( text == null ) {
            return null;
        }

        return trimTrailingWhitespace(Html.fromHtml(markdownToHtml(text)));
    }

} 
