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

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;

import android.util.TypedValue;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.GeneralKeys;

public final class ThemeUtils {

    private final Context context;

    public ThemeUtils(Context context) {
        this.context = context;
    }

    @StyleRes
    public int getAppTheme() {
        return isDarkTheme() ? R.style.Theme_Collect_Dark : R.style.Theme_Collect_Light;
    }

    @StyleRes
    public int getFormEntryActivityTheme() {
        return isDarkTheme() ? R.style.Theme_Collect_Activity_FormEntryActivity_Dark : R.style.Theme_Collect_Activity_FormEntryActivity_Light;
    }

    @StyleRes
    public int getSettingsTheme() {
        return isDarkTheme() ? R.style.Theme_Collect_Settings_Dark : R.style.Theme_Collect_Settings_Light;
    }

    @StyleRes
    public int getBottomDialogTheme() {
        return isDarkTheme() ? R.style.Theme_Collect_MaterialDialogSheet_Dark : R.style.Theme_Collect_MaterialDialogSheet_Light;
    }

    @DrawableRes
    public int getDivider() {
        return isDarkTheme() ? android.R.drawable.divider_horizontal_dark : android.R.drawable.divider_horizontal_bright;
    }

    public boolean isHoloDialogTheme(int theme) {
        return theme == android.R.style.Theme_Holo_Light_Dialog ||
                theme == android.R.style.Theme_Holo_Dialog;
    }

    @StyleRes
    public int getMaterialDialogTheme() {
        return isDarkTheme()
                ? R.style.Theme_Collect_Dark_Dialog
                : R.style.Theme_Collect_Light_Dialog;
    }

    @StyleRes
    public int getHoloDialogTheme() {
        return isDarkTheme() ?
                android.R.style.Theme_Holo_Dialog :
                android.R.style.Theme_Holo_Light_Dialog;
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
        String theme = (String) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_APP_THEME);
        return theme.equals(context.getString(R.string.app_theme_dark));
    }

    /**
     * @return Text color for the current {@link android.content.res.Resources.Theme}
     */
    @ColorInt
    public int getColorOnSurface() {
        return getAttributeValue(R.attr.colorOnSurface);
    }

    /**
     * @return Accent color for the current {@link android.content.res.Resources.Theme}
     */
    @ColorInt
    public int getAccentColor() {
        return getAttributeValue(R.attr.colorAccent);
    }

    /**
     * @return Icon color for the current {@link android.content.res.Resources.Theme}
     */
    @ColorInt
    public int getIconColor() {
        return getAttributeValue(R.attr.iconColor);
    }

    /**
     * @return Rank item color for the current {@link android.content.res.Resources.Theme}
     */
    @ColorInt
    public int getRankItemColor() {
        return getAttributeValue(R.attr.rankItemColor);
    }
}
