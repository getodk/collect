package org.odk.collect.android.utilities.tempinstance;

import android.database.Cursor;
import android.support.annotation.NonNull;

import org.odk.collect.android.dao.InstancesDao;

import java.io.File;

public class ExistingInstanceChecker {
    public boolean shouldDeleteExistingInstance(@NonNull File instancePath) {
        Cursor c = null;
        try {
            c = new InstancesDao().getInstancesCursorForFilePath(instancePath.getAbsolutePath());
            return c.getCount() < 1;

        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
