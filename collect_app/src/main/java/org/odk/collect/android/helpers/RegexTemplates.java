package org.odk.collect.android.helpers;

public class RegexTemplates {

    public static String LOCATION =  "^-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\"
                                     + ".[0-9]+\\s[0-9]+\\.[0-9]+$";
    public static String VALID_GOOGLE_SHEETS = "^[a-zA-Z0-9\\-]+$";
    public static String SEARCH_FUNCTION = "search\\(.+\\)";
    public static String FORM_NAME = "[^\\p{L}\\p{Digit}]";
    public static String ROOT_NAME = "\\p{javaWhitespace}+";
    public static String BARCODE_RESPONSE = "\\p{C}";
    public static String EX_SPEC = "^ex[:]";
    public static String SAFE_COLUMN_NAME = "[^A-Za-z0-9_]";
    public static String STYLES = "style=[\"'](.*?)[\"']";

    public static String HTML_SPAN = "(?s)<\\s?span([^\\/\n]*)>((?:(?!<\\/).)+)<\\/\\s?span\\s?>";
    public static String HTML_STRONG_1 = "(?s)__(.*?)__";
    public static String HTML_STRONG_2 = "(?s)\\*\\*(.*?)\\*\\*";
    public static String HTML_EMPHASIS_1 = "(?s)_([^\\s][^_\n]*)_";
    public static String HTML_EMPHASIS_2 = "(?s)\\*([^\\s][^\\*\n]*)\\*";
    public static String HTML_LINKS = "(?s)\\[([^\\]]*)\\]\\(([^\\)]+)\\)";
    public static String HTML_HEADERS = "(?s)^(#+)([^\n]*)$";
    public static String HTML_PARAGRAPHS = "(?s)([^\n]+)\n";

}
