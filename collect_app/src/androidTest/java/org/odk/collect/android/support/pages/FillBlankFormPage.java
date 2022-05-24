package org.odk.collect.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.odk.collect.android.support.matchers.CustomMatchers.withIndex;
import static org.odk.collect.testshared.ViewActions.clickOnViewContentDescription;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;

import org.odk.collect.android.R;
import org.odk.collect.android.support.WaitFor;

public class FillBlankFormPage extends Page<FillBlankFormPage> {

    @Override
    public FillBlankFormPage assertOnPage() {
        assertToolbarTitle(R.string.enter_data);
        return this;
    }

    public IdentifyUserPromptPage clickOnFormWithIdentityPrompt(String formName) {
        clickOnFormButton(formName);
        return new IdentifyUserPromptPage(formName).assertOnPage();
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
        return new BlankFormSearchPage().assertOnPage();
    }

    public FillBlankFormPage checkIsFormSubtextDisplayed() {
        return WaitFor.waitFor(() -> {
            assertTextNotDisplayed(R.string.no_items_display_forms);
            onView(withIndex(withId(R.id.form_subtitle2), 0)).check(matches(isDisplayed()));
            return this;
        });
    }

    public FillBlankFormPage checkMapIconDisplayedForForm(String formName) {
        onView(withId(R.id.form_list))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(formName)), scrollTo()))
                .check(matches(hasDescendant(allOf(withContentDescription(R.string.open_form_map), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))));
        return this;
    }

    public FillBlankFormPage checkMapIconNotDisplayedForForm(String formName) {
        onView(withId(R.id.form_list))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(formName)), scrollTo()))
                .check(matches(hasDescendant(allOf(withContentDescription(R.string.open_form_map), withEffectiveVisibility(ViewMatchers.Visibility.GONE)))));
        return this;
    }

    public FormMapPage clickOnMapIconForForm(String formName) {
        onView(withId(R.id.form_list))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(formName)), clickOnViewContentDescription(R.string.open_form_map, ApplicationProvider.getApplicationContext())));

        return new FormMapPage(formName).assertOnPage();
    }

    public FormEntryPage clickOnForm(String formName) {
        clickOnFormButton(formName);
        return new FormEntryPage(formName);
    }

    private void clickOnFormButton(String formName) {
        assertFormExists(formName);
        onView(withId(R.id.form_list))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(formName)), click()));
    }

    public FormEndPage clickOnEmptyForm(String formName) {
        clickOnFormButton(formName);
        return new FormEndPage(formName).assertOnPage();
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
        return new ServerAuthDialog().assertOnPage();
    }

    public FillBlankFormPage assertFormExists(String formName) {
        // Seen problems with disk syncing not being waited for even though it's an AsyncTask
        return WaitFor.waitFor(() -> {
            assertTextNotDisplayed(R.string.no_items_display_forms);

            onView(withId(R.id.form_list))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(formName)), scrollTo()));
            return this;
        });
    }

    public FillBlankFormPage assertFormDoesNotExist(String formName) {
        // It seems like `doesNotExist` doesn't work with onData (you get an error that the thing
        // you're looking for doesn't exists)
        onView(withText(formName)).check(doesNotExist());
        return this;
    }

    public FillBlankFormPage assertNoForms() {
        assertText(R.string.no_items_display_forms);
        return this;
    }
}
