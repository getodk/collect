package org.odk.collect.android.settings;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;

import androidx.test.espresso.Espresso;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainMenuSettingsTest {
    @Rule
    public ActivityTestRule<MainMenuActivity> activityRule =
            new ActivityTestRule<>(MainMenuActivity.class);

    @Test
    public void configureWithQRCanBeDisabled() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(withText(R.string.admin_preferences))
                .perform(click());

        onView(withText(R.string.main_menu_settings))
                .perform(click());

        onView(withText(R.string.qr_code))
                .perform(click());

        Espresso.pressBack();
        Espresso.pressBack();

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(withText(R.string.scan_qrcode_overflow_menu))
                .check(doesNotExist());
    }
}
