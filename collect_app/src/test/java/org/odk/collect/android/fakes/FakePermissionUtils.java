package org.odk.collect.android.fakes;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.permissions.PermissionsChecker;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.permissions.PermissionUtils;

/**
 * Mocked implementation of {@link PermissionUtils}.
 * The runtime permissions can be stubbed for unit testing
 *
 * @author Shobhit Agarwal
 */
public class FakePermissionUtils extends PermissionUtils {

    private boolean isPermissionGranted;

    public FakePermissionUtils() {
        super(new PermissionsChecker(InstrumentationRegistry.getInstrumentation().getTargetContext()), new StorageStateProvider());
    }

    @Override
    protected void requestPermissions(Activity activity, @NonNull PermissionListener listener, String... permissions) {
        if (isPermissionGranted) {
            listener.granted();
        } else {
            listener.denied();
        }
    }

    @Override
    protected void showAdditionalExplanation(Activity activity, int title, int message, int drawable, @NonNull PermissionListener action) {
        action.denied();
    }

    public void setPermissionGranted(boolean permissionGranted) {
        isPermissionGranted = permissionGranted;
    }
}
