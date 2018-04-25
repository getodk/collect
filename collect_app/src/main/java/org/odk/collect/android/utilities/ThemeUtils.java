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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.TypedValue;
import android.widget.ImageButton;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;


public final class ThemeUtils {

    private ThemeUtils() {

    }

    private static boolean isDarkTheme() {
        String theme = (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_APP_THEME);
        return theme.equals(Collect.getInstance().getString(R.string.app_theme_dark));
    }

    public static int getAppTheme() {
        return isDarkTheme() ? R.style.DarkAppTheme : R.style.LightAppTheme;
    }

    public static int getSettingsTheme() {
        return isDarkTheme() ? R.style.AppTheme_SettingsTheme_Dark : R.style.AppTheme_SettingsTheme_Light;
    }

    public static int getBottomDialogTheme() {
        return isDarkTheme() ? R.style.DarkMaterialDialogSheet : R.style.LightMaterialDialogSheet;
    }

    /*
     *   TODO : Remove this method once all drawables are converted to vectors and use ?colorControlNormal for android:fillColor attribute
     */
    public static void setIconTint(Context context, ImageButton... imageButtons) {
        for (ImageButton imageButton : imageButtons) {
            setIconTint(context, imageButton.getDrawable());
        }
    }

    private static void setIconTint(Context context, Drawable drawable) {
        DrawableCompat.setTint(drawable, context.getResources()
                .getColor(isDarkTheme() ? android.R.color.white : android.R.color.black));
    }

    public static boolean isHoloDialogTheme(int theme) {
        return theme == android.R.style.Theme_Holo_Light_Dialog ||
                theme == android.R.style.Theme_Holo_Dialog;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static int getMaterialDialogTheme() {
        return isDarkTheme() ?
                android.R.style.Theme_Material_Dialog :
                android.R.style.Theme_Material_Light_Dialog;
    }

    public static int getHoloDialogTheme() {
        return isDarkTheme() ?
                android.R.style.Theme_Holo_Dialog :
                android.R.style.Theme_Holo_Light_Dialog;
    }

    public static int getAttributeValue(Context context, int resId) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, outValue, true);
        return outValue.data;
    }
}
