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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.odk.collect.android.support.CustomMatchers.withIndex;
import static org.odk.collect.android.support.actions.NestedScrollToAction.nestedScrollTo;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.RatingBar;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.RecordedIntentsRule;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.preferences.GuidanceHint;
import org.odk.collect.android.preferences.keys.ProjectKeys;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.FormActivityTestRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.FormEntryPage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

public class FieldListUpdateTest {
    private static final String FIELD_LIST_TEST_FORM = "fieldlist-updates.xml";

    @Rule
    public FormActivityTestRule activityTestRule = new FormActivityTestRule(FIELD_LIST_TEST_FORM, "fieldlist-updates");

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.CAMERA))
            .around(new RecordedIntentsRule())
            .around(new ResetStateRule())
            .around(new CopyFormRule(FIELD_LIST_TEST_FORM, Collections.singletonList("fruits.csv"), true));

    @Test
    public void relevanceChangeAtEnd_ShouldToggleLastWidgetVisibility() {
        jumpToGroupWithText("Single relevance at end");
        onView(withText("Source1")).perform(click());

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Target1")).check(doesNotExist());
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText("A"));
        onView(withText("Target1")).check(matches(isDisplayed()));
        onView(withText("Target1")).check(isCompletelyBelow(withText("Source1")));

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Target1")).check(doesNotExist());
    }

    @Test
    public void relevanceChangeAtBeginning_ShouldToggleFirstWidgetVisibility() {
        jumpToGroupWithText("Single relevance at beginning");
        onView(withText("Source2")).perform(click());

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Target2")).check(doesNotExist());
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText("A"));
        onView(withText("Target2")).check(matches(isDisplayed()));
        onView(withText("Target2")).check(isCompletelyAbove(withText("Source2")));

        onView(withIndex(withClassName(endsWith("EditText")), 1)).perform(replaceText(""));
        onView(withText("Target2")).check(doesNotExist());
    }

    @Test
    public void relevanceChangeInMiddle_ShouldToggleMiddleWidgetVisibility() {
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
        jumpToGroupWithText("Single relevance in middle");
        onView(withText("Source3")).perform(click());

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Target3")).check(doesNotExist());
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText("A"));
        onView(withText("Target3")).check(matches(isDisplayed()));

        onView(withText("Source3")).perform(longClick());
        onView(withText(R.string.clear_answer)).perform(click());
        onView(withText(R.string.discard_answer)).perform(click());
        onView(withIndex(withClassName(endsWith("EditText")), 0)).check(matches(withText("")));
        onView(withText("Target3")).check(doesNotExist());
    }

    @Test
    public void changeInValueUsedInLabel_ShouldChangeLabelText() {
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
        onView(withText("A1A")).perform(nestedScrollTo());
        onView(withText("A1A")).check(matches(isDisplayed()));
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());
    }

    @Test
    public void clearingParentSelect_ShouldUpdateAllDependentLevels() {
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.menu_go_up)).perform(click());
        onView(allOf(withText("Cascading select"), isDisplayed())).perform(click());
        onView(withText(startsWith("Level1"))).perform(click());

        onView(withText("A")).perform(click());

        onView(withText("A1")).perform(nestedScrollTo(), click());
        onView(withText("A1B")).perform(nestedScrollTo(), click());

        onView(withText("Level1")).perform(nestedScrollTo(), longClick());
        onView(withText(R.string.clear_answer)).perform(click());
        onView(withText(R.string.discard_answer)).perform(click());

        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isNotChecked()));
        onView(withText("A1")).check(doesNotExist());
        onView(withText("A1B")).check(doesNotExist());
    }

    @Test
    public void selectionChangeAtOneCascadeLevelWithMinimalAppearance_ShouldUpdateNextLevels() {
        new FormEntryPage("fieldlist-updates")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnGroup("Cascading select minimal")
                .clickOnQuestion("Level1")
                .assertTextDoesNotExist("A1", "B1", "C1", "A1A") // No choices should be shown for levels 2 and 3 when no selection is made for level 1
                .openSelectMinimalDialog(0)
                .clickOnText("C") // Selecting C for level 1 should only reveal options for C at level 2
                .assertTextDoesNotExist("A1", "B1")
                .openSelectMinimalDialog(1)
                .clickOnText("C1")
                .assertTextDoesNotExist("A1A")
                .clickOnText("C")
                .clickOnText("A") // Selecting A for level 1 should reveal options for A at level 2
                .openSelectMinimalDialog(1)
                .assertText("A1")
                .assertTextDoesNotExist("A1A", "B1", "C1")
                .clickOnText("A1") // Selecting A1 for level 2 should reveal options for A1 at level 3
                .openSelectMinimalDialog(2)
                .assertText("A1A")
                .assertTextDoesNotExist("B1A", "B1", "C1");
    }

    @Test
    public void questionsAppearingBeforeCurrentTextQuestion_ShouldNotChangeFocus() {
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
        jumpToGroupWithText("Push off screen binary");
        onView(withText(startsWith("Source10"))).perform(click());

        onView(withText("Target10-15")).check(doesNotExist());

        // FormEntryActivity expects an image at a fixed path so copy the app logo there
        Bitmap icon = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.drawable.notes);
        File tmpJpg = new File(new StoragePathProvider().getTmpImageFilePath());
        tmpJpg.createNewFile();
        FileOutputStream fos = new FileOutputStream(tmpJpg);
        icon.compress(Bitmap.CompressFormat.JPEG, 90, fos);

        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        onView(withId(R.id.capture_image)).perform(click());

        onView(withText("Target10-15")).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.capture_image)).check(matches(isCompletelyDisplayed()));
    }

    @Test
    public void changeInValueUsedInGuidanceHint_ShouldChangeGuidanceHintText() {
        TestSettingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_GUIDANCE_HINT, GuidanceHint.YES.toString());
        jumpToGroupWithText("Guidance hint");
        onView(withText(startsWith("Source11"))).perform(click());

        onView(withText("10")).check(doesNotExist());
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText("5"));
        onView(withText("10")).check(matches(isDisplayed()));
        onView(withText("10")).check(isCompletelyBelow(withText("Target11")));
    }

    @Test
    public void selectingADateForDateTime_ShouldChangeRelevanceOfRelatedField() {
        jumpToGroupWithText("Date time");
        onView(withText(startsWith("Source12"))).perform(click());

        onView(withText("Target12")).check(doesNotExist());

        onView(withText(R.string.select_date)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

        onView(withText("Target12")).check(matches(isDisplayed()));
    }

    @Test
    public void selectingARating_ShouldChangeRelevanceOfRelatedField() throws Exception {
        jumpToGroupWithText("Rating");
        onView(withText(startsWith("Source13"))).perform(click());

        onView(withText("Target13")).check(doesNotExist());
        onView(allOf(withId(R.id.rating_bar1), isDisplayed())).perform(setRating(3.0f));
        onView(withText("Target13")).check(matches(isDisplayed()));

        onView(withText("Source13")).perform(longClick());
        onView(withText(R.string.clear_answer)).perform(click());
        onView(withText(R.string.discard_answer)).perform(click());
        onView(withText("Target13")).check(doesNotExist());
    }

    @Test
    public void manuallySelectingAValueForMissingExternalApp_ShouldTriggerUpdate() {
        jumpToGroupWithText("External app");
        onView(withText(startsWith("Source14"))).perform(click());

        onView(withText(startsWith("Launch"))).perform(click());
        onView(withClassName(endsWith("EditText"))).perform(replaceText(String.valueOf(new Random().nextInt())));

        onView(withText("Target14")).check(matches(isDisplayed()));
    }

    @Test
    public void searchMinimalInFieldList() {
        new FormEntryPage("fieldlist-updates")
                .clickGoToArrow()
                .clickGoUpIcon()
                .clickOnGroup("Search in field-list")
                .clickOnQuestion("Source15")
                .openSelectMinimalDialog()
                .assertText("Mango", "Oranges", "Strawberries")
                .clickOnText("Strawberries")
                .assertText("Target15")
                .assertSelectMinimalDialogAnswer("Strawberries");
    }

    // Scroll down until the desired group name is visible. This is needed to make the tests work
    // on devices with screens of different heights.
    private void jumpToGroupWithText(String text) {
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.menu_go_up)).perform(click());
        onView(withId(R.id.list)).perform(RecyclerViewActions.scrollTo(hasDescendant(withText(text))));

        onView(allOf(isDisplayed(), withText(text))).perform(click());
    }

    public static ViewAction setRating(final float rating) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(RatingBar.class);
            }

            @Override
            public String getDescription() {
                return "Custom view action to set rating on RatingBar";
            }

            @Override
            public void perform(UiController uiController, View view) {
                RatingBar ratingBar = (RatingBar) view;
                ratingBar.setRating(rating);
            }
        };
    }
}
