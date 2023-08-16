package org.odk.collect.android.feature.formmanagement;

import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.StorageUtils;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

import java.io.File;

@RunWith(AndroidJUnit4.class)
public class FormsAdbTest {

    public final TestDependencies testDependencies = new TestDependencies();
    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain(testDependencies)
            .around(rule);

    @Test
    public void canUpdateAFormFromDisk() throws Exception {
        MainMenuPage mainMenuPage = rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .clickFillBlankForm()
                .assertFormExists("One Question")
                .pressBack(new MainMenuPage());

        StorageUtils.copyFormToDemoProject("one-question-updated.xml", "one-question.xml");

        mainMenuPage
                .clickFillBlankForm()
                .assertFormExists("One Question Updated")
                .assertFormDoesNotExist("One Question");
    }

    @Test
    public void canUpdateFormOnDiskFromServer() throws Exception {
        testDependencies.server.addForm("One Question Updated", "one_question", "2", "one-question-updated.xml");

        StorageUtils.copyFormToDemoProject("one-question.xml");

        rule.startAtMainMenu()
                .setServer(testDependencies.server.getURL())
                .clickGetBlankForm()
                .assertText(org.odk.collect.strings.R.string.newer_version_of_a_form_info)

                .clickGetSelected()
                .clickOKOnDialog(new MainMenuPage())
                .clickFillBlankForm()
                .assertFormExists("One Question Updated")
                .assertFormDoesNotExist("One Question");
    }

    @Test
    public void canDeleteFormFromDisk() {
        MainMenuPage mainMenuPage = rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .clickFillBlankForm()
                .assertFormExists("One Question")
                .pressBack(new MainMenuPage());

        String formsDir = testDependencies.storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS);
        boolean formDeleted = new File(formsDir, "one-question.xml").delete();
        assertTrue(formDeleted);

        mainMenuPage
                .clickFillBlankForm()
                .assertNoForms();
    }
}
