package org.odk.collect.android.database;

import org.odk.collect.android.external.ExternalDataUtil;

import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/*
 * If the param type is sql then this is an intermediate stage where the parameter still needs to be tokenized
 */

public class SqlFrag {
    public ArrayList<String> conditions = null;	// The original conditions used to create it

    public StringBuilder sql = new StringBuilder("");
    public ArrayList<SqlFragParam> params = new ArrayList<SqlFragParam> ();
    public ArrayList<String> columns = new ArrayList<String> ();
    public ArrayList<String> humanNames = new ArrayList<String> ();

    private static Logger log = Logger.getLogger(SqlFrag.class.getName());

    public void add(String in) {
        if(sql.length() > 0) {
            sql.append(" ");
        }
        sql.append(in);
    }

    public void addText(String in) {

        // Escape any quotes
        in = in.replaceAll("\'", "\'\'");
        if(sql.length() > 0) {
            sql.append(" ");
        }
        sql.append("'");
        sql.append(in);
        sql.append("'");
    }

    /*
     * Add an SQL expression
     */
    public void addSqlFragment(String in, boolean isCondition,
                               ResourceBundle localisationx,
                               int rowNum				// Set non zero when processing a form definition in a spreadsheet
    ) throws Exception {

        ArrayList<SqlFragParam> tempParams = new ArrayList<SqlFragParam> ();

        /*
         * If this SQL fragment is part of a condition then save it so that it can be exported back to XLS or edited online
         */
        if(isCondition) {
            if(conditions == null) {
                conditions = new ArrayList<String> ();
            }
            conditions.add(in);
        }
        String charTokens = "=+-><*/()";
        in = addSurroundingWhiteSpace(in, charTokens.toCharArray());
        in = addSurroundingWhiteSpace(in, new String[] {"<=", ">=", "!="});

        /*
         * This SQL Fragment may actually be text without quotes
         * If so then wrap in single quotes
         */
        in = escapeDoubledQuotesInText(in);	// Escape double single quotes first
        in = checkForText(in);

        /*
         * Get the text parameters and the sql fragments
         * Text parameters can include spaces, use single quotes to locate them
         */
        int idx1 = -1,
                idx2 = -1,
                addedChars = 0,
                start = 0;
        idx1 = in.indexOf('\'');
        while(idx1 > -1) {

            // Add the sql fragment
            if(idx1 > 0) {
                SqlFragParam p = new SqlFragParam();
                p.setType("sql");
                p.sValue = in.substring(start, idx1);
                tempParams.add(p);
                addedChars = idx1;
            }

            // Add the text fragment
            idx2 = in.indexOf('\'', idx1 + 1);
            if(idx2 > -1) {
                SqlFragParam p = new SqlFragParam();
                p.setType("text");
                /*
                 * If this quote encloses a timestamp value then include the case in the parameter value
                 */
                if(idx2 < in.length() - 1) {
                    String tsCast = "::timestamptz";
                    if(in.substring(idx2 + 1).startsWith(tsCast)) {
                        idx2 += tsCast.length() + 1;
                    }
                }
                p.sValue = in.substring(idx1 + 1, idx2);	// Remove quotation marks
                tempParams.add(p);
                addedChars = idx2 + 1;							// Skip over quote
            } else {
                throw new Exception("sqlfrag: Failed to get text fragment");   // modified for fieldtask
            }

            start = idx2 + 1;
            idx1 = in.indexOf('\'', idx2 + 1);
        }
        if(addedChars < in.length()) {
            SqlFragParam p = new SqlFragParam();
            p.setType("sql");
            p.sValue = in.substring(addedChars);
            tempParams.add(p);
        }

        /*
         * Tokenize the remainder of the SQL
         * These can be split using white space
         */
        for(int i = 0; i < tempParams.size(); i++) {
            SqlFragParam p = tempParams.get(i);
            if(p.getType().equals("sql")) {
                String [] token = p.sValue.split("[\\s]");  // Split on white space
                for(int j = 0; j < token.length; j++) {
                    String s = sqlToken(token[j], null, rowNum);    // localisation nulled for fieldtask

                    if(s.length() > 0) {
                        sql.append(" " + s + " ");
                    }
                }
            } else if(p.getType().equals("text") || p.getType().equals("date")) {
                SqlFragParam px = new SqlFragParam();
                // UnEscape double single quotes
                // Note using double single quotes is no longer required so convert to single quote
                p.sValue = p.sValue.replace("#####xx#####", "'");
                px.addTextParam(p.sValue);
                params.add(px);
                sql.append(" ? ");
            }
        }

        sql = convertToGeography(sql, "st_area");
        sql = convertToGeography(sql, "st_length");
        sql = convertToGeography(sql, "st_perimeter");
    }

    /*
     * convert st_area and st_length functions into using geography
     */
    StringBuilder convertToGeography(StringBuilder sql, String fn) {
        StringBuilder out = null;
        String in = sql.toString();
        int idx = in.indexOf(fn);
        if(idx < 0) {
            out = new StringBuilder(in);
        } else {
            out = new StringBuilder("");
            Pattern pattern = Pattern.compile(fn + " *\\(.+?\\)");
            java.util.regex.Matcher matcher = pattern.matcher(in);
            int start = 0;
            while (matcher.find()) {

                String matched = matcher.group();
                int idx2 = matched.indexOf("(");
                String qname = matched.substring(idx2 + 1, matched.length() - 1).trim();

                // Add any text before the match
                int startOfGroup = matcher.start();
                out.append(in.substring(start, startOfGroup));
                out.append(fn)
                        .append("(geography(")
                        .append(qname)
                        .append("), true) ");


                // Reset the start
                start = matcher.end();
            }
            // Get the remainder of the string
            if(start < in.length()) {
                out.append(in.substring(start));
            }
        }

        return out;
    }

    /*
     * Process a single sql token
     */
    public String sqlToken(String token, ResourceBundle localisation, int rowNum) throws Exception {
        String out = "";

        token = token.trim();

        // Check for a column name
        if((token.startsWith("${") || token.startsWith("#{")) && token.endsWith("}")) {  // fieldTask version add #{ for csv files
            String name = token.substring(2, token.length() - 1);
            boolean columnNameCaptured = false;
            // out = GeneralUtilityMethods.cleanName(name, true, true, false);		// do not use the clean name function in fieldTask - review
            out = ExternalDataUtil.toSafeColumnName(name);                          // Instead use toSafeColumnName
            for(int i = 0; i < columns.size(); i++) {
                if(columns.get(i).equals(out)) {
                    columnNameCaptured = true;
                    break;
                }
            }
            if(!columnNameCaptured) {
                columns.add(out);          // fieldTask version column names also use un cleaned name
                humanNames.add(name);
            }
        } else if (token.equals(">") ||
                token.equals("<") ||
                token.equals("<=") ||
                token.equals(">=") ||
                token.equals("=") ||
                token.equals("!=") ||
                token.equals("-") ||
                token.equals("+") ||
                token.equals("*") ||
                token.equals("/") ||
                token.equals(")") ||
                token.equals("(") ||
                token.equals("or") ||
                //token.equals(SmapServerMeta.UPLOAD_TIME_NAME) ||   // not applicable in fieldTask
                //token.equals(SmapServerMeta.SCHEDULED_START_NAME) ||  // not applicable in fieldTask
                token.equals("and") ||
                token.equals("is") ||
                token.equals("null") ||
                token.equals("not") ||
                token.equals("to_timestamp") ||
                token.equals("::timestamptz") ||
                token.equals("::timestamp") ||
                token.equals("to_date") ||
                token.equals("::date") ||
                token.equals("::integer") ||
                token.equals("like") ||
                token.equals("cast") ||
                token.equals("as") ||
                token.equals("integer") ||
                token.equals("current_date") ||
                token.equals("now()")) {
            out = token;
        } else if (token.equals("area")) {
            out = "st_area";
        } else if (token.equals("distance")) {
            out = "st_length";
        } else if (token.equals("perimeter")) {
            out = "st_perimeter";
        } else if (token.equals("empty")) {
            out = "is null";
        } else if (token.equals("all")) {
            out = "";
        } else if (token.equals("decimal") || token.equals("double")) {
            out = "double precision";
        } else if (token.startsWith("{") && token.endsWith("}")) {	// Preserve {xx} syntax if xx is integer
            out = "";
            String content = token.substring(1, token.length() - 1);

            if(content != null) {
                String [] contentArray = content.split("_");
                String [] contentArray2 = content.split(":");
                if(contentArray.length == 1 && contentArray2.length == 1) {
                    // simple integer assumed to be days
                    try {
                        Integer iValue = Integer.parseInt(contentArray[0]);
                        out = "'" + iValue.toString() + "'";
                    } catch (Exception e) {
                        log.log(Level.SEVERE,"Error", e);
                    }
                } else if(contentArray.length == 2) {
                    // 2 elements first of which must be an integer
                    try {
                        Integer iValue = Integer.parseInt(contentArray[0]);

                        out = "interval '" + iValue.toString() + " ";
                        if(contentArray[1].equals("day")) {
                            out += contentArray[1] + "'";
                        } else if(contentArray[1].equals("days")) {
                            out += contentArray[1] + "'";
                        } else if(contentArray[1].equals("hour")) {
                            out += contentArray[1] + "'";
                        } else if(contentArray[1].equals("hours")) {
                            out += contentArray[1] + "'";
                        } else if(contentArray[1].equals("minute")) {
                            out += contentArray[1] + "'";
                        } else if(contentArray[1].equals("minutes")) {
                            out += contentArray[1] + "'";
                        } else if(contentArray[1].equals("second")) {
                            out += contentArray[1] + "'";
                        } else if(contentArray[1].equals("seconds")) {
                            out += contentArray[1] + "'";
                        } else {
                            out = "";
                        }
                    } catch (Exception e) {
                        log.log(Level.SEVERE,"Error", e);
                    }
                } else if(contentArray2.length == 3) {
                    try {   // Validate content array
                        Integer.parseInt(contentArray2[0]);
                        Integer.parseInt(contentArray2[1]);
                        Integer.parseInt(contentArray2[2]);
                        out = " interval '" + content +"'";		// all looks good
                    } catch (Exception e) {
                        log.log(Level.SEVERE,"Error", e);
                    }
                }
            }
        } else if (token.length() > 0) {
            // Non text parameter, accept decimal or integer
            try {
                if(token.indexOf('.') >= 0) {
                    Double dValue = Double.parseDouble(token);
                    out = dValue.toString();
                } else {
                    Integer iValue = Integer.parseInt(token);
                    out = iValue.toString();
                }
            } catch (Exception e) {
                throw new Exception("Error processing token: " + token);  // modified for fieldtask
            }

        }

        return out;
    }

    /*
     * This function is used as it has been allowed to represent text without quotes when setting a condition value
     * It may return false negatives so it is recommended that quotes always be used to identify text
     */
    private String checkForText(String in) {
        String out = null;
        boolean isText = true;
        if(in != null) {
            if(in.indexOf('\'') > -1) {
                isText = false; // Contains a text fragment
            } else if(in.contains("{")) {
                isText = false; // Contains a column name
            } else if(in.contains("(")) {
                isText = false; // Contains a function possibly
            } else if(in.contains("where") || in.contains("and")) {
                isText = false; // Contains some sql reserved words
            }
        }
        if(isText) {
            out = "'" + in + "'";
        } else {
            out = in;
        }
        return out;
    }
    public void debug() {
        System.out.println("======");
        System.out.println("sql     " + sql.toString());
        for(int i = 0; i < params.size(); i++) {
            System.out.println("   " + params.get(i).debug());
        }
    }

    /*
     * Create an error message that includes the row number if it is set
     */
    private String getErrorMsg(ResourceBundle localisation, String partB, int rowNum) {
        StringBuffer msg = new StringBuffer("");
        if(rowNum > 0) {
            String partA = localisation.getString("mf_sc");
            partA = partA.replace("%s1", String.valueOf(rowNum));
            msg.append(partA).append(". ");
        }
        msg.append(partB);
        return msg.toString();
    }

    private String escapeDoubledQuotesInText(String in) {

        StringBuilder out = new StringBuilder();
        boolean inQuote = false;
        boolean quoteFound = false;
        boolean doubleQuoteFound = false;
        for(int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if(c == '\'') {
                if(quoteFound) {
                    doubleQuoteFound = true;
                }
                quoteFound = true;
            } else {
                if(doubleQuoteFound) {
                    if(inQuote) {
                        out.append("#####xx#####");
                    } else {
                        out.append("''");
                    }
                } else if(quoteFound) {
                    out.append("'");
                    inQuote = !inQuote;
                }
                quoteFound = false;
                doubleQuoteFound = false;
                out.append(c);
            }
        }
        if(doubleQuoteFound) {
            out.append("''");
        } else if(quoteFound) {
            out.append("'");
        }

        return out.toString();
    }

    /*
     * Make sure there is white space around a character
     * Don't make a change if the character is within single quotes
     */
    private static String addSurroundingWhiteSpace(String in, char [] cArray) {
        StringBuffer out = new StringBuffer("");

        if(in != null) {
            int quoteCount = 0;

            for(int i = 0; i < in.length(); i++) {

                if(in.charAt(i) == '\'') {
                    quoteCount++;
                }

                boolean charInList = false;
                for(int j = 0; j < cArray.length; j++) {
                    if(in.charAt(i) == cArray[j]) {
                        if((i < in.length() - 1) && in.charAt(i+1) == '=' && (in.charAt(i) == '<' || in.charAt(i) == '>')) {
                            charInList = false;
                        } else if(i > 0 && in.charAt(i) == '=' && (in.charAt(i-1) == '<' || in.charAt(i-1) == '>')) {
                            charInList = false;
                        } else if(i > 0 && in.charAt(i) == '=' && in.charAt(i-1) == '!' ) {
                            charInList = false;
                        } else if(i < in.length() - 1 && in.charAt(i) == '(' && in.charAt(i+1) == ')') {
                            charInList = false;
                        } else if(i > 0 && in.charAt(i) == ')' && in.charAt(i-1) == '(' ) {
                            charInList = false;
                        } else {
                            charInList = true;
                        }
                        break;
                    }
                }
                if(charInList && quoteCount%2 == 0) {
                    if(i > 0 && in.charAt(i-1) != ' ') {
                        out.append(' ');
                    }
                    out.append(in.charAt(i));
                    if(i < in.length() - 1 && in.charAt(i+1) != ' ') {
                        out.append(' ');
                    }
                } else {
                    out.append(in.charAt(i));
                }
            }
        }

        return out.toString();
    }

    /*
     * Make sure there is white space around a String of characters
     * Don't make a change if the character is within single quotes
     */
    private static String addSurroundingWhiteSpace(String in, String [] token) {
        StringBuffer out = new StringBuffer("");

        if(in != null) {
            int quoteCount = 0;

            for(int i = 0; i < in.length(); i++) {

                if(in.charAt(i) == '\'') {
                    quoteCount++;
                }

                int tokenIndex = -1;
                for(int j = 0; j < token.length; j++) {
                    if(in.substring(i).startsWith(token[j])) {
                        tokenIndex = j;
                        break;
                    }
                }

                if(tokenIndex >= 0 && quoteCount%2 == 0) {
                    if(i > 0 && in.charAt(i-1) != ' ') {
                        out.append(' ');
                    }
                    out.append(token[tokenIndex]);
                    i += token[tokenIndex].length() - 1;		// i will be incremented again next time round the loop
                    if(i + 1 < in.length() && in.charAt(i+2) != ' ') {
                        out.append(' ');
                    }
                } else {
                    out.append(in.charAt(i));
                }
            }
        }

        return out.toString();
    }

    /*
     * Set the parameters for an array of sql fragments
     */
    public static String getParamValue( SqlFragParam p) {
        String value = null;
        if(p.getType().equals("text")) {
            return p.sValue;
        } else if(p.getType().equals("integer")) {
            return String.valueOf(p.iValue);
        } else if(p.getType().equals("double")) {
            return String.valueOf(p.dValue);
        } else if(p.getType().equals("date")) {
            return p.sValue;
        } else {
            return p.sValue;
        }
    }
}
