package org.odk.collect.android.storage;

import android.os.Environment;

import java.io.File;

public class StorageManager {
    public static final String ODK_ROOT = Environment.getExternalStorageDirectory() + File.separator + "odk";
    public static final String INSTANCES_PATH = ODK_ROOT + File.separator + "instances";
    public static final String FORMS_PATH = ODK_ROOT + File.separator + "forms";
}
