package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class CancelRecordingDialog extends Page<CancelRecordingDialog> {

    public CancelRecordingDialog(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public CancelRecordingDialog assertOnPage() {
        onView(withText(getTranslatedString(R.string.recording))).inRoot(isDialog()).check(matches(isDisplayed()));
        return this;
    }

    public <D extends Page<D>> D clickSave(D destination) {
        onView(withText(R.string.save)).inRoot(isDialog()).perform(click());
        return destination.assertOnPage();
    }

    public <D extends Page<D>> D clickKeepRecording(D destination) {
        onView(withText(R.string.cancel)).inRoot(isDialog()).perform(click());
        return destination.assertOnPage();
    }
}
