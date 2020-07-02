package odk.hedera.collect.activities.support;

import android.app.Activity;

import androidx.annotation.NonNull;

import odk.hedera.collect.listeners.PermissionListener;
import odk.hedera.collect.utilities.PermissionUtils;

public class AlwaysDenyStoragePermissionPermissionUtils extends PermissionUtils {

    @Override
    public void requestStoragePermissions(Activity activity, @NonNull PermissionListener action) {
        action.denied();
    }
}
