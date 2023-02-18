package org.odk.collect.android.utilities;

import org.odk.collect.android.application.Collect;
import org.odk.collect.shared.strings.StringUtils;

import java.util.Locale;
import java.util.TreeMap;

/**
 * Changes the locale of the app and keeps the changes persistent
 *
 * @author abdulwd
 */
public class LocaleHelper {
    public TreeMap<String, String> getEntryListValues() {
        //Holds language as key and language code as value
        TreeMap<String, String> languageList = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String localeCode : ApplicationConstants.TRANSLATIONS_AVAILABLE) {
            Locale locale = getLocale(localeCode);
            languageList.put(locale.getDisplayName(locale), localeCode);
        }
        return languageList;
    }

    public static Locale getLocale(String localeCode) {
        String sanitizedLocaleCode = localeCode == null || StringUtils.isBlank(localeCode) ? Collect.defaultSysLanguage : localeCode;

        if (sanitizedLocaleCode.contains("_")) {
            String[] arg = sanitizedLocaleCode.split("_");
            return new Locale(arg[0], arg[1]);
        } else {
            return new Locale(sanitizedLocaleCode);
        }
    }
}
