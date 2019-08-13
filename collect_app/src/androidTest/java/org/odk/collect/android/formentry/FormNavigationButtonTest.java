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

package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.javarosa.core.model.FormIndex;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.test.FormLoadingUtils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.AllOf.allOf;
import static org.odk.collect.android.support.CollectHelpers.waitForFormController;
import static org.odk.collect.android.test.FormLoadingUtils.ALL_WIDGETS_FORM;

/**
 * Tests that visibility of the next and back buttons is correctly linked to the navigation
 * setting and the backwards navigation admin setting.
 *
 * Note: FormEntryActivity uses the following code to detect buttons so there's no need to
 * separately check the option to navigate with either swipes or buttons:
 *
 * <code>
 * String navigation = (String) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_NAVIGATION);
 * showNavigationButtons = navigation.contains(GeneralKeys.NAVIGATION_BUTTONS);
 * </code>
 */
public class FormNavigationButtonTest {
    @Rule
    public ActivityTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(ALL_WIDGETS_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule(ALL_WIDGETS_FORM));

    @Before
    public void resetAllPreferences() {
        GeneralSharedPreferences.getInstance().loadDefaultPreferences();
        AdminSharedPreferences.getInstance().loadDefaultPreferences();
    }

    @AfterClass
    public static void resetAllPreferencesAtEnd() {
        GeneralSharedPreferences.getInstance().loadDefaultPreferences();
        AdminSharedPreferences.getInstance().loadDefaultPreferences();
    }

    @Test
    public void navigationButtons_ShouldNotShowWithDefaultSettings() {
        // rebuild view after preferences reset
        activityTestRule.getActivity().runOnUiThread(() -> activityTestRule.getActivity().recreate());

        for (int i = 0; i < 10; i++) {
            onView(withId(R.id.form_back_button)).check(matches(not(isDisplayed())));
            onView(withId(R.id.form_forward_button)).check(matches(not(isDisplayed())));

            onView(withId(R.id.questionholder)).perform(swipeLeft());
        }
    }

    @Test
    public void onlyNextButton_ShouldShowOnFirstScreen() {
        GeneralSharedPreferences.getInstance().save(GeneralKeys.KEY_NAVIGATION, GeneralKeys.NAVIGATION_BUTTONS);
        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_MOVING_BACKWARDS, true);
        activityTestRule.getActivity().runOnUiThread(() -> activityTestRule.getActivity().recreate());

        onView(withId(R.id.form_forward_button)).check(matches(isDisplayed()));
        onView(withId(R.id.form_forward_button)).check(matches(isEnabled()));
        onView(withId(R.id.form_back_button)).check(matches(not(isDisplayed())));

        // move forward and then back to the first question
        onView(withId(R.id.form_forward_button)).perform(click());
        onView(withId(R.id.form_back_button)).perform(click());

        onView(withId(R.id.form_forward_button)).check(matches(isDisplayed()));
        onView(withId(R.id.form_forward_button)).check(matches(isEnabled()));
        onView(withId(R.id.form_back_button)).check(matches(not(isDisplayed())));
    }

    @Test
    public void nextAndBackButtons_ShouldShowOnMiddleScreensIfNavigatingBackwardsIsEnabled() {
        GeneralSharedPreferences.getInstance().save(GeneralKeys.KEY_NAVIGATION, GeneralKeys.NAVIGATION_BUTTONS);
        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_MOVING_BACKWARDS, true);
        onView(withId(R.id.menu_goto)).perform(click());
        onView(allOf(withText("Image widgets"), isDisplayed())).perform(click());
        onView(withText(startsWith("Draw widget"))).perform(click());

        for (int i = 0; i < 10; i++) {
            onView(withId(R.id.form_forward_button)).perform(click());

            onView(withId(R.id.form_forward_button)).check(matches(isDisplayed()));
            onView(withId(R.id.form_forward_button)).check(matches(isEnabled()));
            onView(withId(R.id.form_back_button)).check(matches(isDisplayed()));
            onView(withId(R.id.form_back_button)).check(matches(isEnabled()));
        }
    }

    @Test
    public void onlyNextButton_ShouldShowOnMiddleScreensIfNavigatingBackwardsIsDisabled() {
        GeneralSharedPreferences.getInstance().save(GeneralKeys.KEY_NAVIGATION, GeneralKeys.NAVIGATION_BUTTONS);
        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_MOVING_BACKWARDS, false);
        activityTestRule.getActivity().runOnUiThread(() -> activityTestRule.getActivity().recreate());

        for (int i = 0; i < 10; i++) {
            onView(withId(R.id.form_forward_button)).perform(click());

            onView(withId(R.id.form_forward_button)).check(matches(isDisplayed()));
            onView(withId(R.id.form_forward_button)).check(matches(isEnabled()));
            onView(withId(R.id.form_back_button)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void onlyBackButton_ShouldShowOnLastScreen_IfNavigatingBackwardsIsEnabled() {
        GeneralSharedPreferences.getInstance().save(GeneralKeys.KEY_NAVIGATION, GeneralKeys.NAVIGATION_BUTTONS);
        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_MOVING_BACKWARDS, true);

        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.jumpEndButton)).perform(click());

        onView(withId(R.id.form_forward_button)).check(matches(not(isDisplayed())));
        onView(withId(R.id.form_back_button)).check(matches(isDisplayed()));
        onView(withId(R.id.form_back_button)).check(matches(isEnabled()));
    }

    @Test
    public void noButtons_ShouldShowOnLastScreen_IfNavigatingBackwardsIsDisabled() throws Exception {
        GeneralSharedPreferences.getInstance().save(GeneralKeys.KEY_NAVIGATION, GeneralKeys.NAVIGATION_BUTTONS);
        AdminSharedPreferences.getInstance().save(AdminKeys.KEY_MOVING_BACKWARDS, false);

        // the jump button doesn't exist so set the form controller to the end and recreate activity
        waitForFormController().jumpToIndex(FormIndex.createEndOfFormIndex());
        activityTestRule.getActivity().runOnUiThread(() -> activityTestRule.getActivity().recreate());

        onView(withId(R.id.form_forward_button)).check(matches(not(isDisplayed())));
        onView(withId(R.id.form_back_button)).check(matches(not(isDisplayed())));
    }
}
