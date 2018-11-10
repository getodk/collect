package org.odk.collect.android.mocks;

import android.app.Activity;
import android.support.annotation.NonNull;

import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.PermissionUtils;

/**
 * Mocked implementation of {@link PermissionUtils}.
 * The runtime permissions can be stubbed for unit testing
 *
 * @author Shobhit Agarwal
 */
public class MockedPermissionUtils extends PermissionUtils {

    private boolean isPermissionGranted;

    public MockedPermissionUtils(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public void requestPermissions(@NonNull PermissionListener listener, String... permissions) {
        if (isPermissionGranted) {
            listener.granted();
        } else {
            listener.denied();
        }
    }

    public void setPermissionGranted(boolean permissionGranted) {
        isPermissionGranted = permissionGranted;
    }
}
