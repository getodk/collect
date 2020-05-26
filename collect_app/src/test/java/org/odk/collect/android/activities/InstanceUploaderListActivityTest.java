package org.odk.collect.android.activities;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.utilities.PermissionUtils;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowAlertDialog;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.support.RobolectricHelpers.overrideAppDependencyModule;

@RunWith(RobolectricTestRunner.class)
public class InstanceUploaderListActivityTest {

    Analytics analytics;

    @Before
    public void setup() {
        WorkManager.initialize(RuntimeEnvironment.application, new Configuration.Builder().build());

        analytics = mock(Analytics.class);
        overrideAppDependencyModule(new AppDependencyModule(
                analytics,
                new AlwaysGrantStoragePermissionsPermissionUtils()
        ));
    }

    @Test
    public void clickingChangeView_thenClickingShowAll_sendsAnalyticsEvent() {
        InstanceUploaderListActivity activity = Robolectric.setupActivity(InstanceUploaderListActivity.class);

        activity.onOptionsItemSelected(new RoboMenuItem(R.id.menu_change_view));
        AlertDialog dialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        dialog.getListView().performItemClick(null, 1, 0L);

        verify(analytics).logEvent("FilterSendForms", "SentAndUnsent");
    }

    private static class AppDependencyModule extends org.odk.collect.android.injection.config.AppDependencyModule {

        private final Analytics tracker;
        private final PermissionUtils permissionUtils;

        private AppDependencyModule(Analytics tracker, PermissionUtils permissionUtils) {
            this.tracker = tracker;
            this.permissionUtils = permissionUtils;
        }

        @Override
        public Analytics providesAnalytics(Application application, GeneralSharedPreferences generalSharedPreferences) {
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
