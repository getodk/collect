package org.odk.collect.android.formentry;

import android.Manifest;
import android.widget.RadioButton;

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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagKey;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.odk.collect.android.test.CustomMatchers.withIndex;

public class LikertTest {
    // having trouble getting correct labels, check the xslx and likert_test.xml file
    /* Test Cases:
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
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).perform(click());
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).check(matches(isChecked()));
    }

    @Test
    public void allImages_canClick() {
        openWidgetList();
        onView(withText("Likert Image Widget")).perform(click());
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).perform(click());
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).check(matches(isChecked()));
    }

    @Test
    public void insufficientText_canClick() {
        openWidgetList();
        onView(withText("Likert Widget Error")).perform(click());
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).perform(click());
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).check(matches(isChecked()));
    }

    @Test
    public void insufficientImages_canClick() {
        openWidgetList();
        onView(withText("Likert Image Widget Error")).perform(click());
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).perform(click());
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).check(matches(isChecked()));
    }

    @Test
    public void missingImage_canClick() {
        openWidgetList();
        onView(withText("Likert Image Widget Error2")).perform(click());
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).perform(click());
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).check(matches(isChecked()));
    }

    @Test
    public void missingText_canClick() {
        openWidgetList();
        onView(withText("Likert Missing text Error")).perform(click());
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).perform(click());
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).check(matches(isChecked()));
    }

    @Test
    public void onlyOneRemainsClicked() {
        openWidgetList();
        onView(withText("Likert Image Widget")).perform(click());
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).perform(click());
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).check(matches(isChecked()));
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(3)))).perform(click());
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(3)))).check(matches(isChecked()));
        onView(allOf(withClassName(endsWith("RadioButton")), withTagValue(equalTo(1)))).check(matches(isNotChecked()));
    }

    @Test
    public void testImagesLoad() {
        openWidgetList();
        onView(withText("Likert Image Widget")).perform(click());

        for (int i = 0; i < 5; i++) {
            onView(allOf(withClassName(endsWith("ImageView")), withTagValue(equalTo(i)))).check(matches(isDisplayed()));
        }
    }

//    @Test
//    public void selectionChangeAtOneCascadeLevelWithMinimalAppearance_ShouldUpdateNextLevels() {
//        openWidgetList();
//        onView(withText("Cascading likert")).perform(click());
//        onView(withText(startsWith("Level1"))).perform(click());
//
//        // No choices should be shown for levels 2 and 3 when no selection is made for level 1
//        onView(withText("A1")).check(doesNotExist());
//        onView(withText("B1")).check(doesNotExist());
//        onView(withText("C1")).check(doesNotExist());
//        onView(withText("A1A")).check(doesNotExist());
//
//        // Selecting C for level 1 should only reveal options for C at level 2
//        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
//        onView(withText("C")).perform(click());
//        onView(withText("A1")).check(doesNotExist());
//        onView(withText("B1")).check(doesNotExist());
//        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
//        onView(withText("C1")).perform(click());
//        onView(withText("A1A")).check(doesNotExist());
//
//        // Selecting A for level 1 should reveal options for A at level 2
//        onView(withText("C")).perform(click());
//        onView(withText("A")).perform(click());
//        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
//        onView(withText("A1")).check(matches(isDisplayed()));
//        onView(withText("A1A")).check(doesNotExist());
//        onView(withText("B1")).check(doesNotExist());
//        onView(withText("C1")).check(doesNotExist());
//
//        // Selecting A1 for level 2 should reveal options for A1 at level 3
//        onView(withText("A1")).perform(click());
//        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
//        onView(withText("A1A")).check(matches(isDisplayed()));
//        onView(withText("B1A")).check(doesNotExist());
//        onView(withText("B1")).check(doesNotExist());
//        onView(withText("C1")).check(doesNotExist());
//    }

    private void openWidgetList() {
        onView(withId(R.id.menu_goto)).perform(click());
    }
}
