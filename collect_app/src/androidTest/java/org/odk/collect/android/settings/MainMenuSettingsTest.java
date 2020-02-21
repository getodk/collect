package org.odk.collect.android.settings;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.support.pages.AdminSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainMenuSettingsTest {
    @Rule
    public ActivityTestRule<MainMenuActivity> rule = new ActivityTestRule<>(MainMenuActivity.class);

    @Test
    public void configureWithQRCanBeDisabled() {
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickAdminSettings()
                .clickOnString(R.string.main_menu_settings)
                .clickOnString(R.string.qr_code)
                .pressBack(new AdminSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .clickOnMenu()
                .checkIfTextDoesNotExist(R.string.scan_qrcode_overflow_menu);
    }

}
