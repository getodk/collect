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

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.regex.MatchResult;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import timber.log.Timber;

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

            for (int i = 0; i < styles.length; i++) {
                String[] stylesAttributes = styles[i].trim().split(":");
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

    private static String markdownToHtml(String text) {

        // https://github.com/enketo/enketo-transformer/blob/master/src/markdown.js

        // span - replaced &lt; and &gt; with <>
        text = ReplaceCallback.replace("(?s)<\\s?span([^\\/\n]*)>((?:(?!<\\/).)+)<\\/\\s?span\\s?>",
                text, createSpan);
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

        return text;
    }

    public static CharSequence textToHtml(String text) {

        if (text == null) {
            return null;
        }

        return Html.fromHtml(markdownToHtml(text));
    }

    public static String compress(String data) throws IOException {
        if (data == null || data.length() == 0) {
            return data;
        }

        // Encode string into bytes
        byte[] input = data.getBytes("UTF-8");

        // Compress the bytes
        byte[] output = new byte[input.length];
        Deflater compresser = new Deflater();
        compresser.setInput(input);
        compresser.finish();
        int compressedDataLength = compresser.deflate(output);
        compresser.end();

        // Encode to base64
        String base64String = Base64.encodeBase64String(output);
        Timber.i("Original length : %d", data.length());
        Timber.i("Compressed length : %d", compressedDataLength);
        Timber.i("Compression ratio : %2f", ((data.length() * 1.0) / compressedDataLength) * 100);
        return base64String;
    }

    public static String decompress(String compressedString) throws IOException, DataFormatException {

        // Decode from base64
        byte[] output = Base64.decodeBase64(compressedString);

        // Decompresses the bytes
        Inflater decompresser = new Inflater();
        decompresser.setInput(output);
        byte[] result = compressedString.getBytes();
        int resultLength = decompresser.inflate(result);
        decompresser.end();

        // Decode the bytes into a String
        String outputString = new String(result, 0, resultLength, "UTF-8");
        Timber.i("Compressed length : %d", compressedString.length());
        Timber.i("Decompressed length : %d", resultLength);
        return outputString;
    }
} 
