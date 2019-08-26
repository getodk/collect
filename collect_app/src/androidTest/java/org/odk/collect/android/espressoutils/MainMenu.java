package org.odk.collect.android.espressoutils;

import android.app.Activity;

import androidx.test.espresso.Espresso;

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

public final class MainMenu extends Page {

    public MainMenu(Activity activity) {
        super(activity);
    }

    public MainMenu clickOnMenu() {
        Espresso.openActionBarOverflowOrOptionsMenu(ActivityHelpers.getActivity());
        return this;
    }

    public void startBlankForm(String text) {
        onView(withId(R.id.enter_data)).perform(click());
        onData(withRowString(FormsProviderAPI.FormsColumns.DISPLAY_NAME, text)).perform(click());
    }

    public void clickGeneralSettings() {
        onView(withText(getString(R.string.general_preferences))).perform(click());
    }

    public void clickAdminSettings() {
        onView(withText(getString(R.string.admin_preferences))).perform(click());
    }

    public void clickFillBlankForm() {
        onView(withId(R.id.enter_data)).perform(click());
    }

    public void clickOnSortByButton() {
        onView(withId(R.id.menu_sort)).perform(click());
    }

    public void clickMenuFilter() {
        onView(withId(R.id.menu_filter)).perform(click());
    }

    public void searchInBar(String message) {
        onView(withId(R.id.search_src_text)).perform(replaceText(message));
    }

    public void checkIsFormSubtextDisplayed() {
        onView(withIndex(withId(R.id.form_subtitle2), 0)).check(matches(isDisplayed()));
    }

}

