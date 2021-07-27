package org.odk.collect.android.utilities;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.keys.ProjectKeys;
import org.odk.collect.shared.Settings;

import java.util.Locale;
import java.util.TreeMap;

/**
 * Changes the locale of the app and keeps the changes persistent
 *
 * @author abdulwd
 */
public class LocaleHelper {
    public static String getLocaleCode(Settings generalSettings) {
        String localeCode = generalSettings.getString(ProjectKeys.KEY_APP_LANGUAGE);
        if (localeCode == null) {
            return "";
        }
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

    public Locale getLocale(Settings generalSettings) {
        return getLocale(getLocaleCode(generalSettings));
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
