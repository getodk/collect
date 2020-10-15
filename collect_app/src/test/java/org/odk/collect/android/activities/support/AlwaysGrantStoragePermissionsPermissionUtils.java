package org.odk.collect.android.activities.support;

import android.app.Activity;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.PermissionUtils;

public class AlwaysGrantStoragePermissionsPermissionUtils extends PermissionUtils {

    public AlwaysGrantStoragePermissionsPermissionUtils() {
        super(R.style.Theme_Collect_Dialog_PermissionAlert);
    }

    @Override
    public void requestStoragePermissions(Activity activity, @NonNull PermissionListener action) {
        action.granted();
    }
}
