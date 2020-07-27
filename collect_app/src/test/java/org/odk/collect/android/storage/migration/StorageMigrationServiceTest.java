package org.odk.collect.android.storage.migration;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.reference.ReferenceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.backgroundwork.ChangeLock;
import org.odk.collect.android.backgroundwork.FormSubmitManager;
import org.odk.collect.android.backgroundwork.FormUpdateManager;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.support.BooleanChangeLock;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.Robolectric;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(AndroidJUnit4.class)
public class StorageMigrationServiceTest {

    private final StorageMigrator storageMigrator = mock(StorageMigrator.class);
    private final StorageMigrationRepository storageMigrationRepository = mock(StorageMigrationRepository.class);
    private final BooleanChangeLock changeLock = new BooleanChangeLock();

    @Before
    public void setup() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ChangeLock providesChangeLock() {
                return changeLock;
            }

            @Override
            public StorageMigrationRepository providesStorageMigrationRepository() {
                return storageMigrationRepository;
            }

            @Override
            public StorageMigrator providesStorageMigrator(StoragePathProvider storagePathProvider, StorageStateProvider storageStateProvider, StorageMigrationRepository storageMigrationRepository, ReferenceManager referenceManager, FormUpdateManager formUpdateManager, FormSubmitManager formSubmitManager, Analytics analytics, ChangeLock changeLock) {
                return storageMigrator;
            }
        });
    }

    @Test
    public void whenChangeLockLocked_doesNotMigrate_andSetsResultInRepository() {
        changeLock.lock();

        StorageMigrationService service = Robolectric.setupIntentService(StorageMigrationService.class);
        service.onHandleIntent(null);

        verifyNoInteractions(storageMigrator);
        verify(storageMigrationRepository).setResult(StorageMigrationResult.CHANGES_IN_PROGRESS);
        verify(storageMigrationRepository).markMigrationEnd();
    }
}