package org.odk.collect.android.espressoutils;

import androidx.test.espresso.Espresso;
import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsProviderAPI;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.CursorMatchers.withRowString;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public final class MainMenu {
    private MainMenu() {
    }

    public static void clickOnMenu() {
        Espresso.openContextualActionModeOverflowMenu();
    }

    public static void startBlankForm(String text) {
        onView(withId(R.id.enter_data)).perform(click());
        onData(withRowString(FormsProviderAPI.FormsColumns.DISPLAY_NAME, text)).perform(click());
    }

    public static void clickGeneralSettings() {
        onView(withText(getInstrumentation().getTargetContext().getString(R.string.general_preferences))).perform(click());
    }

    public static void clickFillBlankForm() {
        onView(withId(R.id.enter_data)).perform(click());
    }

    public static void clickOnSortByButton() {
        onView(withId(R.id.menu_sort)).perform(click());
    }
}

