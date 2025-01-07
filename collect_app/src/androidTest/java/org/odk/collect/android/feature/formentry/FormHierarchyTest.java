package org.odk.collect.android.feature.formentry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.odk.collect.testshared.RecyclerViewMatcher.withRecyclerView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.testshared.RecyclerViewMatcher;
import org.odk.collect.android.support.pages.AddNewRepeatDialog;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.FormHierarchyPage;

public class FormHierarchyTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    //https://github.com/getodk/collect/issues/2871
    public void allRelevantQuestionsShouldBeVisibleInHierarchyView() {
        rule.startAtMainMenu()
                .copyForm("formHierarchy1.xml")
                .startBlankForm("formHierarchy1")
                .clickGoToArrow();

        onView(withRecyclerView(R.id.list)
                .atPositionOnView(0, R.id.primary_text))
                .check(matches(withText("what is your name?")));

        onView(withRecyclerView(R.id.list)
                .atPositionOnView(1, R.id.primary_text))
                .check(matches(withText("what is your age?")));
    }

    @Test
    //https://github.com/getodk/collect/issues/2944
    public void notRelevantRepeatGroupsShouldNotBeVisibleInHierarchy() {
        final FormHierarchyPage page = rule.startAtMainMenu()
                .copyForm("formHierarchy2.xml")
                .startBlankForm("formHierarchy2")
                .inputText("2")
                .clickGoToArrow();

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
                .atPositionOnView(2, R.id.group_label))
                .check(matches(withText("Repeatable Group")));

        page.clickOnText("Guest details");

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(2)));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(0, R.id.primary_text))
                .check(matches(withText("Guest details > 1")));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(1, R.id.primary_text))
                .check(matches(withText("Guest details > 2")));

        page.clickGoToStart()
                .inputText("1")
                .clickGoToArrow()
                .clickOnText("Guest details");

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(1)));
        onView(withRecyclerView(R.id.list)
                .atPositionOnView(0, R.id.primary_text))
                .check(matches(withText("Guest details > 1")));
    }

    @Test
    //https://github.com/getodk/collect/issues/2936
    public void repeatGroupsShouldBeVisibleAsAppropriate() {
        FormHierarchyPage page = rule.startAtMainMenu()
                .copyForm("formHierarchy3.xml")
                .startBlankForm("formHierarchy3")
                .assertQuestion("Intro")
                .swipeToNextQuestion("Text")
                .swipeToNextQuestion("Integer 1_1")
                .swipeToNextQuestion("Integer 1_2")
                .swipeToNextQuestion("Integer 2_1")
                .swipeToNextQuestion("Integer 2_2")
                .swipeToNextQuestionWithRepeatGroup("Repeat Group 1")
                .clickOnAdd(new FormEntryPage("formHierarchy3"))
                .swipeToNextQuestionWithRepeatGroup("Repeat Group 1_1")
                .clickOnAdd(new FormEntryPage("formHierarchy3"))
                .swipeToNextQuestionWithRepeatGroup("Repeat Group 1_1")
                .clickOnDoNotAdd(new AddNewRepeatDialog("Repeat Group 1"))
                .clickOnDoNotAdd(new FormEntryPage("formHierarchy3"))
                .clickGoToArrow();

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));

        page.clickOnText("Group 1");

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(3)));

        page.assertTextDoesNotExist("Repeat Group 1");
    }

    @Test
    //https://github.com/getodk/collect/issues/2942
    public void deletingLastGroupShouldNotBreakHierarchy() {
        FormHierarchyPage page = rule.startAtMainMenu()
                .copyForm("formHierarchy3.xml")
                .startBlankForm("formHierarchy3")
                .swipeToNextQuestion("Text")
                .swipeToNextQuestion("Integer 1_1")
                .swipeToNextQuestion("Integer 1_2")
                .swipeToNextQuestion("Integer 2_1")
                .swipeToNextQuestion("Integer 2_2")
                .swipeToNextQuestionWithRepeatGroup("Repeat Group 1")
                .clickOnAdd(new FormEntryPage("formHierarchy3"))
                .assertQuestion("Barcode")
                .swipeToNextQuestionWithRepeatGroup("Repeat Group 1_1")
                .clickOnAdd(new FormEntryPage("formHierarchy3"))
                .assertQuestion("Date")
                .swipeToNextQuestionWithRepeatGroup("Repeat Group 1_1")
                .clickOnAdd(new FormEntryPage("formHierarchy3"))
                .assertQuestion("Date")
                .swipeToNextQuestionWithRepeatGroup("Repeat Group 1_1")
                .clickOnDoNotAdd(new AddNewRepeatDialog("Repeat Group 1"))
                .clickOnDoNotAdd(new FormEntryPage("formHierarchy3"))
                .clickGoToArrow()
                .clickOnText("Repeat Group 1")
                .clickOnText("Repeat Group 1 > 1")
                .clickOnText("Repeat Group 1_1")
                .clickOnText("Repeat Group 1_1 > 2")
                .deleteGroup();

        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(1)));

        page.assertText("Repeat Group 1_1 > 1");
    }

    @Test
    //https://github.com/getodk/collect/issues/3971
    public void deletingLastGroupAndAddingOneShouldNotBreakHierarchy() {
        rule.startAtMainMenu()
                .copyForm("repeat_group_new.xml")
                .startBlankFormWithRepeatGroup("RepeatGroupNew", "People")
                .clickOnAdd(new FormEntryPage("RepeatGroupNew"))
                .assertQuestion("Name")
                .swipeToNextQuestion("Age")
                .swipeToNextQuestionWithRepeatGroup("People")
                .clickOnAdd(new FormEntryPage("RepeatGroupNew"))
                .assertQuestion("Name")
                .swipeToNextQuestion("Age")
                .swipeToNextQuestionWithRepeatGroup("People")
                .clickOnAdd(new FormEntryPage("RepeatGroupNew"))
                .clickGoToArrow()
                .deleteGroup()
                .addGroup();
    }

    @Test
    //https://github.com/getodk/collect/issues/4570
    public void showRepeatsPickerWhenFirstRepeatIsEmpty() {
        rule.startAtMainMenu()
                .copyForm("Empty First Repeat.xml")
                .startBlankFormWithRepeatGroup("Empty First Repeat", "Repeat")
                .clickOnAdd(new FormEntryPage("Empty First Repeat"))
                .answerQuestion("Question in repeat", "Not empty!")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickGoUpIcon()
                .assertTexts("Repeat", "Repeatable Group");
    }

    @Test
    //https://github.com/getodk/collect/issues/6015
    public void regularGroupThatWrapsARepeatableGroupShouldBeTreatedAsARegularOne() {
        rule.startAtMainMenu()
                .copyForm("repeat_group_wrapped_with_a_regular_group.xml")
                .startBlankForm("Repeat group wrapped with a regular group")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickGoUpIcon()
                .assertPath("Outer")
                .assertNotRemovableGroup();
    }

    @Test
    public void when_openHierarchyViewFromLastPage_should_mainGroupViewBeVisible() {
        rule.startAtMainMenu()
                .copyForm("repeat_group_form.xml")
                .startBlankFormWithRepeatGroup("Repeat Group", "Grp1")
                .clickOnDoNotAdd(new FormEntryPage("Repeat Group"))
                .clickGoToArrow()
                .clickJumpEndButton()
                .clickGoToArrow()
                .checkIfElementInHierarchyMatchesToText("Group Name", 0);
    }

    @Test
    public void hierachyView_shouldNotChangeAfterScreenRotation() {
        rule.startAtMainMenu()
                .copyForm("repeat_group_form.xml")
                .startBlankFormWithRepeatGroup("Repeat Group", "Grp1")
                .clickOnDoNotAdd(new FormEntryPage("Repeat Group"))
                .clickGoToArrow()
                .clickGoUpIcon()
                .checkIfElementInHierarchyMatchesToText("Group Name", 0)
                .rotateToLandscape(new FormHierarchyPage("Repeat Group"))
                .checkIfElementInHierarchyMatchesToText("Group Name", 0);
    }

    @Test
    public void theListOfQuestionsShouldBeScrolledToTheLastDisplayedQuestionAfterOpeningTheHierarchy() {
        rule.startAtMainMenu()
                .copyForm("manyQ.xml")
                .startBlankForm("manyQ")
                .swipeToNextQuestion("t2")
                .swipeToNextQuestion("n1")
                .clickGoToArrow()
                .assertText("n1")
                .assertTextDoesNotExist("t1")
                .assertTextDoesNotExist("t2");
    }
}
