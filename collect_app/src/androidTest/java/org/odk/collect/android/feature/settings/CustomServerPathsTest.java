package org.odk.collect.android.feature.settings;

import android.Manifest;
import android.webkit.MimeTypeMap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.StubOpenRosaServer;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.ServerSettingsPage;
import org.odk.collect.utilities.UserAgentProvider;

@RunWith(AndroidJUnit4.class)
public class CustomServerPathsTest {

    public final StubOpenRosaServer server = new StubOpenRosaServer();

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
            ))
            .around(new ResetStateRule(new AppDependencyModule() {
                @Override
                public OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap, UserAgentProvider userAgentProvider) {
                    return server;
                }
            }))
            .around(new CopyFormRule("one-question.xml"))
            .around(rule);

    @Test // Issue number NODK-235 TestCase1
    public void changingFormListPathInSettings_changesFormListDownloadPath() {
        server.setFormListPath("/customPath");
        server.addForm("Custom path form", "one-question", "1", "one-question.xml");

        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .clickServerSettings()
                .clickOnURL()
                .inputText(server.getURL())
                .clickOKOnDialog()
                .clickCustomServerPaths()
                .clickFormListPath()
                .inputText("/customPath")
                .clickOKOnDialog()
                .assertText("/customPath")
                .pressBack(new ServerSettingsPage(rule))
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))

                .clickGetBlankForm()
                .assertText("Custom path form");
    }

    @Test // Issue number NODK-235 TestCase2
    public void changingSubmissionPathInSettings_changesSubmissionUploadPath() {
        server.setFormSubmissionPath("/customPath");

        new MainMenuPage(rule)
                .startBlankForm("One Question")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickOnMenu()
                .clickGeneralSettings()
                .clickServerSettings()
                .clickOnURL()
                .inputText(server.getURL())
                .clickOKOnDialog()
                .clickCustomServerPaths()
                .clickSubmissionPath()
                .inputText("/customPath")
                .clickOKOnDialog()
                .assertText("/customPath")
                .pressBack(new ServerSettingsPage(rule))
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))

                .clickSendFinalizedForm(1)
                .clickOnForm("One Question")
                .clickSendSelected()
                .assertText("One Question - Success");
    }
}
