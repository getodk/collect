package org.odk.collect.android.support;

import android.content.Context;
import android.webkit.MimeTypeMap;

import androidx.test.espresso.IdlingResource;
import androidx.work.WorkManager;

import org.odk.collect.android.gdrive.DriveApi;
import org.odk.collect.android.gdrive.GoogleAccountPicker;
import org.odk.collect.android.gdrive.GoogleApiProvider;
import org.odk.collect.android.gdrive.SheetsApi;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.storage.migration.StorageMigrationService;
import org.odk.collect.async.Scheduler;
import org.odk.collect.utilities.UserAgentProvider;

import java.util.List;

import static java.util.Arrays.asList;

public class TestDependencies extends AppDependencyModule {

    private final CallbackCountingTaskExecutorRule countingTaskExecutorRule = new CallbackCountingTaskExecutorRule();

    public final StubOpenRosaServer server = new StubOpenRosaServer();
    public final TestScheduler scheduler = new TestScheduler();
    public final StubGoogleApi googleApi = new StubGoogleApi();

    public final List<IdlingResource> idlingResources = asList(
            new SchedulerIdlingResource(scheduler),
            new CountingTaskExecutorIdlingResource(countingTaskExecutorRule),
            new IntentServiceIdlingResource(StorageMigrationService.SERVICE_NAME)
    );

    @Override
    public OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap, UserAgentProvider userAgentProvider) {
        return server;
    }

    @Override
    public Scheduler providesScheduler(WorkManager workManager) {
        return scheduler;
    }

    @Override
    public GoogleApiProvider providesGoogleApiProvider() {
        return new GoogleApiProvider() {

            @Override
            public GoogleAccountPicker getAccountPicker(Context context) {
                return googleApi;
            }

            @Override
            public SheetsApi getSheetsApi(GoogleAccountPicker googleAccountPicker) {
                return googleApi;
            }

            @Override
            public DriveApi getDriveApi(GoogleAccountPicker googleAccountPicker) {
                return googleApi;
            }
        };
    }
}
