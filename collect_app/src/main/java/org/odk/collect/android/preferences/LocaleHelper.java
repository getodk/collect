package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

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
        if (!localeCode.contains("_")) locale = new Locale(localeCode);
        else {
            String[] arg = localeCode.split("_");
            locale = new Locale(arg[0], arg[1]);
        }
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) configuration.setLocale(locale);
        else configuration.locale = locale;
        context.getResources().updateConfiguration(configuration, displayMetrics);
        context.getApplicationContext().getResources().updateConfiguration(configuration, displayMetrics);
    }

}
