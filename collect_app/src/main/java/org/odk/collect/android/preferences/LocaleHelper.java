package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

/**
 * Changes the locale of the app and keeps the changes persistent
 *
 * @author abdulwd
 */

public class LocaleHelper {

    public void updateLocale(Context context) {
        String localeCode = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PreferenceKeys.KEY_LANGUAGE, "en");
        Locale locale;
        if (!localeCode.contains("_")) {
            locale = new Locale(localeCode);
        } else {
            String[] arg = localeCode.split("_");
            locale = new Locale(arg[0], arg[1]);
        }
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        context.getResources().updateConfiguration(configuration, displayMetrics);
        context.getApplicationContext().getResources().updateConfiguration(configuration, displayMetrics);
    }

    public String[][] getEntryListValues() {
        String[] languages = {"af", "am", "ar", "bn", "ca", "cs", "de", "en",
                "es", "es_SV", "et", "fa", "fi", "fr", "ha", "hi", "hi_IN", "hu", "in",
                "it", "ja", "ka", "km", "lo_LA", "lt", "mg", "my", "nb", "ne_NP", "nl",
                "no", "pl", "ps", "pt", "ro", "ru", "so", "sq", "sw", "sw_KE", "ta",
                "th_TH", "tl", "tl_PH", "tr", "uk", "ur", "ur_PK", "vi", "zh", "zu"};
        int length = languages.length;

        //Array to hold names and codes of languages
        String languageList[][] = new String[length][2];
        for (int i = 0; i < length; i++) {
            Locale locale;
            if (languages[i].contains("_")) {
                String arg[] = languages[i].split("_");
                locale = new Locale(arg[0], arg[1]);
            } else {
                locale = new Locale(languages[i]);
            }
            languageList[i][0] = languages[i];
            languageList[i][1] = locale.getDisplayName();
        }

        //Sort list of languages by display name
        Arrays.sort(languageList, new Comparator<String[]>() {
            @Override
            public int compare(String[] s1, String[] s2) {
                return s1[1].compareTo(s2[1]);
            }
        });

        //Transpose languageList so that 1st row contains display names and 2nd language codes
        String[][] prefList = new String[2][length];
        for (int i = 0; i < length; i++) {
            prefList[0][i] = languageList[i][0];
            prefList[1][i] = languageList[i][1];
        }
        return prefList;
    }
}
