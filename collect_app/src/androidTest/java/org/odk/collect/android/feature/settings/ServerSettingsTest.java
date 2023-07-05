package org.odk.collect.android.feature.settings;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.ProjectSettingsPage;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.androidtest.RecordedIntentsRule;

@RunWith(AndroidJUnit4.class)
public class ServerSettingsTest {

    private final TestDependencies testDependencies = new TestDependencies();

    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain(testDependencies)
            .around(new RecordedIntentsRule())
            .around(rule);

    @Test
    public void whenUsingODKServer_canAddCredentialsForServer() {
        testDependencies.server.setCredentials("Joe", "netsky");
        testDependencies.server.addForm("One Question", "one-question", "1", "one-question.xml");

        new MainMenuPage().assertOnPage()
                .openProjectSettingsDialog()
                .clickSettings()
                .clickServerSettings()
                .clickOnURL()
                .inputText(testDependencies.server.getURL())
                .clickOKOnDialog()
                .assertText(testDependencies.server.getURL())
                .clickServerUsername()
                .inputText("Joe")
                .clickOKOnDialog()
                .assertText("Joe")
                .clickServerPassword()
                .inputText("netsky")
                .clickOKOnDialog()
                .assertText("********")
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())

                .clickGetBlankForm()
                .clickGetSelected()
                .assertMessage("All downloads succeeded!")
                .clickOKOnDialog(new MainMenuPage());
    }

    @Test
    public void selectingServerTypeIsDisabled() {
        new MainMenuPage().assertOnPage()
                .openProjectSettingsDialog()
                .clickSettings()
                .clickServerSettings()
                .clickOnServerType()
                .assertTextDoesNotExist(org.odk.collect.strings.R.string.cancel);
    }
}
