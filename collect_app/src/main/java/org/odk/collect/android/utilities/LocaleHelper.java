package org.odk.collect.android.utilities;

import android.content.Context;
import android.preference.PreferenceManager;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralKeys;

import java.util.Locale;
import java.util.TreeMap;

/**
 * Changes the locale of the app and keeps the changes persistent
 *
 * @author abdulwd
 */
public class LocaleHelper {
    public static String getLocaleCode(Context context) {
        String localeCode = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(GeneralKeys.KEY_APP_LANGUAGE, "");
        boolean isUsingSysLanguage = localeCode.equals("");
        if (isUsingSysLanguage) {
            localeCode = Collect.defaultSysLanguage;
        }
        return localeCode;
    }

    public TreeMap<String, String> getEntryListValues() {
        //Holds language as key and language code as value
        TreeMap<String, String> languageList = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String language : ApplicationConstants.TRANSLATIONS_AVAILABLE) {
            Locale locale = getLocale(language);
            languageList.put(locale.getDisplayName(locale), language);
        }
        return languageList;
    }

    public Locale getLocale(Context context) {
        return getLocale(getLocaleCode(context));
    }

    private Locale getLocale(String splitLocaleCode) {
        if (splitLocaleCode.contains("_")) {
            String[] arg = splitLocaleCode.split("_");
            return new Locale(arg[0], arg[1]);
        } else {
            return new Locale(splitLocaleCode);
        }
    }
}
