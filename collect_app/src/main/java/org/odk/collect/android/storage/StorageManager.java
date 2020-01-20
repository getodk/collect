package org.odk.collect.android.storage;

import android.os.Environment;

import java.io.File;

public class StorageManager {
    public static final String ODK_ROOT = Environment.getExternalStorageDirectory() + File.separator + "odk";
    public static final String SETTINGS = ODK_ROOT + File.separator + "settings";
    public static final String OFFLINE_LAYERS = ODK_ROOT + File.separator + "layers";
    public static final String METADATA_PATH = ODK_ROOT + File.separator + "metadata";
    public static final String CACHE_PATH = ODK_ROOT + File.separator + ".cache";
    public static final String INSTANCES_PATH = ODK_ROOT + File.separator + "instances";
    public static final String FORMS_PATH = ODK_ROOT + File.separator + "forms";
    public static final String TMPFILE_PATH = CACHE_PATH + File.separator + "tmp.jpg";
    public static final String TMPDRAWFILE_PATH = CACHE_PATH + File.separator + "tmpDraw.jpg";
}
