package org.odk.collect.android.fakes;

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
public class FakePermissionUtils extends PermissionUtils {

    private boolean isPermissionGranted;

    public FakePermissionUtils(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    protected void requestPermissions(@NonNull PermissionListener listener, String... permissions) {
        if (isPermissionGranted) {
            listener.granted();
        } else {
            listener.denied();
        }
    }

    @Override
    protected void showAdditionalExplanation(int title, int message, int drawable, @NonNull PermissionListener action) {
        action.denied();
    }

    public void setPermissionGranted(boolean permissionGranted) {
        isPermissionGranted = permissionGranted;
    }
}
