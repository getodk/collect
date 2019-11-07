package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.pages.FormEntryPage;
import org.odk.collect.android.espressoutils.pages.MainMenuPage;
import org.odk.collect.android.regression.BaseRegressionTest;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.matchers.RecyclerViewMatcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.odk.collect.android.support.matchers.RecyclerViewMatcher.withRecyclerView;

public class FormHierarchyTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("formHierarchy1.xml", null))
            .around(new CopyFormRule("formHierarchy2.xml", null))
            .around(new CopyFormRule("formHierarchy3.xml", null));

    @Test
    //https://github.com/opendatakit/collect/issues/2871
    public void allRelevantQuestionsShouldBeVisibleInHierarchyView() {
        new MainMenuPage(main)
                .startBlankForm("formHierarchy1")
                .clickGoToIconInForm();

        onView(withRecyclerView(R.id.list)
                .atPositionOnView(0, R.id.primary_text))
                .check(matches(withText("what is your name?")));

        onView(withRecyclerView(R.id.list)
                .atPositionOnView(1, R.id.primary_text))
                .check(matches(withText("what is your age?")));
    }

    @Test
    //https://github.com/opendatakit/collect/issues/2944
    public void notRelevantRepeatGroupsShouldNotBeVisibleInHierarchy() {
        final FormEntryPage page = new MainMenuPage(main)
                .startBlankForm("formHierarchy2")
                .inputText("2")
                .clickGoToIconInForm();

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(0, R.id.primary_text))
                .check(matches(withText("How many guests are in your party?")));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(0, R.id.secondary_text))
                .check(matches(withText("2")));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(1, R.id.primary_text))
                .check(matches(withText("Please provide details for each guest.")));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(2, R.id.primary_text))
                .check(matches(withText("Guest details")));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(2, R.id.secondary_text))
                .check(matches(withText("Repeatable Group")));

        page.clickOnText("Guest details");

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(2)));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(0, R.id.primary_text))
                .check(matches(withText("Guest details > 1")));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(1, R.id.primary_text))
                .check(matches(withText("Guest details > 2")));

        page.clickJumpStartButton()
                .inputText("1")
                .clickGoToIconInForm()
                .clickOnText("Guest details");

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(1)));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(0, R.id.primary_text))
                .check(matches(withText("Guest details > 1")));
    }

    @Test
    //https://github.com/opendatakit/collect/issues/2936
    public void repeatGroupsShouldBeVisibleAsAppropriate() {
        FormEntryPage page = new MainMenuPage(main)
                .startBlankForm("formHierarchy3")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickOnString(R.string.add_another)
                .swipeToNextQuestion()
                .clickOnString(R.string.add_another)
                .swipeToNextQuestion()
                .clickOnString(R.string.add_repeat_no)
                .clickOnString(R.string.add_repeat_no)
                .clickGoToIconInForm();

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));

        page.clickOnText("Group 1");

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));

        page.checkIfTextDoesNotExist("Repeat Group 1");
    }

    @Test
    //https://github.com/opendatakit/collect/issues/2942
    public void deletingLastGroupShouldNotBreakHierarchy() {
        FormEntryPage page = new MainMenuPage(main)
                .startBlankForm("formHierarchy3")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickOnString(R.string.add_another)
                .swipeToNextQuestion()
                .clickOnString(R.string.add_another)
                .swipeToNextQuestion()
                .clickOnString(R.string.add_another)
                .swipeToNextQuestion()
                .clickOnString(R.string.add_repeat_no)
                .clickOnString(R.string.add_repeat_no)
                .clickGoToIconInForm()
                .clickOnText("Repeat Group 1")
                .clickOnText("Repeat Group 1 > 1")
                .clickOnText("Repeat Group 1_1")
                .clickOnText("Repeat Group 1_1 > 2")
                .deleteGroup();

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(1)));

        page.checkIsTextDisplayed("Repeat Group 1_1 > 1");
    }
}
