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
import android.view.Menu;
import android.widget.ImageButton;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;


public final class ThemeUtils {

    private ThemeUtils() {

    }

    public static boolean isDarkTheme() {
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

    public static void setIconTint(Context context, ImageButton... imageButtons) {
        for (ImageButton imageButton : imageButtons) {
            setIconTint(context, imageButton.getDrawable());
        }
    }

    public static void setIconTint(Context context, Drawable... drawables) {
        for (Drawable drawable : drawables) {
            DrawableCompat.setTint(drawable, context.getResources().getColor(isDarkTheme() ? R.color.white : R.color.black));
        }
    }

    public static void setMenuTint(Context context, Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable menuIcon = menu.getItem(i).getIcon();
            if (menuIcon != null) {
                setIconTint(context, menuIcon);
            }
        }
    }

    public static boolean isHoloDialogTheme(int theme) {
        return theme == android.R.style.Theme_Holo_Light_Dialog ||
                theme == android.R.style.Theme_Holo_Dialog;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static int getMaterialDialogTheme() {
        return ThemeUtils.isDarkTheme() ?
                android.R.style.Theme_Material_Dialog :
                android.R.style.Theme_Material_Light_Dialog;
    }

    public static int getHoloDialogTheme() {
        return ThemeUtils.isDarkTheme() ?
                android.R.style.Theme_Holo_Dialog :
                android.R.style.Theme_Holo_Light_Dialog;
    }
}
