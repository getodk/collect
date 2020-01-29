package org.odk.collect.android.storage;

import java.io.File;

public enum StorageSubdirectory {
    ODK("odk"),
    FORMS(ODK.directoryName + File.separator + "forms"),
    INSTANCES(ODK.directoryName + File.separator + "instances"),
    CACHE(ODK.directoryName + File.separator + ".cache"),
    METADATA(ODK.directoryName + File.separator + "metadata"),
    LAYERS(ODK.directoryName + File.separator + "layers"),
    SETTINGS(ODK.directoryName + File.separator + "settings");

    private String directoryName;

    StorageSubdirectory(String directoryName) {
        this.directoryName = directoryName;
    }

    public String getDirectoryName() {
        return directoryName;
    }
}
