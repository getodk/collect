package org.odk.collect.android.utilities;

import android.content.res.Resources;
import android.content.res.TypedArray;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_APP_THEME;

/**
 * Unit tests for checking the behaviour of updating themes from User Interface settings
 */

@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class ThemeUtilsTests {

    private final int[] attrs;
    private ThemeUtils themeUtils;
    private MainMenuActivity mainMenuActivity;

    public ThemeUtilsTests() {
        attrs = new int[]{
                R.attr.primaryTextColor,
                R.attr.iconColor,
                android.R.attr.alertDialogTheme,
                android.R.attr.searchViewStyle,
                android.R.attr.colorControlNormal
        };
    }

    @Before
    public void setup() {
        mainMenuActivity = Robolectric.setupActivity(MainMenuActivity.class);
        themeUtils = new ThemeUtils(mainMenuActivity);
    }

    @Test
    public void defaultThemeShouldBeLight() {
        assertCurrentTheme(getLightTheme(), mainMenuActivity.getTheme(), true);
        assertCurrentTheme(getDarkTheme(), mainMenuActivity.getTheme(), false);
    }

    @Test
    public void themeShouldBeChangedWhenUpdatedFromUISettings() {
        assertCurrentTheme(getDarkTheme(), mainMenuActivity.getTheme(), false);

        applyDarkTheme();

        MainMenuActivity newMainMenuActivity = Robolectric.setupActivity(MainMenuActivity.class);

        assertCurrentTheme(getLightTheme(), newMainMenuActivity.getTheme(), false);
        assertCurrentTheme(getDarkTheme(), newMainMenuActivity.getTheme(), true);
    }

    @Test
    public void correctStylesShouldBeAppliedForLightTheme() {
        applyLightTheme();
        assertEquals(themeUtils.getAppTheme(), R.style.LightAppTheme);
        assertEquals(themeUtils.getSettingsTheme(), R.style.AppTheme_SettingsTheme_Light);
        assertEquals(themeUtils.getBottomDialogTheme(), R.style.LightMaterialDialogSheet);
        assertEquals(themeUtils.getMaterialDialogTheme(), android.R.style.Theme_Material_Light_Dialog);
        assertEquals(themeUtils.getHoloDialogTheme(), android.R.style.Theme_Holo_Light_Dialog);
    }

    @Test
    public void correctStylesShouldBeAppliedForDarkTheme() {
        applyDarkTheme();
        assertEquals(themeUtils.getAppTheme(), R.style.DarkAppTheme);
        assertEquals(themeUtils.getSettingsTheme(), R.style.AppTheme_SettingsTheme_Dark);
        assertEquals(themeUtils.getBottomDialogTheme(), R.style.DarkMaterialDialogSheet);
        assertEquals(themeUtils.getMaterialDialogTheme(), android.R.style.Theme_Material_Dialog);
        assertEquals(themeUtils.getHoloDialogTheme(), android.R.style.Theme_Holo_Dialog);
    }

    private void assertCurrentTheme(Resources.Theme expectedTheme, Resources.Theme actualTheme, boolean assertTrue) {
        TypedArray expectedTypedArray = expectedTheme.obtainStyledAttributes(attrs);
        TypedArray actualTypedArray = actualTheme.obtainStyledAttributes(attrs);

        assertEquals(actualTypedArray.length(), expectedTypedArray.length());

        for (int i = 0; i < actualTypedArray.length() - 1; i++) {
            if (assertTrue) {
                assertEquals(expectedTypedArray.getText(i), actualTypedArray.getText(i));
            } else {
                assertNotSame(expectedTypedArray.getText(i), actualTypedArray.getText(i));
            }
        }
    }

    private Resources.Theme getDarkTheme() {
        return createTheme(R.style.DarkAppTheme);
    }

    private Resources.Theme getLightTheme() {
        return createTheme(R.style.LightAppTheme);
    }

    private Resources.Theme createTheme(int styleResId) {
        Resources.Theme theme = mainMenuActivity.getResources().newTheme();
        theme.applyStyle(styleResId, true);
        return theme;
    }

    private void applyDarkTheme() {
        GeneralSharedPreferences.getInstance().save(KEY_APP_THEME, mainMenuActivity.getString(R.string.app_theme_dark));
    }

    private void applyLightTheme() {
        GeneralSharedPreferences.getInstance().save(KEY_APP_THEME, mainMenuActivity.getString(R.string.app_theme_light));
    }
}
