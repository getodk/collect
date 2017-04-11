package org.odk.collect.android.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by simran on 4/8/2017.
 */

public class Regex {
    public static final String GPS_LOCATION= "^-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\"
            + ".[0-9]+\\s[0-9]+\\.[0-9]+$";
    public static final String VALID_GOOGLE_SHEETS_ID = "^[a-zA-Z0-9\\-]+$";
    private static Pattern valid_google_sheets_id, gps_location;

    /**
     * Google sheets currently only allows a-zA-Z0-9 and dash
     */
    // Check to see if answer is a location, if so, get rid of accuracy
    // and altitude
    // try to match a fairly specific pattern to determine
    // if it's a location
    // [-]#.# [-]#.# #.# #.#


    static {
        valid_google_sheets_id = Pattern.compile(VALID_GOOGLE_SHEETS_ID);
        gps_location = Pattern.compile(GPS_LOCATION);
    }


    public static boolean isValidGoogleSheetsString(String name) {

        Matcher m = valid_google_sheets_id.matcher(name);
        return m.matches();
    }

    public static boolean isValidLocation(String answer) {
        java.util.regex.Matcher m = gps_location.matcher(answer);
        return m.matches();
    }
}

