package org.odk.collect.android.utilities.tempinstance;

import android.support.annotation.NonNull;

import org.odk.collect.android.tasks.SaveToDiskTask;

import java.io.File;

public class TemporaryInstanceFileManager {
    public File getSavePointFileForInstancePath(@NonNull File instancePath) {
        return SaveToDiskTask.savepointFile(instancePath);
    }

    public File getInstanceFolder(@NonNull File instancePath) {
        return new File(instancePath.getParent());
    }
}
