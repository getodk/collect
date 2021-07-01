package org.odk.collect.android.feature.external;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;

@RunWith(AndroidJUnit4.class)
public class AndroidShortcutsTest {

    CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain testRuleChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void showsFormsForCurrentProject() {
        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .clickFillBlankForm() // Load form
                .pressBack(new MainMenuPage())
                .addAndSwitchToProject("https://example.com")
                .copyForm("two-question.xml")
                .clickFillBlankForm(); // Load form

        rule.launchShortcuts()
                .assertText("Two Question")
                .assertTextDoesNotExist("One Question");
    }

    @Test
    public void canFillOutFormFromShortcut() {
        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .clickFillBlankForm(); // Load form

        Intent shortcutIntent = rule.launchShortcuts()
                .selectForm("One Question");

        rule.launch(shortcutIntent, new FormEntryPage("One Question"))
                .assertQuestion("what is your age");
    }

    @Test
    public void whenDifferentProjectSelected_canStillFillOutFormFromShortcut() {
        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .clickFillBlankForm() // Load form
                .pressBack(new MainMenuPage());

        Intent shortcutIntent = rule.launchShortcuts()
                .selectForm("One Question");

        rule.restart()
                .startAtMainMenu()
                .addAndSwitchToProject("https://example.com");

        rule.launch(shortcutIntent, new FormEntryPage("One Question"))
                .assertQuestion("what is your age");
    }
}
