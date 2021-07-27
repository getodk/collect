package org.odk.collect.android.utilities;

import android.content.res.Resources;
import android.content.res.TypedArray;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.shared.Settings;
import org.robolectric.Robolectric;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_APP_THEME;

/**
 * Unit tests for checking the behaviour of updating themes from User Interface settings
 */
@RunWith(AndroidJUnit4.class)
public class ThemeUtilsTest {

    private final int[] attrs;
    private ThemeUtils themeUtils;
    private MainMenuActivity mainMenuActivity;
    private Settings generalSettings;

    public ThemeUtilsTest() {
        attrs = new int[]{
                android.R.attr.alertDialogTheme,
                android.R.attr.searchViewStyle,
                android.R.attr.colorControlNormal
        };
    }

    @Before
    public void setup() {
        CollectHelpers.setupDemoProject();

        mainMenuActivity = Robolectric.setupActivity(MainMenuActivity.class);
        themeUtils = new ThemeUtils(mainMenuActivity);
        generalSettings = TestSettingsProvider.getGeneralSettings();
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
        assertEquals(themeUtils.getAppTheme(), R.style.Theme_Collect_Light);
        assertEquals(themeUtils.getSettingsTheme(), R.style.Theme_Collect_Settings_Light);
        assertEquals(themeUtils.getBottomDialogTheme(), R.style.Theme_Collect_MaterialDialogSheet_Light);
        assertEquals(themeUtils.getCalendarDatePickerDialogTheme(), R.style.Theme_Collect_Light_Calendar_DatePicker_Dialog);
        assertEquals(themeUtils.getSpinnerDatePickerDialogTheme(), R.style.Theme_Collect_Light_Spinner_DatePicker_Dialog);
    }

    @Test
    public void correctStylesShouldBeAppliedForDarkTheme() {
        applyDarkTheme();
        assertEquals(themeUtils.getAppTheme(), R.style.Theme_Collect_Dark);
        assertEquals(themeUtils.getSettingsTheme(), R.style.Theme_Collect_Settings_Dark);
        assertEquals(themeUtils.getBottomDialogTheme(), R.style.Theme_Collect_MaterialDialogSheet_Dark);
        assertEquals(themeUtils.getCalendarDatePickerDialogTheme(), R.style.Theme_Collect_Dark_Calendar_DatePicker_Dialog);
        assertEquals(themeUtils.getSpinnerDatePickerDialogTheme(), R.style.Theme_Collect_Dark_Spinner_DatePicker_Dialog);
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
        return createTheme(R.style.Theme_Collect_Dark);
    }

    private Resources.Theme getLightTheme() {
        return createTheme(R.style.Theme_Collect_Light);
    }

    private Resources.Theme createTheme(int styleResId) {
        Resources.Theme theme = mainMenuActivity.getResources().newTheme();
        theme.applyStyle(styleResId, true);
        return theme;
    }

    private void applyDarkTheme() {
        generalSettings.save(KEY_APP_THEME, mainMenuActivity.getString(R.string.app_theme_dark));
        assertThat(generalSettings.getString(KEY_APP_THEME), is(mainMenuActivity.getString(R.string.app_theme_dark)));
    }

    private void applyLightTheme() {
        generalSettings.save(KEY_APP_THEME, mainMenuActivity.getString(R.string.app_theme_light));
    }
}
