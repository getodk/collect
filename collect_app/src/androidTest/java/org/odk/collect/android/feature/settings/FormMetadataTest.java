package org.odk.collect.android.feature.settings;

import android.Manifest;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.metadata.InstallIDProvider;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;
import org.odk.collect.android.support.pages.UserAndDeviceIdentitySettingsPage;
import org.odk.collect.android.utilities.DeviceDetailsProvider;

@RunWith(AndroidJUnit4.class)
public class FormMetadataTest {

    private final DeviceDetailsProvider deviceDetailsProvider = new FakeDeviceDetailsProvider();
    public ActivityTestRule<MainMenuActivity> rule = new ActivityTestRule<>(MainMenuActivity.class);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(new ResetStateRule(new AppDependencyModule() {
                @Override
                public DeviceDetailsProvider providesDeviceDetailsProvider(Context context, InstallIDProvider installIDProvider) {
                    return deviceDetailsProvider;
                }
            }))
            .around(new CopyFormRule("metadata.xml"))
            .around(rule);

    @Test
    public void settingMetadata_letsThemBeIncludedInAForm() {
        new MainMenuPage(rule)
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .clickUserAndDeviceIdentity()
                .clickFormMetadata()
                .clickUsername()
                .inputText("Chino")
                .clickOKOnDialog()
                .assertPreference(R.string.username, "Chino")
                .clickEmail()
                .inputText("chino@whitepony.com")
                .clickOKOnDialog()
                .assertPreference(R.string.email, "chino@whitepony.com")
                .clickPhoneNumber()
                .inputText("664615")
                .clickOKOnDialog()
                .assertPreference(R.string.phone_number, "664615")
                .pressBack(new UserAndDeviceIdentitySettingsPage(rule))
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .startBlankForm("Metadata")
                .assertText("Chino", "chino@whitepony.com", "664615");
    }

    @Test // Issue number NODK-238 TestCase4 TestCase5
    public void settingServerUsername_usedAsFallbackForMetadataUsername() {
        new MainMenuPage(rule)
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .clickServerSettings()
                .clickServerUsername()
                .inputText("Chino")
                .clickOKOnDialog()
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .startBlankForm("Metadata")
                .assertText("Chino")
                .pressBack(new SaveOrIgnoreDialog<>("Metadata", new MainMenuPage(rule), rule))
                .clickIgnoreChanges()
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .clickUserAndDeviceIdentity()
                .clickFormMetadata()
                .clickUsername()
                .inputText("Stephen")
                .clickOKOnDialog()
                .pressBack(new UserAndDeviceIdentitySettingsPage(rule))
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .startBlankForm("Metadata")
                .assertText("Stephen");
    }

    @Test
    public void deviceIdentifiersAreDisplayedInSettings() {
        new MainMenuPage(rule)
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .clickUserAndDeviceIdentity()
                .clickFormMetadata()
                .assertPreference(R.string.device_id, deviceDetailsProvider.getDeviceId());
    }

    @Test
    public void deviceIdentifiersCanBeIncludedInAForm() {
        new MainMenuPage(rule)
                .startBlankForm("Metadata")
                .scrollToAndAssertText(deviceDetailsProvider.getDeviceId());
    }

    private static class FakeDeviceDetailsProvider implements DeviceDetailsProvider {

        @Override
        public String getDeviceId() {
            return "deviceID";
        }

        @Override
        public String getLine1Number() {
            return "line1Number";
        }
    }
}
