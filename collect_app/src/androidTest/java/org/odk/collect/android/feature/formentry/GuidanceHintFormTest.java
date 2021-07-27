package org.odk.collect.android.feature.formentry;

import android.text.TextUtils;

import androidx.test.espresso.matcher.ViewMatchers;

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GuidanceHint;
import org.odk.collect.android.preferences.keys.ProjectKeys;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.FormActivityTestRule;
import org.odk.collect.android.support.AdbFormLoadingUtils;
import org.odk.collect.android.support.ResetStateRule;

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
    public FormActivityTestRule activityTestRule = AdbFormLoadingUtils.getFormActivityTestRuleFor(GUIDANCE_SAMPLE_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new CopyFormRule(GUIDANCE_SAMPLE_FORM, true));

    @Test
    public void guidanceHint_ShouldBeHiddenByDefault() {
        onView(ViewMatchers.withId(R.id.guidance_text_view)).check(matches(not(isDisplayed())));
    }

    @Test
    public void guidanceHint_ShouldBeDisplayedWhenSettingSetToYes() {
        TestSettingsProvider.getGeneralSettings().save(ProjectKeys.KEY_GUIDANCE_HINT, GuidanceHint.Yes.toString());
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
        TestSettingsProvider.getGeneralSettings().save(ProjectKeys.KEY_GUIDANCE_HINT, GuidanceHint.YesCollapsed.toString());
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
