package org.odk.collect.android.database;

public class SqlFragParam {
    private String type;			// text || sql || integer || double

    public String sValue;		// text || sql
    public int iValue;			// integer
    public double dValue;		// double

    public SqlFragParam() {

    }

    public SqlFragParam(String t) {
        type = t;
    }

    void addTextParam(String v) {
        type = "text";
        sValue = v;
    }

    public void setType(String v) {
        type = v;
    }

    public String getType() {
        String t = type;

        if(t.equals("text")) {
            if(isDate(sValue)) {
                t = "date";
            }
        }
        return t;
    }

    String debug() {
        if(type.equals("text") || type.equals("sql")) {
            return type + " : " + sValue;
        } else {
            return "";
        }
    }

    boolean isDate(String t) {
        if (t.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return true;
        } else {
            return false;
        }
    }
}
