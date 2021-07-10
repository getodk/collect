package org.odk.collect.android.feature.formmanagement;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.ProjectSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;

@RunWith(AndroidJUnit4.class)
public class HideOldVersionsTest {

    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void whenHideOldVersionsEnabled_onlyTheNewestVersionOfAFormShowsInFormList() {
        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .clickFillBlankForm() // Sync forms on disk
                .pressBack(new MainMenuPage())
                .copyForm("one-question-updated.xml")

                .clickFillBlankForm()
                .assertFormExists("One Question Updated")
                .assertFormDoesNotExist("One Question");
    }

    @Test
    public void whenHideOldVersionsDisabled_allVersionOfAFormShowsInFormList() {
        rule.startAtMainMenu()
                .openProjectSettings()
                .clickGeneralSettings()
                .clickFormManagement()
                .scrollToRecyclerViewItemAndClickText(R.string.hide_old_form_versions_setting_title)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())

                .copyForm("one-question.xml")
                .copyForm("one-question-updated.xml")

                .clickFillBlankForm()
                .assertFormExists("One Question Updated")
                .assertFormExists("One Question");
    }
}
