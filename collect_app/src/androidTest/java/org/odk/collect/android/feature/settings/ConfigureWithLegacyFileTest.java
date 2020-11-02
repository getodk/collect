package org.odk.collect.android.feature.settings;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.OkDialog;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class ConfigureWithLegacyFileTest {

    private final TestDependencies testDependencies = new TestDependencies();
    private final CollectTestRule rule = new CollectTestRule();

    @Rule
    public TestRule chain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void canConfigureWithJSONFile() {
        File settingsFile = new File(testDependencies.storagePathProvider.getStorageRootDirPath(), "collect.settings.json");
        String settings = "{\"general\":{ \"server_url\": \"http://fire.water\" },\"admin\":{}}";
        FileUtils.write(settingsFile, settings.getBytes());

        new OkDialog(rule).assertOnPage()
                .assertText(R.string.settings_successfully_loaded_file_notification)
                .clickOK(new MainMenuPage(rule))
                .clickOnMenu()
                .clickGeneralSettings()
                .clickServerSettings()
                .assertText("http://fire.water");
    }

    @Test
    public void canConfigureWithObjectSettingsFile() throws Exception {
        File settingsFile = new File(testDependencies.storagePathProvider.getStorageRootDirPath(), "collect.settings");
        Map<String, Object> generalSettings = new HashMap<>();
        generalSettings.put("server_url", "http://fire.water");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(settingsFile));
        objectOutputStream.writeObject(generalSettings);
        objectOutputStream.writeObject(new HashMap<String, Object>());
        objectOutputStream.flush();
        objectOutputStream.close();

        new OkDialog(rule).assertOnPage()
                .assertText(R.string.settings_successfully_loaded_file_notification)
                .clickOK(new MainMenuPage(rule))
                .clickOnMenu()
                .clickGeneralSettings()
                .clickServerSettings()
                .assertText("http://fire.water");
    }
}
