package org.odk.collect.android.feature.settings;

import android.Manifest;
import android.webkit.MimeTypeMap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.StubOpenRosaServer;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.utilities.UserAgentProvider;

@RunWith(AndroidJUnit4.class)
public class ServerSettingsTest {

    public final StubOpenRosaServer server = new StubOpenRosaServer();

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.GET_ACCOUNTS
            ))
            .around(new ResetStateRule(new AppDependencyModule() {
                @Override
                public OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap, UserAgentProvider userAgentProvider) {
                    return server;
                }
            }))
            .around(rule);

    @Test
    public void settingODKServer_usesServerForFetchingAndSubmittingForms() {
        server.setCredentials("Joe", "netsky");
        server.addForm("One Question", "one-question.xml");

        rule.mainMenu()
                .clickOnMenu()
                .clickGeneralSettings()
                .clickServerSettings()
                .clickOnURL()
                .inputText(server.getURL())
                .clickOKOnDialog()
                .assertText(server.getURL())
                .clickServerUsername()
                .inputText("Joe")
                .clickOKOnDialog()
                .assertText("Joe")
                .clickServerPassword()
                .inputText("netsky")
                .clickOKOnDialog()
                .assertText("********")
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))

                .clickGetBlankForm()
                .clickGetSelected()
                .assertMessage("One Question (Version:: 1 ID: 0) - Success")
                .clickOK(new MainMenuPage(rule))

                .startBlankForm("One Question")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickSendFinalizedForm(1)
                .clickOnForm("One Question")
                .clickSendSelected()
                .assertText("One Question - Success");
    }

    /**
     * This test could definitely be extended to cover form download/submit (like the ODK server
     * type test with the creation of a stub
     * {@link org.odk.collect.android.utilities.gdrive.DriveHelper} and
     * {@link org.odk.collect.android.utilities.gdrive.GoogleAccountsManager}
     */
    @Test
    public void selectingGoogleAccount_showsGoogleAccountSettings() {
        rule.mainMenu()
                .clickOnMenu()
                .clickGeneralSettings()
                .clickServerSettings()
                .clickOnServerType()
                .clickOnString(R.string.server_platform_google_sheets)
                .assertText(R.string.selected_google_account_text)
                .assertText(R.string.google_sheets_url);
    }
}
