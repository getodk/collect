package org.odk.collect.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class AddNewRepeatDialog extends Page<AddNewRepeatDialog> {

    private final String repeatName;

    public AddNewRepeatDialog(String repeatName) {
        this.repeatName = repeatName;
    }

    @Override
    public AddNewRepeatDialog assertOnPage() {
        String dialogMessage;
        if (repeatName.isBlank()) {
            dialogMessage = getTranslatedString(org.odk.collect.strings.R.string.add_another_question);
        } else {
            dialogMessage = getTranslatedString(org.odk.collect.strings.R.string.add_repeat_question, repeatName);
        }
        onView(withText(dialogMessage))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        return this;
    }

    public <D extends Page<D>> D clickOnAdd(D destination) {
        return clickOnTextInDialog(org.odk.collect.strings.R.string.add_repeat, destination);
    }

    public <D extends Page<D>> D clickOnDoNotAdd(D destination) {
        return clickOnTextInDialog(org.odk.collect.strings.R.string.cancel, destination);
    }

}
