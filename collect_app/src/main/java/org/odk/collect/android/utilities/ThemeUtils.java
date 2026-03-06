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

import static org.odk.collect.androidshared.system.ContextExt.getThemeAttributeValue;
import static org.odk.collect.androidshared.system.ContextExt.isDarkTheme;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.settings.SettingsProvider;

import javax.inject.Inject;

/**
 * @deprecated Use
 * {@link org.odk.collect.androidshared.system.ContextExt#getThemeAttributeValue(Context, int)}
 * instead.
 */
@Deprecated
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
        return isDarkTheme(context) ? android.R.drawable.divider_horizontal_dark : android.R.drawable.divider_horizontal_bright;
    }

    public boolean isSpinnerDatePickerDialogTheme(int theme) {
        return theme == R.style.Theme_Collect_Dark_Spinner_DatePicker_Dialog ||
                theme == R.style.Theme_Collect_Light_Spinner_DatePicker_Dialog;
    }

    @StyleRes
    public int getCalendarDatePickerDialogTheme() {
        return isDarkTheme(context)
                ? R.style.Theme_Collect_Dark_Calendar_DatePicker_Dialog
                : R.style.Theme_Collect_Light_Calendar_DatePicker_Dialog;
    }

    @StyleRes
    public int getSpinnerDatePickerDialogTheme() {
        return isDarkTheme(context) ?
                R.style.Theme_Collect_Dark_Spinner_DatePicker_Dialog :
                R.style.Theme_Collect_Light_Spinner_DatePicker_Dialog;
    }

    @StyleRes
    public int getSpinnerTimePickerDialogTheme() {
        return isDarkTheme(context) ?
                R.style.Theme_Collect_Dark_Spinner_TimePicker_Dialog :
                R.style.Theme_Collect_Light_Spinner_TimePicker_Dialog;
    }

    /**
     * @return Text color for the current {@link android.content.res.Resources.Theme}
     */
    @ColorInt
    public int getColorOnSurface() {
        return getThemeAttributeValue(context, com.google.android.material.R.attr.colorOnSurface);
    }

    @ColorInt
    public int getColorPrimary() {
        return getThemeAttributeValue(context, androidx.appcompat.R.attr.colorPrimary);
    }
}
