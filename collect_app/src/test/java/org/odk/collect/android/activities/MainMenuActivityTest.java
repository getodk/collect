package org.odk.collect.android.activities;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.storage.migration.StorageMigrationResult;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.LooperMode;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowEnvironment;

import javax.inject.Singleton;

import dagger.Provides;

import static android.os.Environment.MEDIA_MOUNTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(RobolectricTestRunner.class)
@LooperMode(PAUSED)
public class MainMenuActivityTest {

    @Before
    public void setUp() throws Exception {
        ShadowEnvironment.setExternalStorageState(MEDIA_MOUNTED); // Required for ODK directories to be created
    }

    @Test
    public void pressingConfigureQRCode_launchesScanQRCodeActivity() throws Exception {
        MainMenuActivity activity = Robolectric.setupActivity(MainMenuActivity.class);

        MenuItem item = new RoboMenuItem(R.id.menu_configure_qr_code);
        activity.onOptionsItemSelected(item);

        Intent expectedIntent = new Intent(activity, ScanQRCodeActivity.class);
        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
        assertThat(expectedIntent.getComponent(), equalTo(actual.getComponent()));
    }

    @Test
    public void whenStorageMigrationIsFinished_storageCompletionBannerOnlyShowsInOneInstance() {
        whenStorageMigrationIsFinished();

        ActivityController<MainMenuActivity> activityController = Robolectric.buildActivity(MainMenuActivity.class).setup();
        assertThat(activityController.get().findViewById(R.id.storageMigrationBanner).getVisibility(), equalTo(View.VISIBLE));
        activityController.pause().stop().destroy();

        MainMenuActivity secondActivity = Robolectric.setupActivity(MainMenuActivity.class);
        assertThat(secondActivity.findViewById(R.id.storageMigrationBanner).getVisibility(), equalTo(View.GONE));
    }

    private void whenStorageMigrationIsFinished() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Provides
            @Singleton
            public StorageMigrationRepository providesStorageMigrationRepository() {
                StorageMigrationRepository storageMigrationRepository = new StorageMigrationRepository();
                storageMigrationRepository.setResult(StorageMigrationResult.SUCCESS);
                return storageMigrationRepository;
            }

            @Override
            public StorageStateProvider providesStorageStateProvider() {
                StorageStateProvider storageStateProvider = new StorageStateProvider();
                storageStateProvider.enableUsingScopedStorage();
                return storageStateProvider;
            }
        });
    }
}
