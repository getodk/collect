package org.odk.collect.android.formentry.loading;

import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Supplier;

import timber.log.Timber;

public class FormInstanceFileCreator {

    private final StoragePathProvider storagePathProvider;
    private final Supplier<Long> clock;

    public FormInstanceFileCreator(StoragePathProvider storagePathProvider, Supplier<Long> clock) {
        this.storagePathProvider = storagePathProvider;
        this.clock = clock;
    }

    public File createInstanceFile(String formDefinitionPath) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH)
                .format(new Date(clock.get()));
        String formFileName = formDefinitionPath.substring(formDefinitionPath.lastIndexOf('/') + 1,
                formDefinitionPath.lastIndexOf('.'));
        String instancesDir = storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES);
        String instanceDir = instancesDir + File.separator + formFileName + "_" + timestamp;

        if (FileUtils.createFolder(instanceDir)) {
            return new File(instanceDir + File.separator + formFileName + "_" + timestamp + ".xml");
        } else {
            Timber.e("Error creating form instance file");
            return null;
        }
    }
}
