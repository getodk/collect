package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.test.FormLoadingUtils;

import java.util.Collections;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class LikertTest {
    // having trouble getting correct labels, check the xslx and likert_test.xml file
    /* Test Cases:
     * (1) Interacting with the different widgets
     * (2) Loading images
     * (3) FieldList Update
     */
    private static final String LIKERT_TEST_FORM = "likert_test.xml";

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(LIKERT_TEST_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule(LIKERT_TEST_FORM, Collections.singletonList("famous.jpg")));

    @Test
    public void allText_canClick() {
        openWidgetList();
        onView(withText("Likert Widget")).perform(click());
        onView(withId(1)).perform(click());
        onView(withId(1)).check(matches(isChecked()));
    }

    @Test
    public void allImages_canClick() {
        openWidgetList();
        onView(withText("Likert Image Widget")).perform(click());
        onView(withId(1)).perform(click());
        onView(withId(1)).check(matches(isChecked()));
    }

    @Test
    public void insufficientText_canClick() {
        openWidgetList();
        onView(withText("Likert Widget Error")).perform(click());
        onView(withId(1)).perform(click());
        onView(withId(1)).check(matches(isChecked()));
    }

    @Test
    public void insufficientImages_canClick() {
        openWidgetList();
        onView(withText("Likert Image Widget Error")).perform(click());
        onView(withId(1)).perform(click());
        onView(withId(1)).check(matches(isChecked()));
    }

    @Test
    public void missingImage_canClick() {
        openWidgetList();
        onView(withText("Likert Image Widget Error2")).perform(click());
        onView(withId(1)).perform(click());
        onView(withId(1)).check(matches(isChecked()));
    }

    @Test
    public void missingText_canClick() {
        openWidgetList();
        onView(withText("Likert Missing text Error")).perform(click());
        onView(withId(1)).perform(click());
        onView(withId(1)).check(matches(isChecked()));
    }

    @Test
    public void onlyOneRemainsClicked() {
        openWidgetList();
        onView(withText("Likert Image Widget")).perform(click());
        onView(withId(1)).perform(click());
        onView(withId(1)).check(matches(isChecked()));
        onView(withId(3)).perform(click());
        onView(withId(3)).check(matches(isChecked()));
        onView(withId(1)).check(matches(isNotChecked()));
    }

    private void openWidgetList() {
        onView(withId(R.id.menu_goto)).perform(click());
    }
}
