package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.util.Locale;
import java.util.TreeMap;

/**
 * Changes the locale of the app and keeps the changes persistent
 *
 * @author abdulwd
 */

public class LocaleHelper {

    public void updateLocale(Context context) {
        String localeCode = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PreferenceKeys.KEY_LANGUAGE, "en");
        Locale locale = getLocale(localeCode);
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

    public TreeMap<String, String> getEntryListValues() {
        String[] languages = {"af", "am", "ar", "bn", "ca", "cs", "de", "en",
                "es", "es_SV", "et", "fa", "fi", "fr", "ha", "hi", "hi_IN", "hu", "in",
                "it", "ja", "ka", "km", "lo_LA", "lt", "mg", "my", "nb", "ne_NP", "nl",
                "no", "pl", "ps", "pt", "ro", "ru", "so", "sq", "sw", "sw_KE", "ta",
                "th_TH", "tl", "tl_PH", "tr", "uk", "ur", "ur_PK", "vi", "zh", "zu"};

        //Holds language as key and language code as value
        TreeMap<String, String> languageList = new TreeMap<>();
        for (String language : languages) {
            Locale locale = getLocale(language);
            languageList.put(locale.getDisplayName(locale), language);
        }
        return languageList;
    }

    private Locale getLocale(String language) {
        if (language.contains("_")) {
            String arg[] = language.split("_");
            return new Locale(arg[0], arg[1]);
        } else {
            return new Locale(language);
        }
    }
}
