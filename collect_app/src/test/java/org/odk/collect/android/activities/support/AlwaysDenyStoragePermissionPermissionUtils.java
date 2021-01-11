package org.odk.collect.android.activities.support;

import android.app.Activity;

import androidx.annotation.NonNull;

import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.permissions.PermissionsChecker;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.utilities.PermissionUtils;

public class AlwaysDenyStoragePermissionPermissionUtils extends PermissionUtils {

    public AlwaysDenyStoragePermissionPermissionUtils(PermissionsChecker permissionsChecker, StorageStateProvider storageStateProvider) {
        super(permissionsChecker, storageStateProvider);
    }

    @Override
    public void requestStoragePermissions(Activity activity, @NonNull PermissionListener action) {
        action.denied();
    }
}
