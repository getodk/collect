package org.odk.collect.android.formmanagement;

import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class FormMetadataParser {
    public Map<String, String> parse(File file, File mediaDir) {
        HashMap<String, String> metadata;
        try {
            metadata = FileUtils.getMetadataFromFormDefinition(file);
        } catch (Exception e) {
            Timber.e(e);

            throw e;
        }

        return metadata;
    }
}
