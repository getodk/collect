package org.odk.collect.android.storage.migration;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageStateProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class StorageMigratorTest {

    private StorageMigrator storageMigrator;
    private final StorageEraser storageEraser = spy(StorageEraser.class);
    private final StoragePathProvider storagePathProvider = spy(StoragePathProvider.class);
    private final StorageStateProvider storageStateProvider = mock(StorageStateProvider.class);

    @Before
    public void setup() {
        storageMigrator = spy(new StorageMigrator(storagePathProvider, storageStateProvider, storageEraser));
        doNothing().when(storageMigrator).reopenDatabases();
        doNothing().when(storageEraser).clearOdkDirOnScopedStorage(storagePathProvider);
        doNothing().when(storageEraser).deleteOdkDirFromUnscopedStorage(storagePathProvider);
    }

    @Test
    public void when_formUploaderIsRunning_should_appropriateResultBeReturned() {
        doReturn(true).when(storageMigrator).isFormUploaderRunning();

        assertThat(storageMigrator.performStorageMigration(), is(StorageMigrationResult.FORM_UPLOADER_IS_RUNNING));
    }

    @Test
    public void when_formDownloaderIsRunning_should_appropriateResultBeReturned() {
        doReturn(false).when(storageMigrator).isFormUploaderRunning();
        doReturn(true).when(storageMigrator).isFormDownloaderRunning();

        assertThat(storageMigrator.performStorageMigration(), is(StorageMigrationResult.FORM_DOWNLOADER_IS_RUNNING));
    }

    @Test
    public void when_thereIsNoEnoughSpaceToPerformMigration_should_appropriateResultBeReturned() {
        doReturn(false).when(storageMigrator).isFormUploaderRunning();
        doReturn(false).when(storageMigrator).isFormDownloaderRunning();
        doReturn(false).when(storageStateProvider).isEnoughSpaceToPerformMigartion(storagePathProvider);

        assertThat(storageMigrator.performStorageMigration(), is(StorageMigrationResult.NOT_ENOUGH_SPACE));
    }

    @Test
    public void when_anyExceptionIsThrownDuringMovingFiles_should_appropriateResultBeReturned() {
        doReturn(false).when(storageMigrator).isFormUploaderRunning();
        doReturn(false).when(storageMigrator).isFormDownloaderRunning();
        doReturn(true).when(storageStateProvider).isEnoughSpaceToPerformMigartion(storagePathProvider);
        doReturn(StorageMigrationResult.MOVING_FILES_FAILED).when(storageMigrator).moveAppDataToScopedStorage();

        assertThat(storageMigrator.performStorageMigration(), is(StorageMigrationResult.MOVING_FILES_FAILED));
    }

    @Test
    public void when_anyExceptionIsThrownDuringMigratingDatabases_should_appropriateResultBeReturned() {
        doReturn(false).when(storageMigrator).isFormUploaderRunning();
        doReturn(false).when(storageMigrator).isFormDownloaderRunning();
        doReturn(true).when(storageStateProvider).isEnoughSpaceToPerformMigartion(storagePathProvider);
        doReturn(StorageMigrationResult.MOVING_FILES_SUCCEEDED).when(storageMigrator).moveAppDataToScopedStorage();
        doReturn(StorageMigrationResult.MIGRATING_DATABASE_PATHS_FAILED).when(storageMigrator).migrateDatabasePaths();

        assertThat(storageMigrator.performStorageMigration(), is(StorageMigrationResult.MIGRATING_DATABASE_PATHS_FAILED));
    }

    @Test
    public void when_migrationFinishedWithSuccess_should_appropriateResultBeReturned() {
        doReturn(false).when(storageMigrator).isFormUploaderRunning();
        doReturn(false).when(storageMigrator).isFormDownloaderRunning();
        doReturn(true).when(storageStateProvider).isEnoughSpaceToPerformMigartion(storagePathProvider);
        doReturn(StorageMigrationResult.MOVING_FILES_SUCCEEDED).when(storageMigrator).moveAppDataToScopedStorage();
        doReturn(StorageMigrationResult.MIGRATING_DATABASE_PATHS_SUCCEEDED).when(storageMigrator).migrateDatabasePaths();

        assertThat(storageMigrator.performStorageMigration(), is(StorageMigrationResult.SUCCESS));
    }

    @Test
    public void when_migrationStarts_should_scopedStorageBeCleared() {
        doReturn(true).when(storageMigrator).isFormUploaderRunning();

        storageMigrator.performStorageMigration();

        verify(storageEraser).clearOdkDirOnScopedStorage(storagePathProvider);
    }

    @Test
    public void when_movingFilesIsFinished_should_scopedStorageBeEnabled() {
        doReturn(false).when(storageMigrator).isFormUploaderRunning();
        doReturn(false).when(storageMigrator).isFormDownloaderRunning();
        doReturn(true).when(storageStateProvider).isEnoughSpaceToPerformMigartion(storagePathProvider);
        doReturn(StorageMigrationResult.MOVING_FILES_SUCCEEDED).when(storageMigrator).moveAppDataToScopedStorage();
        doReturn(StorageMigrationResult.MIGRATING_DATABASE_PATHS_FAILED).when(storageMigrator).migrateDatabasePaths();

        storageMigrator.performStorageMigration();

        verify(storageStateProvider).enableUsingScopedStorage();
    }

    @Test
    public void when_movingFilesIsFinished_should_databasesBeReopened() {
        doReturn(false).when(storageMigrator).isFormUploaderRunning();
        doReturn(false).when(storageMigrator).isFormDownloaderRunning();
        doReturn(true).when(storageStateProvider).isEnoughSpaceToPerformMigartion(storagePathProvider);
        doReturn(StorageMigrationResult.MOVING_FILES_SUCCEEDED).when(storageMigrator).moveAppDataToScopedStorage();
        doReturn(StorageMigrationResult.MIGRATING_DATABASE_PATHS_SUCCEEDED).when(storageMigrator).migrateDatabasePaths();

        storageMigrator.performStorageMigration();

        verify(storageMigrator).reopenDatabases();
    }

    @Test
    public void when_movingFilesFailed_should_databasesBeReopenedAgain() {
        doReturn(false).when(storageMigrator).isFormUploaderRunning();
        doReturn(false).when(storageMigrator).isFormDownloaderRunning();
        doReturn(true).when(storageStateProvider).isEnoughSpaceToPerformMigartion(storagePathProvider);
        doReturn(StorageMigrationResult.MOVING_FILES_SUCCEEDED).when(storageMigrator).moveAppDataToScopedStorage();
        doReturn(StorageMigrationResult.MIGRATING_DATABASE_PATHS_FAILED).when(storageMigrator).migrateDatabasePaths();

        storageMigrator.performStorageMigration();

        verify(storageMigrator, times(2)).reopenDatabases();
    }

    @Test
    public void when_movingFilesFailed_should_scopedStorageBeDisabled() {
        doReturn(false).when(storageMigrator).isFormUploaderRunning();
        doReturn(false).when(storageMigrator).isFormDownloaderRunning();
        doReturn(true).when(storageStateProvider).isEnoughSpaceToPerformMigartion(storagePathProvider);
        doReturn(StorageMigrationResult.MOVING_FILES_SUCCEEDED).when(storageMigrator).moveAppDataToScopedStorage();
        doReturn(StorageMigrationResult.MIGRATING_DATABASE_PATHS_FAILED).when(storageMigrator).migrateDatabasePaths();

        storageMigrator.performStorageMigration();

        verify(storageStateProvider).disableUsingScopedStorage();
    }

    @Test
    public void when_migrationFinished_should_oldOdkDirBeRemoved() {
        doReturn(false).when(storageMigrator).isFormUploaderRunning();
        doReturn(false).when(storageMigrator).isFormDownloaderRunning();
        doReturn(true).when(storageStateProvider).isEnoughSpaceToPerformMigartion(storagePathProvider);
        doReturn(StorageMigrationResult.MOVING_FILES_SUCCEEDED).when(storageMigrator).moveAppDataToScopedStorage();
        doReturn(StorageMigrationResult.MIGRATING_DATABASE_PATHS_SUCCEEDED).when(storageMigrator).migrateDatabasePaths();

        storageMigrator.performStorageMigration();

        verify(storageEraser).deleteOdkDirFromUnscopedStorage(storagePathProvider);
    }
}
