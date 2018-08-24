package org.odk.collect.android.utilities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferenceKeys;

import java.util.Locale;
import java.util.TreeMap;

/**
 * Changes the locale of the app and keeps the changes persistent
 *
 * @author abdulwd
 */

public class LocaleHelper {

    // Created based on https://gunhansancar.com/change-language-programmatically-in-android/
    public Context updateLocale(Context context) {
        return updateLocale(context, getLocaleCode(context));
    }

    private Context updateLocale(Context context, String language) {
        Locale locale = getLocale(language);
        Locale.setDefault(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }

        return updateResourcesLegacy(context, language);
    }

    private String getLocaleCode(Context context) {
        String localeCode = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PreferenceKeys.KEY_APP_LANGUAGE, "");
        boolean isUsingSysLanguage = localeCode.equals("");
        if (isUsingSysLanguage) {
            localeCode = Collect.defaultSysLanguage;
        }
        return localeCode;
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Context updateResources(Context context, String language) {
        Locale locale = getLocale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private Context updateResourcesLegacy(Context context, String language) {
        Locale locale = getLocale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
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

    private Locale getLocale(String splitLocaleCode) {
        if (splitLocaleCode.contains("_")) {
            String[] arg = splitLocaleCode.split("_");
            return new Locale(arg[0], arg[1]);
        } else {
            return new Locale(splitLocaleCode);
        }
    }
}
