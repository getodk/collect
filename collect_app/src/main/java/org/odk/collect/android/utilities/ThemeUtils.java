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

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.keys.ProjectKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;

import javax.inject.Inject;

public final class ThemeUtils {

    @Inject
    SettingsProvider settingsProvider;

    private final Context context;

    public ThemeUtils(Context context) {
        DaggerUtils.getComponent(context).inject(this);
        this.context = context;
    }

    @StyleRes
    public int getAppTheme() {
        if (isMagentaEnabled()) {
            return R.style.Theme_Collect_Magenta;
        } else {
            String theme = getPrefsTheme();
            if (theme.equals(context.getString(R.string.app_theme_dark))) {
                return R.style.Theme_Collect_Dark;
            } else {
                return R.style.Theme_Collect_Light;
            }
        }
    }

    @StyleRes
    public int getFormEntryActivityTheme() {
        if (isMagentaEnabled()) {
            return R.style.Theme_Collect_Activity_FormEntryActivity_Magenta;
        } else {
            String theme = getPrefsTheme();
            if (theme.equals(context.getString(R.string.app_theme_dark))) {
                return R.style.Theme_Collect_Activity_FormEntryActivity_Dark;
            } else {
                return R.style.Theme_Collect_Activity_FormEntryActivity_Light;
            }
        }
    }

    @StyleRes
    public int getSettingsTheme() {
        if (isMagentaEnabled()) {
            return R.style.Theme_Collect_Settings_Magenta;
        } else {
            String theme = getPrefsTheme();
            if (theme.equals(context.getString(R.string.app_theme_dark))) {
                return R.style.Theme_Collect_Settings_Dark;
            } else {
                return R.style.Theme_Collect_Settings_Light;
            }
        }
    }

    @StyleRes
    public int getBottomDialogTheme() {
        return isDarkTheme() ? R.style.Theme_Collect_MaterialDialogSheet_Dark : R.style.Theme_Collect_MaterialDialogSheet_Light;
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

    private int getAttributeValue(@AttrRes int resId) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, outValue, true);
        return outValue.data;
    }

    public int getAccountPickerTheme() {
        return isDarkTheme() ? 0 : 1;
    }

    public boolean isDarkTheme() {
        String theme = getPrefsTheme();
        return theme.equals(context.getString(R.string.app_theme_dark));
    }

    private boolean isMagentaEnabled() {
        return settingsProvider.getGeneralSettings().getBoolean(ProjectKeys.KEY_MAGENTA_THEME);
    }

    private String getPrefsTheme() {
        return settingsProvider.getGeneralSettings().getString(ProjectKeys.KEY_APP_THEME);
    }

    /**
     * @return Text color for the current {@link android.content.res.Resources.Theme}
     */
    @ColorInt
    public int getColorOnSurface() {
        return getAttributeValue(R.attr.colorOnSurface);
    }

    @ColorInt
    public int getAccentColor() {
        return getAttributeValue(R.attr.colorAccent);
    }

    @ColorInt
    public int getIconColor() {
        return getAttributeValue(R.attr.colorOnSurface);
    }

    @ColorInt
    public int getColorPrimary() {
        return getAttributeValue(R.attr.colorPrimary);
    }

    @ColorInt
    public int getColorOnPrimary() {
        return getAttributeValue(R.attr.colorOnPrimary);
    }

    @ColorInt
    public int getColorSecondary() {
        return getAttributeValue(R.attr.colorSecondary);
    }
}
