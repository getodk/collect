package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.test.FormLoadingUtils;

import java.util.Collections;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.endsWith;

public class ExternalCsvSearchTest {
    private static final String EXTERNAL_CSV_SEARCH_FORM = "external-csv-search.xml";

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(EXTERNAL_CSV_SEARCH_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule(EXTERNAL_CSV_SEARCH_FORM, Collections.singletonList("external-csv-search-produce.csv")));

    @Test
    public void simpleSearchStatement_ShouldDisplayAllCsvChoices() {
        onView(withText("Multiple produce")).check(matches(isDisplayed()));

        onView(withText("Artichoke")).check(matches(isDisplayed()));
        onView(withText("Apple")).check(matches(isDisplayed()));
        onView(withText("Banana")).check(matches(isDisplayed()));
        onView(withText("Blueberry")).check(matches(isDisplayed()));
        onView(withText("Cherimoya")).check(matches(isDisplayed()));
        onView(withText("Carrot")).check(matches(isDisplayed()));
    }

    @Test
    // Regression: https://github.com/opendatakit/collect/issues/3132
    public void searchStatementWithContainsFilter_ShouldUpdateOnSearchChange() {
        onView(withText("Multiple produce")).perform(swipeLeft());

        onView(withText("Produce search")).check(matches(isDisplayed()));
        onView(withClassName(endsWith("EditText"))).perform(replaceText("A"));
        onView(withText("Produce search")).perform(swipeLeft());
        onView(withText("Artichoke")).check(matches(isDisplayed()));
        onView(withText("Apple")).check(matches(isDisplayed()));
        onView(withText("Banana")).check(matches(isDisplayed()));
        onView(withText("Blueberry")).check(doesNotExist());
        onView(withText("Cherimoya")).check(matches(isDisplayed()));
        onView(withText("Carrot")).check(matches(isDisplayed()));

        onView(withText("Produce")).perform(swipeRight());
        onView(withClassName(endsWith("EditText"))).perform(replaceText("B"));
        onView(withText("Produce search")).perform(swipeLeft());
        onView(withText("Artichoke")).check(doesNotExist());
        onView(withText("Apple")).check(doesNotExist());
        onView(withText("Banana")).check(matches(isDisplayed()));
        onView(withText("Blueberry")).check(matches(isDisplayed()));
        onView(withText("Cherimoya")).check(doesNotExist());
        onView(withText("Carrot")).check(doesNotExist());
    }
}
