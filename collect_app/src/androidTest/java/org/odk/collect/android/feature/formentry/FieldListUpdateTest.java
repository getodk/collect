/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.feature.formentry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove;
import static androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasFocus;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.android.gms.common.util.CollectionUtils.listOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.odk.collect.android.support.matchers.CustomMatchers.withIndex;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.GuidanceHint;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.rules.FormEntryActivityTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.androidtest.RecordedIntentsRule;
import org.odk.collect.settings.keys.ProjectKeys;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public class FieldListUpdateTest {
    public FormEntryActivityTestRule rule = new FormEntryActivityTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(new RecordedIntentsRule())
            .around(rule);

    @Test
    public void relevanceChangeAtEnd_ShouldToggleLastWidgetVisibility() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
                .assertTextDoesNotExist("Target1")
                .answerQuestion("Source1", "A")
                .assertQuestion("Target1")
                .assertQuestionsOrder("Source1", "Target1")
                .answerQuestion("Source1", "")
                .assertTextDoesNotExist("Target1");
    }

    @Test
    public void relevanceChangeAtBeginning_ShouldToggleFirstWidgetVisibility() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnGroup("Single relevance at beginning")
                .clickOnQuestion("Source2")
                .assertTextDoesNotExist("Target2")
                .answerQuestion("Source2", "A")
                .assertQuestion("Target2")
                .assertQuestionsOrder("Target2", "Source2")
                .answerQuestion("Source2", "")
                .assertTextDoesNotExist("Target2");
    }

    @Test
    public void relevanceChangeInMiddle_ShouldToggleMiddleWidgetVisibility() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates");

        jumpToGroupWithText("Single relevance in middle");
        onView(withText("Source3")).perform(click());

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Target3")).check(doesNotExist());
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText("A"));
        onView(withText("Target3")).check(matches(isDisplayed()));
        onView(withText("Target3")).check(isCompletelyBelow(withText("Source3")));
        onView(withText("Target3")).check(isCompletelyAbove(withText("Filler3")));

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Target3")).check(doesNotExist());
    }

    @Test
    public void longPress_ShouldClearAndUpdate() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnGroup("Single relevance in middle")
                .clickOnQuestion("Source3")
                .answerQuestion(0, "")
                .assertTextDoesNotExist("Target3")
                .answerQuestion(0, "A")
                .assertText("Target3")

                .longPressOnQuestion("Source3")
                .removeResponse()
                .assertTextDoesNotExist("A")
                .assertTextDoesNotExist("Target3");
    }

    @Test
    public void changeInValueUsedInLabel_ShouldChangeLabelText() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates");

        jumpToGroupWithText("Label change");
        onView(withText(startsWith("Hello"))).perform(click());

        String name = UUID.randomUUID().toString();

        onView(withIndex(withClassName(endsWith("EditText")), 1)).perform(replaceText(""));
        onView(withText("Hello, , how are you today?")).check(matches(isDisplayed()));
        onView(withIndex(withClassName(endsWith("EditText")), 1)).perform(replaceText(name));
        onView(withText("Hello, " + name + ", how are you today?")).check(matches(isDisplayed()));
        onView(withIndex(withClassName(endsWith("EditText")), 1)).perform(replaceText(""));
        onView(withText("Hello, , how are you today?")).check(matches(isDisplayed()));
    }

    @Test
    public void changeInValueUsedInHint_ShouldChangeHintText() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates");

        jumpToGroupWithText("Hint change");
        onView(withText(startsWith("What is your"))).perform(click());

        String name = UUID.randomUUID().toString();

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Please don't use your calculator, !")).check(matches(isDisplayed()));
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(name));
        onView(withText("Please don't use your calculator, " + name + "!")).check(matches(isDisplayed()));
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Please don't use your calculator, !")).check(matches(isDisplayed()));
    }


    @Test
    public void changeInValueUsedInOtherField_ShouldChangeValue() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates");

        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.menu_go_up)).perform(click());
        onView(allOf(withText("Value change"), isDisplayed())).perform(click());
        onView(withText(startsWith("What is your"))).perform(click());

        String name = UUID.randomUUID().toString();

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withIndex(withClassName(endsWith("EditText")), 1)).check(matches(withText("0")));
        onView(withIndex(withClassName(endsWith("EditText")), 2)).check(matches(withText("")));
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(name));
        onView(withIndex(withClassName(endsWith("EditText")), 1)).check(matches(withText(String.valueOf(name.length()))));
        onView(withIndex(withClassName(endsWith("EditText")), 2)).check(matches(withText(String.valueOf(name.charAt(0)))));
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withIndex(withClassName(endsWith("EditText")), 1)).check(matches(withText("0")));
        onView(withIndex(withClassName(endsWith("EditText")), 2)).check(matches(withText("")));
    }

    @Test
    public void selectionChangeAtFirstCascadeLevel_ShouldUpdateNextLevels() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates");

        jumpToGroupWithText("Cascading select");
        onView(withText(startsWith("Level1"))).perform(click());

        // No choices should be shown for levels 2 and 3 when no selection is made for level 1
        onView(withText("A1")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());
        onView(withText("A1A")).check(doesNotExist());

        // Selecting C for level 1 should only reveal options for C at level 2
        onView(withText("C")).perform(click());
        onView(withText("A1")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(matches(isDisplayed()));
        onView(withText("A1A")).check(doesNotExist());

        // Selecting A for level 1 should reveal options for A at level 2
        onView(withText("A")).perform(click());
        onView(withText("A1")).check(matches(isDisplayed()));
        onView(withText("A1A")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());

        // Selecting A1 for level 2 should reveal options for A1 at level 3
        onView(withText("A1")).perform(click());
        onView(withText("A1A")).perform(scrollTo());
        onView(withText("A1A")).check(matches(isDisplayed()));
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());
    }

    @Test
    public void clearingParentSelect_ShouldUpdateAllDependentLevels() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates");

        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.menu_go_up)).perform(click());
        onView(allOf(withText("Cascading select"), isDisplayed())).perform(click());
        onView(withText(startsWith("Level1"))).perform(click());

        onView(withText("A")).perform(click());

        onView(withText("A1")).perform(scrollTo(), click());
        onView(withText("A1B")).perform(scrollTo(), click());

        onView(withText("Level1")).perform(scrollTo(), longClick());
        onView(withText(org.odk.collect.strings.R.string.clear_answer)).perform(click());
        onView(withText(org.odk.collect.strings.R.string.discard_answer)).perform(click());

        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isNotChecked()));
        onView(withText("A1")).check(doesNotExist());
        onView(withText("A1B")).check(doesNotExist());
    }

    @Test
    public void selectionChangeAtOneCascadeLevelWithMinimalAppearance_ShouldUpdateNextLevels() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates");

        new FormEntryPage("fieldlist-updates")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnGroup("Cascading select minimal")
                .clickOnQuestion("Level1")
                .assertTextsDoNotExist("A1", "B1", "C1", "A1A") // No choices should be shown for levels 2 and 3 when no selection is made for level 1
                .openSelectMinimalDialog(0)
                .selectItem("C") // Selecting C for level 1 should only reveal options for C at level 2
                .assertTextsDoNotExist("A1", "B1")
                .openSelectMinimalDialog(1)
                .selectItem("C1")
                .assertTextDoesNotExist("A1A")
                .clickOnText("C")
                .clickOnText("A") // Selecting A for level 1 should reveal options for A at level 2
                .openSelectMinimalDialog(1)
                .assertText("A1")
                .assertTextsDoNotExist("A1A", "B1", "C1")
                .selectItem("A1") // Selecting A1 for level 2 should reveal options for A1 at level 3
                .openSelectMinimalDialog(2)
                .assertText("A1A")
                .assertTextsDoNotExist("B1A", "B1", "C1");
    }

    @Test
    public void questionsAppearingBeforeCurrentTextQuestion_ShouldNotChangeFocus() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates");

        jumpToGroupWithText("Push off screen");
        onView(withText(startsWith("Source9"))).perform(click());

        onView(withText("Target9-15")).check(doesNotExist());
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText("A"));

        onView(withText("Target9-15")).check(matches(isDisplayed()));

        onView(withIndex(withClassName(endsWith("EditText")), 15)).check(matches(isCompletelyDisplayed()));
        onView(withIndex(withClassName(endsWith("EditText")), 15)).check(matches(hasFocus()));
    }

    @Test
    public void questionsAppearingBeforeCurrentBinaryQuestion_ShouldNotChangeFocus() throws IOException {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates");

        jumpToGroupWithText("Push off screen binary");
        onView(withText(startsWith("Source10"))).perform(click());

        onView(withText("Target10-15")).check(doesNotExist());

        // FormFillingActivity expects an image at a fixed path so copy the app logo there
        Bitmap icon = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.drawable.notes);
        File tmpJpg = new File(new StoragePathProvider().getTmpImageFilePath());
        tmpJpg.createNewFile();
        FileOutputStream fos = new FileOutputStream(tmpJpg);
        icon.compress(Bitmap.CompressFormat.JPEG, 90, fos);

        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        onView(withId(R.id.capture_button)).perform(click());

        onView(withText("Target10-15")).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.capture_button)).check(matches(isCompletelyDisplayed()));
    }

    @Test
    public void changeInValueUsedInGuidanceHint_ShouldChangeGuidanceHintText() {
        FormEntryPage page = rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates");

        DaggerUtils.getComponent(ApplicationProvider.<Application>getApplicationContext())
                .settingsProvider()
                .getUnprotectedSettings()
                .save(ProjectKeys.KEY_GUIDANCE_HINT, GuidanceHint.YES.toString());

        page.clickGoToArrow()
                .clickGoUpIcon()
                .clickOnGroup("Guidance hint")
                .clickOnQuestion("Source11")
                .assertTextDoesNotExist("10")
                .answerQuestion("Source11", "5")
                .assertQuestion("10");
    }

    @Test
    public void selectingADateForDateTime_ShouldChangeRelevanceOfRelatedField() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnGroup("Date time")
                .clickOnQuestion("Source12")
                .assertTextDoesNotExist("Target12")
                .clickOnString(org.odk.collect.strings.R.string.select_date)
                .clickOKOnDialog()
                .assertQuestion("Target12");
    }

    @Test
    public void selectingARating_ShouldChangeRelevanceOfRelatedField() throws Exception {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnGroup("Rating")
                .clickOnQuestion("Source13")
                .assertTextDoesNotExist("Target13")
                .setRating(3.0f)
                .assertQuestion("Target13")
                .longPressOnQuestion("Source13")
                .removeResponse()
                .assertTextDoesNotExist("Target13");
    }

    @Test
    public void manuallySelectingAValueForMissingExternalApp_ShouldTriggerUpdate() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnGroup("External app")
                .clickOnQuestion("Source14")
                .clickOnText("Launch")
                .assertTextDoesNotExist("Target14")
                .answerQuestion("Source14", String.valueOf(new Random().nextInt()))
                .assertQuestion("Target14");
    }

    @Test
    public void searchMinimalInFieldList() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnGroup("Search in field-list")
                .clickOnQuestion("Source15")
                .openSelectMinimalDialog()
                .assertTexts("Mango", "Oranges", "Strawberries")
                .selectItem("Strawberries")
                .assertText("Target15")
                .assertSelectMinimalDialogAnswer("Strawberries");
    }

    @Test
    public void listOfQuestionsShouldNotBeScrolledToTheLastEditedQuestionAfterClickingOnAQuestion() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnGroup("Long list of questions")
                .clickOnQuestion("Question1")
                .answerQuestion(0, "X")
                .clickOnQuestionField("Question20")
                .assertText("Question20");
    }

    @Test
    public void recordingAudio_ShouldChangeRelevanceOfRelatedField() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnGroup("Audio")
                .clickOnQuestion("Source16")
                .assertTextDoesNotExist("Target16")
                .clickOnString(org.odk.collect.strings.R.string.capture_audio)
                .clickOnContentDescription(org.odk.collect.strings.R.string.stop_recording)
                .assertText("Target16")
                .clickOnString(org.odk.collect.strings.R.string.delete_answer_file)
                .clickOnTextInDialog(org.odk.collect.strings.R.string.delete_answer_file, new FormEntryPage("fieldlist-updates"))
                .assertTextDoesNotExist("Target16");
    }

    @Test
    public void changeInValueUsedToDetermineIfAQuestionIsRequired_ShouldUpdateTheRelatedRequiredQuestion() {
        rule.setUpProjectAndCopyForm("fieldlist-updates.xml", listOf("fruits.csv"))
                .fillNewForm("fieldlist-updates.xml", "fieldlist-updates")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnGroup("Dynamic required question")
                .clickOnQuestion("Source17")
                .assertQuestion("Target17")
                .answerQuestion(0, "blah")
                .assertQuestion("Target17", true)
                .swipeToNextQuestionWithConstraintViolation(org.odk.collect.strings.R.string.required_answer_error)
                .answerQuestion(0, "")
                .assertQuestion("Target17")
                .assertTextDoesNotExist(org.odk.collect.strings.R.string.required_answer_error);
    }

    // Scroll down until the desired group name is visible. This is needed to make the tests work
    // on devices with screens of different heights.
    private void jumpToGroupWithText(String text) {
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.menu_go_up)).perform(click());
        onView(withId(R.id.list)).perform(RecyclerViewActions.scrollTo(hasDescendant(withText(text))));

        onView(allOf(isDisplayed(), withText(text))).perform(click());
    }
}
