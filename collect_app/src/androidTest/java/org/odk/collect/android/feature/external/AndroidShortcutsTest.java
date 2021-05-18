package org.odk.collect.android.feature.external;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.AndroidShortcutsActivity;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.FormEntryPage;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class AndroidShortcutsTest {

    CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain testRuleChain = TestRuleChain.chain()
            .around(rule);

    @Test
    @Ignore("See comment in #pickAndLaunchShortcutForForm")
    public void canFillOutFormFromShortcut() {
        rule.mainMenu()
                .copyForm("one-question.xml")
                .clickFillBlankForm(); // Load form

        pickAndLaunchShortcutForForm("One Question")
                .assertQuestion("what is your age");
    }

    private FormEntryPage pickAndLaunchShortcutForForm(String formName) {
        ActivityScenario<AndroidShortcutsActivity> scenario = ActivityScenario.launch(AndroidShortcutsActivity.class);
        onView(withText(formName)).perform(click());
        Intent resultData = scenario.getResult().getResultData();
        Intent shortcutIntent = resultData.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);

         /*
        This launch call doesn't work because of: https://github.com/android/android-test/issues/496
         */
        ActivityScenario.launch(shortcutIntent);
        return new FormEntryPage(formName);
    }
}
