package org.odk.collect.android.helpers;

public class RegexTemplates {

    public static final String LOCATION =  "^-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\"
                                     + ".[0-9]+\\s[0-9]+\\.[0-9]+$";
    public static final String VALID_GOOGLE_SHEETS = "^[a-zA-Z0-9\\-]+$";
    public static final String SEARCH_FUNCTION = "search\\(.+\\)";
    public static final String FORM_NAME = "[^\\p{L}\\p{Digit}]";
    public static final String ROOT_NAME = "\\p{javaWhitespace}+";
    public static final String BARCODE_RESPONSE = "\\p{C}";
    public static final String EX_SPEC = "^ex[:]";
    public static final String SAFE_COLUMN_NAME = "[^A-Za-z0-9_]";
    public static final String STYLES = "style=[\"'](.*?)[\"']";

    public static final String HTML_SPAN = "(?s)<\\s?span([^\\/\n]*)>((?:(?!<\\/).)+)<\\/\\s?span\\s?>";
    public static final String HTML_STRONG_1 = "(?s)__(.*?)__";
    public static final String HTML_STRONG_2 = "(?s)\\*\\*(.*?)\\*\\*";
    public static final String HTML_EMPHASIS_1 = "(?s)_([^\\s][^_\n]*)_";
    public static final String HTML_EMPHASIS_2 = "(?s)\\*([^\\s][^\\*\n]*)\\*";
    public static final String HTML_LINKS = "(?s)\\[([^\\]]*)\\]\\(([^\\)]+)\\)";
    public static final String HTML_HEADERS = "(?s)^(#+)([^\n]*)$";
    public static final String HTML_PARAGRAPHS = "(?s)([^\n]+)\n";

}
