package org.odk.collect.android.configure.legacy;

import androidx.core.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.storage.StoragePathProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

public class LegacySettingsFileReader {

    private final File objectFile;
    private final File jsonFile;

    public LegacySettingsFileReader(StoragePathProvider storagePathProvider) {
        this.objectFile = new File(storagePathProvider.getStorageRootDirPath() + "/collect.settings");
        this.jsonFile = new File(storagePathProvider.getStorageRootDirPath() + "/collect.settings.json");
    }

    public String toJSON() throws CorruptSettingsFileException {
        try {
            if (jsonFile.exists()) {
                return readJSONFile(jsonFile);
            } else if (objectFile.exists()) {
                Pair<Map<String, Object>, Map<String, Object>> settings = readSettingsFile(objectFile);
                return new JSONObject()
                        .put("general", new JSONObject(settings.first))
                        .put("admin", new JSONObject(settings.second))
                        .toString();
            } else {
                return null;
            }
        } catch (IOException | JSONException | ClassNotFoundException e) {
            throw new CorruptSettingsFileException();
        }
    }

    public void delete() {
        jsonFile.delete();
        objectFile.delete();
    }

    private String readJSONFile(File src) throws IOException {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(src))) {
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }

            return builder.toString();
        }
    }

    private Pair<Map<String, Object>, Map<String, Object>> readSettingsFile(File src) throws IOException, ClassNotFoundException {
        // this should probably be in a thread if it ever gets big
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(src))) {
            Map<String, Object> generalEntries = (Map<String, Object>) input.readObject();
            Map<String, Object> adminEntries = (Map<String, Object>) input.readObject();

            return new Pair<>(generalEntries, adminEntries);
        }
    }

    public static class CorruptSettingsFileException extends Exception {

        private CorruptSettingsFileException() {

        }
    }
}
