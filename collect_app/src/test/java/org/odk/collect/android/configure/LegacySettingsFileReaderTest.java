package org.odk.collect.android.configure;

import com.google.common.io.Files;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LegacySettingsFileReaderTest {

    private String storageDir;
    private LegacySettingsFileReader fileReader;

    @Before
    public void setup() {
        storageDir = Files.createTempDir().getAbsolutePath();
        StoragePathProvider pathProvider = mock(StoragePathProvider.class);
        when(pathProvider.getStorageRootDirPath()).thenReturn(storageDir);

        fileReader = new LegacySettingsFileReader(pathProvider);
    }

    @Test
    public void toJSON_whenSettingTxtExists_deletesFile() throws Exception {
        File settingsTxtFile = new File(storageDir + "/collect.settings");
        writeEmptyObjectSettings(settingsTxtFile);

        fileReader.toJSON();
        assertThat(settingsTxtFile.exists(), is(false));
    }

    @Test
    public void toJSON_whenSettingJSONExists_deletesFile() throws Exception {
        File settingsJsonFile = new File(storageDir + "/collect.settings.json");
        writeEmptyJsonSettings(settingsJsonFile);

        fileReader.toJSON();
        assertThat(settingsJsonFile.exists(), is(false));
    }

    private void writeEmptyJsonSettings(File file) throws JSONException {
        JSONObject jsonObject = new JSONObject()
                .put("general", new JSONObject())
                .put("admin", new JSONObject());

        FileUtils.write(file, jsonObject.toString().getBytes());
    }

    private void writeEmptyObjectSettings(File file) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));

        output.writeObject(new HashMap<String, Object>());
        output.writeObject(new HashMap<String, Object>());
        output.flush();
        output.close();
    }
}