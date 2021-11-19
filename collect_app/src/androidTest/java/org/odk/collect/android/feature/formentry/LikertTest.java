package org.odk.collect.android.feature.formentry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.odk.collect.android.support.CustomMatchers.withIndex;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.FormActivityTestRule;
import org.odk.collect.android.support.ResetStateRule;

import java.util.Collections;

public class LikertTest {
    private static final String LIKERT_TEST_FORM = "likert_test.xml";

    @Rule
    public FormActivityTestRule activityTestRule = new FormActivityTestRule(LIKERT_TEST_FORM, "All widgets likert icon");

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.CAMERA))
            .around(new ResetStateRule())
            .around(new CopyFormRule(LIKERT_TEST_FORM, Collections.singletonList("famous.jpg"), true));

    @Test
    public void allText_canClick() {
        openWidgetList();
        onView(withText("Likert Widget")).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isChecked()));
    }

    @Test
    public void allImages_canClick() {
        openWidgetList();
        onView(withText("Likert Image Widget")).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isChecked()));
    }

    @Test
    public void insufficientText_canClick() {
        openWidgetList();
        onView(withText("Likert Widget Error")).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isChecked()));
    }

    @Test
    public void insufficientImages_canClick() {
        openWidgetList();
        onView(withText("Likert Image Widget Error")).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isChecked()));
    }

    @Test
    public void missingImage_canClick() {
        openWidgetList();
        onView(withText("Likert Image Widget Error2")).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isChecked()));
    }

    @Test
    public void missingText_canClick() {
        openWidgetList();
        onView(withText("Likert Missing text Error")).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isChecked()));
    }

    @Test
    public void onlyOneRemainsClicked() {
        openWidgetList();
        onView(withText("Likert Image Widget")).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isChecked()));
        onView(withIndex(withClassName(endsWith("RadioButton")), 2)).perform(click());
        onView(withIndex(withClassName(endsWith("RadioButton")), 2)).check(matches(isChecked()));
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isNotChecked()));
    }

    @Test
    public void testImagesLoad() {
        openWidgetList();
        onView(withText("Likert Image Widget")).perform(click());

        for (int i = 0; i < 5; i++) {
            onView(withIndex(withClassName(endsWith("RadioButton")), i)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void updateTest_SelectionChangeAtOneCascadeLevelWithLikert_ShouldUpdateNextLevels() {
        openWidgetList();
        onView(withText("Cascading likert")).perform(click());

        // No choices should be shown for levels 2 and 3 when no selection is made for level 1
        onView(withText(startsWith("Level1"))).perform(click());
        onView(withText("A1")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());
        onView(withText("A1A")).check(doesNotExist());

        // Selecting C for level 1 should only reveal options for C at level 2
        // and selecting C3 for level 2 shouldn't reveal options in level 3
        onView(withIndex(withClassName(endsWith("RadioButton")), 2)).perform(click());
        onView(withText("C1")).check(matches(isDisplayed()));
        onView(withText("C4")).check(matches(isDisplayed()));
        onView(withText("A1")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withIndex(withClassName(endsWith("RadioButton")), 5)).perform(click());
        onView(withText("A1A")).check(doesNotExist());

        // Selecting A for level 1 should reveal options for A at level 2
        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(withText("A1")).check(matches(isDisplayed()));
        onView(withText("A1A")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());

        // Selecting A1 for level 2 should reveal options for A1 at level 3
        onView(withIndex(withClassName(endsWith("RadioButton")), 3)).perform(click());
        onView(withText("A1A")).check(matches(isDisplayed()));
        onView(withText("B1A")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());
    }

    private void openWidgetList() {
        onView(withId(R.id.menu_goto)).perform(click());
    }
}
