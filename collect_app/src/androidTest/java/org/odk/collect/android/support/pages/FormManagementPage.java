package org.odk.collect.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.not;

import androidx.test.espresso.contrib.RecyclerViewActions;

import org.odk.collect.android.R;

public class FormManagementPage extends Page<FormManagementPage> {

    @Override
    public FormManagementPage assertOnPage() {
        assertToolbarTitle(getTranslatedString(R.string.form_management_preferences));
        return this;
    }

    public ListPreferenceDialog<FormManagementPage> clickUpdateForms() {
        clickOnString(R.string.form_update_mode_title);
        return new ListPreferenceDialog<>(R.string.form_update_mode_title, this).assertOnPage();
    }

    public ListPreferenceDialog<FormManagementPage> clickAutomaticUpdateFrequency() {
        clickOnString(R.string.form_update_frequency_title);
        return new ListPreferenceDialog<>(R.string.form_update_frequency_title, this).assertOnPage();
    }

    public ListPreferenceDialog<FormManagementPage> clickAutoSend() {
        clickOnString(R.string.autosend);
        return new ListPreferenceDialog<>(R.string.autosend, this).assertOnPage();
    }

    public FormManagementPage openShowGuidanceForQuestions() {
        scrollToRecyclerViewItemAndClickText(getTranslatedString(R.string.guidance_hint_title));
        return this;
    }

    public FormManagementPage clickOnDefaultToFinalized() {
        scrollToRecyclerViewItemAndClickText(getTranslatedString(R.string.default_completed));
        return this;
    }

    public FormManagementPage openConstraintProcessing() {
        scrollToRecyclerViewItemAndClickText(getTranslatedString(R.string.constraint_behavior_title));
        return this;
    }

    public FormManagementPage scrollToConstraintProcessing() {
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions
                .actionOnItem(hasDescendant(withText(getTranslatedString(R.string.constraint_behavior_title))), scrollTo()));
        return this;
    }

    public FormManagementPage checkIfConstraintProcessingIsDisabled() {
        onView(withText(getTranslatedString(R.string.constraint_behavior_title))).check(matches(not(isEnabled())));
        return this;
    }
}
