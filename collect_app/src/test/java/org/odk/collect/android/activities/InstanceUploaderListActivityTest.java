package org.odk.collect.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import androidx.annotation.NonNull;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.PermissionUtils;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowAlertDialog;

import androidx.work.Configuration;
import androidx.work.WorkManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.support.RobolectricHelpers.overrideAppDependencyModule;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class InstanceUploaderListActivityTest {

    Tracker tracker;

    @Before
    public void setup() {
        WorkManager.initialize(RuntimeEnvironment.application, new Configuration.Builder().build());

        tracker = mock(Tracker.class);
        overrideAppDependencyModule(new AppDependencyModule(
                tracker,
                new AlwaysGrantStoragePermissionsPermissionUtils()
        ));
    }

    @Test
    public void clickingChangeView_thenClickingShowAll_sendsAnalyticsEvent() {
        InstanceUploaderListActivity activity = Robolectric.setupActivity(InstanceUploaderListActivity.class);

        activity.onOptionsItemSelected(new RoboMenuItem(R.id.menu_change_view));
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        shadowOf(dialog).clickOnItem(1);

        verify(tracker).send(new HitBuilders.EventBuilder()
                .setCategory("FilterSendForms")
                .setAction("SentAndUnsent")
                .build());
    }

    private static class AppDependencyModule extends org.odk.collect.android.injection.config.AppDependencyModule {

        private final Tracker tracker;
        private final PermissionUtils permissionUtils;

        private AppDependencyModule(Tracker tracker, PermissionUtils permissionUtils) {
            this.tracker = tracker;
            this.permissionUtils = permissionUtils;
        }

        @Override
        public Tracker providesTracker(Application application) {
            return tracker;
        }

        @Override
        public PermissionUtils providesPermissionUtils() {
            return permissionUtils;
        }
    }

    private static class AlwaysGrantStoragePermissionsPermissionUtils extends PermissionUtils {

        @Override
        public void requestStoragePermissions(Activity activity, @NonNull PermissionListener action) {
            action.granted();
        }
    }
}
