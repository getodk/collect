package org.odk.collect.android.espressoutils;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.support.ActivityHelpers;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.CursorMatchers.withRowString;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.odk.collect.android.test.CustomMatchers.withIndex;

public final class MainMenuPage extends Page<MainMenuPage> {

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

    public void clickGeneralSettings() {
        onView(withText(getString(R.string.general_preferences))).perform(click());
    }

    public void clickAdminSettings() {
        onView(withText(getString(R.string.admin_preferences))).perform(click());
    }

    public MainMenuPage clickFillBlankForm() {
        onView(withId(R.id.enter_data)).perform(click());
        return this;
    }

    public MainMenuPage clickOnSortByButton() {
        onView(withId(R.id.menu_sort)).perform(click());
        return this;
    }

    public MainMenuPage clickMenuFilter() {
        onView(withId(R.id.menu_filter)).perform(click());
        return this;
    }

    public MainMenuPage searchInBar(String message) {
        onView(withId(R.id.search_src_text)).perform(replaceText(message));
        return this;
    }

    public void checkIsFormSubtextDisplayed() {
        onView(withIndex(withId(R.id.form_subtitle2), 0)).check(matches(isDisplayed()));
    }
}

