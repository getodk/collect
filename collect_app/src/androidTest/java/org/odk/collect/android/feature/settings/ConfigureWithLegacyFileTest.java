package org.odk.collect.android.feature.settings;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.RunnableRule;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.OkDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class ConfigureWithLegacyFileTest {

    private final TestDependencies testDependencies = new TestDependencies();
    private final CollectTestRule rule = new CollectTestRule();

    @Rule
    public TestRule chain = TestRuleChain.chain()
            .around(new RunnableRule(() -> {
                try {
                    File settingsFile = new File(testDependencies.storagePathProvider.getStorageRootDirPath(), "collect.settings");
                    Map<String, Object> generalSettings = new HashMap<>();
                    generalSettings.put("server_url", "http://fire.water");
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(settingsFile));
                    objectOutputStream.writeObject(generalSettings);
                    objectOutputStream.writeObject(new HashMap<String, Object>());
                    objectOutputStream.flush();
                    objectOutputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }))
            .around(rule);

    @Test
    public void canConfigureWithObjectSettingsFile() {
        new OkDialog(rule).assertOnPage()
                .assertText(R.string.settings_successfully_loaded_file_notification)
                .clickOK(new MainMenuPage(rule))
                .clickOnMenu()
                .clickGeneralSettings()
                .clickServerSettings()
                .assertText("http://fire.water");
    }
}
