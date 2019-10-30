package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.test.FormLoadingUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class LikertTest {
    // having trouble getting correct labels, check the xslx and likert_test.xml file
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
    public void longLabelsShouldBeDisplayed() {
        openWidgetList();
        onView(withText("Likert Image Widget")).perform(click());
    }

    private void openWidgetList() {
        onView(withId(R.id.menu_goto)).perform(click());
    }
}
