package org.odk.collect.android.activities.support;

import android.app.Activity;

import androidx.annotation.NonNull;

import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.permissions.PermissionsChecker;
import org.odk.collect.android.permissions.PermissionsProvider;

public class AlwaysGrantStoragePermissionsPermissionsProvider extends PermissionsProvider {

    public AlwaysGrantStoragePermissionsPermissionsProvider(PermissionsChecker permissionsChecker) {
        super(permissionsChecker);
    }

    @Override
    public void requestStoragePermissions(Activity activity, @NonNull PermissionListener action) {
        action.granted();
    }
}
