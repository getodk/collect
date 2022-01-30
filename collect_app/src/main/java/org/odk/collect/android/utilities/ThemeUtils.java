/*
 * Copyright (C) 2018 Shobhit Agarwal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.utilities;

import static android.content.res.Configuration.UI_MODE_NIGHT_MASK;
import static android.content.res.Configuration.UI_MODE_NIGHT_YES;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
import static org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatDelegate;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.settings.keys.ProjectKeys;

import javax.inject.Inject;

public final class ThemeUtils {

    @Inject
    SettingsProvider settingsProvider;

    private final Context context;

    public ThemeUtils(Context context) {
        DaggerUtils.getComponent(context).inject(this);
        this.context = context;
    }

    @DrawableRes
    public int getDivider() {
        return isDarkTheme() ? android.R.drawable.divider_horizontal_dark : android.R.drawable.divider_horizontal_bright;
    }

    public boolean isSpinnerDatePickerDialogTheme(int theme) {
        return theme == R.style.Theme_Collect_Dark_Spinner_DatePicker_Dialog ||
                theme == R.style.Theme_Collect_Light_Spinner_DatePicker_Dialog;
    }

    @StyleRes
    public int getCalendarDatePickerDialogTheme() {
        return isDarkTheme()
                ? R.style.Theme_Collect_Dark_Calendar_DatePicker_Dialog
                : R.style.Theme_Collect_Light_Calendar_DatePicker_Dialog;
    }

    @StyleRes
    public int getSpinnerDatePickerDialogTheme() {
        return isDarkTheme() ?
                R.style.Theme_Collect_Dark_Spinner_DatePicker_Dialog :
                R.style.Theme_Collect_Light_Spinner_DatePicker_Dialog;
    }

    @StyleRes
    public int getSpinnerTimePickerDialogTheme() {
        return isDarkTheme() ?
                R.style.Theme_Collect_Dark_Spinner_TimePicker_Dialog :
                R.style.Theme_Collect_Light_Spinner_TimePicker_Dialog;
    }

    public int getAccountPickerTheme() {
        return isDarkTheme() ? 0 : 1;
    }

    public boolean isSystemTheme() {
        return getPrefsTheme().equals(context.getString(R.string.app_theme_system));
    }

    public boolean isDarkTheme() {
        if (isSystemTheme()) {
            int uiMode = context.getResources().getConfiguration().uiMode;
            return (uiMode & UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES;
        } else {
            String theme = getPrefsTheme();
            return theme.equals(context.getString(R.string.app_theme_dark));
        }
    }

    public void setDarkModeForCurrentProject() {
        if (isSystemTheme()) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            AppCompatDelegate.setDefaultNightMode(isDarkTheme() ? MODE_NIGHT_YES : MODE_NIGHT_NO);
        }
    }

    private String getPrefsTheme() {
        return settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_APP_THEME);
    }

    /**
     * @return Text color for the current {@link android.content.res.Resources.Theme}
     */
    @ColorInt
    public int getColorOnSurface() {
        return getThemeAttributeValue(context, R.attr.colorOnSurface);
    }

    @ColorInt
    public int getColorOnSurfaceLowEmphasis() {
        return context.getResources().getColor(R.color.color_on_surface_low_emphasis);
    }

    @ColorInt
    public int getAccentColor() {
        return getThemeAttributeValue(context, R.attr.colorAccent);
    }

    @ColorInt
    public int getIconColor() {
        return getThemeAttributeValue(context, R.attr.colorOnSurface);
    }

    @ColorInt
    public int getColorPrimary() {
        return getThemeAttributeValue(context, R.attr.colorPrimary);
    }

    @ColorInt
    public int getColorOnPrimary() {
        return getThemeAttributeValue(context, R.attr.colorOnPrimary);
    }

    @ColorInt
    public int getColorSecondary() {
        return getThemeAttributeValue(context, R.attr.colorSecondary);
    }

    @ColorInt
    public int getColorError() {
        return getThemeAttributeValue(context, R.attr.colorError);
    }

    @ColorInt
    public int getColorPrimaryDark() {
        return getThemeAttributeValue(context, R.attr.colorPrimaryDark);
    }
}
