package org.odk.collect.android;

import android.Manifest;
import android.text.TextUtils;

import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.GuidanceHint;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.test.FormLoadingUtils;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.CoreMatchers.not;

public class GuidanceHintFormTest {
    private static final String GUIDANCE_SAMPLE_FORM = "guidance_hint_form.xml";

    @BeforeClass
    public static void beforeAll() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());
    }

    @Rule
    public ActivityTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(GUIDANCE_SAMPLE_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .around(new ResetStateRule())
            .around(new CopyFormRule(GUIDANCE_SAMPLE_FORM));

    @Before
    public void resetPreferences() {
        GeneralSharedPreferences.getInstance().reloadPreferences();
    }

    @AfterClass
    public static void resetPreferencesAtEnd() {
        GeneralSharedPreferences.getInstance().reloadPreferences();
    }

    @Test
    public void guidanceHint_ShouldBeHiddenByDefault() {
        onView(withId(R.id.guidance_text_view)).check(matches(not(isDisplayed())));
    }

    @Test
    public void guidanceHint_ShouldBeDisplayedWhenSettingSetToYes() {
        GeneralSharedPreferences.getInstance().save(GeneralKeys.KEY_GUIDANCE_HINT, GuidanceHint.Yes.toString());
        // jump to force recreation of the view after the settings change
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.jumpBeginningButton)).perform(click());

        FormEntryPrompt prompt = Collect.getInstance().getFormController().getQuestionPrompt();
        String guidance = prompt.getSpecialFormQuestionText(prompt.getQuestion().getHelpTextID(), "guidance");
        assertFalse(TextUtils.isEmpty(guidance));

        Screengrab.screenshot("guidance_hint");

        onView(withId(R.id.guidance_text_view)).check(matches(withText(guidance)));
    }

    @Test
    public void guidanceHint_ShouldBeDisplayedAfterClickWhenSettingSetToYesCollapsed() {
        GeneralSharedPreferences.getInstance().save(GeneralKeys.KEY_GUIDANCE_HINT, GuidanceHint.YesCollapsed.toString());
        // jump to force recreation of the view after the settings change
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.jumpBeginningButton)).perform(click());

        FormEntryPrompt prompt = Collect.getInstance().getFormController().getQuestionPrompt();
        String guidance = prompt.getSpecialFormQuestionText(prompt.getQuestion().getHelpTextID(), "guidance");
        assertFalse(TextUtils.isEmpty(guidance));

        onView(withId(R.id.guidance_text_view)).check(matches(not(isDisplayed())));
        onView(withId(R.id.help_icon)).perform(click());
        onView(withId(R.id.guidance_text_view)).check(matches(withText(guidance)));
    }
}
