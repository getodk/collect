package org.odk.collect.android.espressoutils.pages;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.support.ActivityHelpers;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.CursorMatchers.withRowString;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class MainMenuPage extends Page<MainMenuPage> {

    public MainMenuPage(ActivityTestRule rule) {
        super(rule);
    }

    public MainMenuPage clickOnMenu() {
        Espresso.openActionBarOverflowOrOptionsMenu(ActivityHelpers.getActivity());
        return this;
    }

    public FormEntryPage startBlankForm(String text) {
        onView(withId(R.id.enter_data)).perform(click());
        onData(withRowString(FormsProviderAPI.FormsColumns.DISPLAY_NAME, text)).perform(click());
        return new FormEntryPage(rule);
    }

    public SettingsPage clickGeneralSettings() {
        clickOnString(R.string.general_preferences);
        return new SettingsPage(rule);
    }

    public void clickAdminSettings() {
        clickOnString(R.string.admin_preferences);
    }

    public FillBlankFormPage clickFillBlankForm() {
        onView(withId(R.id.enter_data)).perform(click());
        return new FillBlankFormPage(rule);
    }
}

