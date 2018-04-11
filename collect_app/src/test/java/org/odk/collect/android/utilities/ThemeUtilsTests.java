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

    private MainMenuActivity mainMenuActivity;
    private final int[] attrs;

    public ThemeUtilsTests() {
        attrs = new int[]{
                android.R.attr.textColor,
                android.R.attr.alertDialogTheme,
                android.R.attr.searchViewStyle,
                android.R.attr.colorControlNormal
        };
    }

    @Before
    public void setup() {
        mainMenuActivity = Robolectric.setupActivity(MainMenuActivity.class);
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
        assertEquals(ThemeUtils.getAppTheme(), R.style.LightAppTheme);
        assertEquals(ThemeUtils.getSettingsTheme(), R.style.AppTheme_SettingsTheme_Light);
        assertEquals(ThemeUtils.getBottomDialogTheme(), R.style.LightMaterialDialogSheet);
        assertEquals(ThemeUtils.getMaterialDialogTheme(), android.R.style.Theme_Material_Light_Dialog);
        assertEquals(ThemeUtils.getHoloDialogTheme(), android.R.style.Theme_Holo_Light_Dialog);
        assertEquals(ThemeUtils.getTextColor(), android.R.color.black);
    }

    @Test
    public void correctStylesShouldBeAppliedForDarkTheme() {
        applyDarkTheme();
        assertEquals(ThemeUtils.getAppTheme(), R.style.DarkAppTheme);
        assertEquals(ThemeUtils.getSettingsTheme(), R.style.AppTheme_SettingsTheme_Dark);
        assertEquals(ThemeUtils.getBottomDialogTheme(), R.style.DarkMaterialDialogSheet);
        assertEquals(ThemeUtils.getMaterialDialogTheme(), android.R.style.Theme_Material_Dialog);
        assertEquals(ThemeUtils.getHoloDialogTheme(), android.R.style.Theme_Holo_Dialog);
        assertEquals(ThemeUtils.getTextColor(), android.R.color.white);
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
