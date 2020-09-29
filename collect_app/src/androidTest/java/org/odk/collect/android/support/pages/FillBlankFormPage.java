package org.odk.collect.android.support.pages;

import android.database.Cursor;

import androidx.test.espresso.matcher.CursorMatchers;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.CursorMatchers.withRowString;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.odk.collect.android.support.CustomMatchers.withIndex;

public class FillBlankFormPage extends Page<FillBlankFormPage> {

    public FillBlankFormPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public FillBlankFormPage assertOnPage() {
        assertToolbarTitle(R.string.enter_data);
        return this;
    }

    public IdentifyUserPromptPage clickOnFormWithIdentityPrompt(String formName) {
        clickOnFormButton(formName);
        return new IdentifyUserPromptPage(formName, rule).assertOnPage();
    }

    public FillBlankFormPage clickOnSortByButton() {
        onView(withId(R.id.menu_sort)).perform(click());
        return this;
    }

    public FillBlankFormPage clickMenuFilter() {
        onView(withId(R.id.menu_filter)).perform(click());
        return this;
    }

    public BlankFormSearchPage searchInBar(String query) {
        onView(withId(R.id.search_src_text)).perform(replaceText(query));
        return new BlankFormSearchPage(rule).assertOnPage();
    }

    public FillBlankFormPage checkIsFormSubtextDisplayed() {
        onView(withIndex(withId(R.id.form_subtitle2), 0)).check(matches(isDisplayed()));
        return this;
    }

    public FillBlankFormPage checkMapIconDisplayedForForm(String formName) {
        onData(allOf(is(instanceOf(Cursor.class)), CursorMatchers.withRowString(FormsColumns.DISPLAY_NAME, is(formName))))
                .onChildView(withId(R.id.map_button))
                .check(matches(isDisplayed()));
        return this;
    }

    public FillBlankFormPage checkMapIconNotDisplayedForForm(String formName) {
        onData(allOf(is(instanceOf(Cursor.class)), CursorMatchers.withRowString(FormsColumns.DISPLAY_NAME, is(formName))))
                .onChildView(withId(R.id.map_button))
                .check(matches(not(isDisplayed())));
        return this;
    }

    public FormMapPage clickOnMapIconForForm(String formName) {
        onData(allOf(is(instanceOf(Cursor.class)), CursorMatchers.withRowString(FormsColumns.DISPLAY_NAME, is(formName))))
                .onChildView(withId(R.id.map_button))
                .perform(click());
        return new FormMapPage(rule).assertOnPage();
    }

    public FormEntryPage clickOnForm(String formName) {
        clickOnFormButton(formName);
        return new FormEntryPage(formName, rule);
    }

    private void clickOnFormButton(String formName) {
        assertFormExists(formName);
        onData(withRowString(FormsColumns.DISPLAY_NAME, formName)).perform(click());
    }

    public FormEndPage clickOnEmptyForm(String formName) {
        clickOnFormButton(formName);
        return new FormEndPage(formName, rule).assertOnPage();
    }

    public FillBlankFormPage clickRefresh() {
        onView(withId(R.id.menu_refresh)).perform(click());
        return this;
    }

    public FillBlankFormPage clickRefreshWithError() {
        onView(withId(R.id.menu_refresh)).perform(click());
        return this;
    }

    public ServerAuthDialog clickRefreshWithAuthError() {
        onView(withId(R.id.menu_refresh)).perform(click());
        return new ServerAuthDialog(rule).assertOnPage();
    }

    public FillBlankFormPage assertFormExists(String formName) {
        // Seen problems with disk syncing not being waited for even though it's an AsyncTask
        return waitFor(() -> {
            assertTextNotDisplayed(R.string.no_items_display_forms);
            onData(withRowString(FormsColumns.DISPLAY_NAME, formName)).check(matches(isDisplayed()));
            return this;
        });
    }

    public FillBlankFormPage assertFormDoesNotExist(String formName) {
        onData(withRowString(FormsColumns.DISPLAY_NAME, formName)).check(doesNotExist());
        return this;
    }

    public FillBlankFormPage assertNoForms() {
        assertText(R.string.no_items_display_forms);
        return this;
    }
}
