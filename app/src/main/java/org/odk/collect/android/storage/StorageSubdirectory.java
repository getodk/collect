package org.odk.collect.android.storage;

public enum StorageSubdirectory {
    FORMS("forms"),
    INSTANCES("instances"),
    CACHE(".cache"),
    METADATA("metadata"),
    LAYERS("layers"),
    SETTINGS("settings"),
    PROJECTS("projects"),
    SHARED_LAYERS("layers");

    private final String directoryName;

    StorageSubdirectory(String directoryName) {
        this.directoryName = directoryName;
    }

    public String getDirectoryName() {
        return directoryName;
    }
}
