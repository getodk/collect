package org.odk.collect.android.configure;

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

import timber.log.Timber;

public class LegacySettingsFileReader {

    private final StoragePathProvider storagePathProvider;

    public LegacySettingsFileReader(StoragePathProvider storagePathProvider) {
        this.storagePathProvider = storagePathProvider;
    }

    public String toJSON() throws CorruptSettingsFileException {
        File f = new File(storagePathProvider.getStorageRootDirPath() + "/collect.settings");
        File j = new File(storagePathProvider.getStorageRootDirPath() + "/collect.settings.json");

        try {
            if (j.exists()) {
                String settings = readJSONFile(j);
                f.delete();
                return settings;
            } else if (f.exists()) {
                Pair<Map<String, Object>, Map<String, Object>> settings = readSettingsFile(f);
                j.delete();

                return new JSONObject()
                        .put("general", settings.first)
                        .put("admin", settings.second)
                        .toString();
            } else {
                return null;
            }
        } catch (IOException | JSONException | ClassNotFoundException e) {
            throw new CorruptSettingsFileException();
        }
    }

    private String readJSONFile(File src) throws IOException {
        BufferedReader br = null;

        try {
            String line = null;
            StringBuilder builder = new StringBuilder();
            br = new BufferedReader(new FileReader(src));

            while ((line = br.readLine()) != null) {
                builder.append(line);
            }

            return builder.toString();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                Timber.e(ex, "Exception thrown while closing an input stream due to: %s ", ex.getMessage());
            }
        }
    }

    private Pair<Map<String, Object>, Map<String, Object>> readSettingsFile(File src) throws IOException, ClassNotFoundException {
        // this should probably be in a thread if it ever gets big
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));

            // first object is preferences
            Map<String, Object> generalEntries = (Map<String, Object>) input.readObject();
            Map<String, Object> adminEntries = (Map<String, Object>) input.readObject();

            return new Pair<>(generalEntries, adminEntries);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                Timber.e(ex, "Exception thrown while closing an input stream due to: %s ", ex.getMessage());
            }
        }
    }

    public static class CorruptSettingsFileException extends Exception {

        private CorruptSettingsFileException() {

        }
    }
}
