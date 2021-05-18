package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class AddNewRepeatDialog extends Page<AddNewRepeatDialog> {

    private final String repeatName;

    public AddNewRepeatDialog(String repeatName) {
        this.repeatName = repeatName;
    }

    @Override
    public AddNewRepeatDialog assertOnPage() {
        onView(withText(getTranslatedString(R.string.add_repeat_question, repeatName))).check(matches(isDisplayed()));
        return this;
    }

    public <D extends Page<D>> D clickOnAdd(D destination) {
        clickOnString(R.string.add_repeat);
        return destination;
    }

    public <D extends Page<D>> D clickOnDoNotAdd(D destination) {
        clickOnString(R.string.dont_add_repeat);
        return destination;
    }

}
