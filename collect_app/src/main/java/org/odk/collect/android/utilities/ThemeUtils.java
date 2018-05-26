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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.StyleRes;
import android.util.TypedValue;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;

import javax.inject.Inject;

public final class ThemeUtils {

    @Inject
    GeneralSharedPreferences generalSharedPreferences;
    @Inject
    Context context;

    @Inject
    public ThemeUtils() {
    }

    public boolean isDarkTheme() {
        String theme = (String) generalSharedPreferences.get(PreferenceKeys.KEY_APP_THEME);
        return theme.equals(context.getString(R.string.app_theme_dark));
    }

    @StyleRes
    public int getAppTheme() {
        return isDarkTheme() ? R.style.DarkAppTheme : R.style.LightAppTheme;
    }

    @StyleRes
    public int getSettingsTheme() {
        return isDarkTheme() ? R.style.AppTheme_SettingsTheme_Dark : R.style.AppTheme_SettingsTheme_Light;
    }

    @StyleRes
    public int getBottomDialogTheme() {
        return isDarkTheme() ? R.style.DarkMaterialDialogSheet : R.style.LightMaterialDialogSheet;
    }

    @DrawableRes
    public int getDivider() {
        return isDarkTheme() ? android.R.drawable.divider_horizontal_dark : android.R.drawable.divider_horizontal_bright;
    }

    public boolean isHoloDialogTheme(int theme) {
        return theme == android.R.style.Theme_Holo_Light_Dialog ||
                theme == android.R.style.Theme_Holo_Dialog;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @StyleRes
    public int getMaterialDialogTheme() {
        return isDarkTheme() ?
                android.R.style.Theme_Material_Dialog :
                android.R.style.Theme_Material_Light_Dialog;
    }

    @StyleRes
    public int getHoloDialogTheme() {
        return isDarkTheme() ?
                android.R.style.Theme_Holo_Dialog :
                android.R.style.Theme_Holo_Light_Dialog;
    }

    public int getAccountPickerTheme() {
        return isDarkTheme() ? 0 : 1;
    }

    /**
     * @param context Should only be an activity's context. This is because application context
     *                is unaware of current activity's theme/attributes.
     */
    private int getAttributeValue(Context context, @AttrRes int resId) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, outValue, true);
        return outValue.data;
    }

    /**
     * @return Text color for the current {@link android.content.res.Resources.Theme}
     */
    @ColorInt
    public int getPrimaryTextColor(Context context) {
        return getAttributeValue(context, R.attr.primaryTextColor);
    }

    /**
     * @return Accent color for the current {@link android.content.res.Resources.Theme}
     */
    @ColorInt
    public int getAccentColor(Context context) {
        return getAttributeValue(context, R.attr.colorAccent);
    }

    /**
     * @return Icon color for the current {@link android.content.res.Resources.Theme}
     */
    @ColorInt
    public int getIconColor(Context context) {
        return getAttributeValue(context, R.attr.iconColor);
    }

    /**
     * @return Rank item color for the current {@link android.content.res.Resources.Theme}
     */
    @ColorInt
    public int getRankItemColor() {
        return getAttributeValue(R.attr.rankItemColor);
    }
}
