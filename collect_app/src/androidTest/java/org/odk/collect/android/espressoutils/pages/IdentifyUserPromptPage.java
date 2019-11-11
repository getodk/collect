package org.odk.collect.android.espressoutils.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class IdentifyUserPromptPage extends Page<IdentifyUserPromptPage> {

    private final String formName;

    public IdentifyUserPromptPage(String formName, ActivityTestRule rule) {
        super(rule);
        this.formName = formName;
    }

    @Override
    public IdentifyUserPromptPage assertOnPage() {
        onView(allOf(withText(formName), isDescendantOfA(withId(R.id.toolbar)))).check(matches(isDisplayed()));
        onView(withText(getTranslatedString(R.string.enter_identity))).check(matches(isDisplayed()));
        return this;
    }

    public IdentifyUserPromptPage enterIdentity(String identity) {
        onView(withHint(getTranslatedString(R.string.identity))).perform(replaceText(identity));
        return this;
    }

    public FormEntryPage clickKeyboardEnter() {
        onView(withHint(getTranslatedString(R.string.identity))).perform(pressImeActionButton());
        return new FormEntryPage(formName, rule).assertOnPage();
    }

    public IdentifyUserPromptPage clickKeyboardEnterWithValidationError() {
        onView(withHint(getTranslatedString(R.string.identity))).perform(pressImeActionButton());
        return this.assertOnPage();
    }

    public MainMenuPage pressClose() {
        onView(withContentDescription(getTranslatedString(R.string.close))).perform(click());
        return new MainMenuPage(rule).assertOnPage();
    }
}
