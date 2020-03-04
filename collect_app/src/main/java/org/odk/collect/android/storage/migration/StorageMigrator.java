package org.odk.collect.android.storage.migration;

import android.content.ContentValues;
import android.database.Cursor;

import org.apache.commons.io.FileUtils;
import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.provider.FormsProvider;
import org.odk.collect.android.provider.InstanceProvider;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.tasks.ServerPollingJob;
import org.odk.collect.android.upload.AutoSendWorker;
import org.odk.collect.utilities.BackgroundWorkManager;

import java.io.File;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.analytics.AnalyticsEvents.SCOPED_STORAGE_MIGRATION;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_REFERENCE_LAYER;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH;

public class StorageMigrator {
    private static final String WHERE_ID = _ID + "=?";

    private final StoragePathProvider storagePathProvider;
    private final StorageStateProvider storageStateProvider;
    private final StorageEraser storageEraser;
    private final GeneralSharedPreferences generalSharedPreferences;
    private final ReferenceManager referenceManager;

    private final StorageMigrationRepository storageMigrationRepository;
    private final BackgroundWorkManager backgroundWorkManager;

    private final Analytics analytics;

    public StorageMigrator(StoragePathProvider storagePathProvider, StorageStateProvider storageStateProvider,
                           StorageEraser storageEraser, StorageMigrationRepository storageMigrationRepository,
                           GeneralSharedPreferences generalSharedPreferences, ReferenceManager referenceManager,
                           BackgroundWorkManager workManager, Analytics analytics) {

        this.storagePathProvider = storagePathProvider;
        this.storageStateProvider = storageStateProvider;
        this.storageEraser = storageEraser;
        this.storageMigrationRepository = storageMigrationRepository;
        this.generalSharedPreferences = generalSharedPreferences;
        this.referenceManager = referenceManager;
        this.backgroundWorkManager = workManager;
        this.analytics = analytics;
    }

    void performStorageMigration() {
        storageMigrationRepository.markMigrationStart();
        StorageMigrationResult result = migrate();
        storageMigrationRepository.setResult(result);
        storageMigrationRepository.markMigrationEnd();

        analytics.logEvent(SCOPED_STORAGE_MIGRATION, result.toString());
    }

    public StorageMigrationResult migrate() {
        storageEraser.clearOdkDirOnScopedStorage();

        if (isFormUploaderRunning()) {
            return StorageMigrationResult.FORM_UPLOADER_IS_RUNNING;
        }

        if (isFormDownloaderRunning()) {
            return StorageMigrationResult.FORM_DOWNLOADER_IS_RUNNING;
        }

        if (!storageStateProvider.isEnoughSpaceToPerformMigration(storagePathProvider)) {
            return StorageMigrationResult.NOT_ENOUGH_SPACE;
        }

        if (!moveAppDataToScopedStorage()) {
            return StorageMigrationResult.MOVING_FILES_FAILED;
        }

        storageStateProvider.enableUsingScopedStorage();
        reopenDatabases();
        if (!migrateDatabasePaths()) {
            storageStateProvider.disableUsingScopedStorage();
            reopenDatabases();
            return StorageMigrationResult.MOVING_FILES_FAILED;
        }

        migrateMapLayerPath();
        referenceManager.reset();
        storageEraser.removeItemsetsDb();
        storageEraser.clearCache();
        storageEraser.deleteOdkDirFromUnscopedStorage();

        return StorageMigrationResult.SUCCESS;
    }

    private boolean isFormUploaderRunning() {
        return backgroundWorkManager.isRunning(AutoSendWorker.TAG);
    }

    private boolean isFormDownloaderRunning() {
        return backgroundWorkManager.isRunning(ServerPollingJob.TAG);
    }

    boolean moveAppDataToScopedStorage() {
        try {
            File odkDirOnUnscopedStorage = new File(storagePathProvider.getUnscopedStorageRootDirPath());
            File odkDirOnScopedStorage = new File(storagePathProvider.getScopedStorageRootDirPath());
            FileUtils.copyDirectory(odkDirOnUnscopedStorage, odkDirOnScopedStorage);
        } catch (Exception | Error e) {
            Timber.w(e);
            return false;
        }
        return true;
    }

    boolean migrateDatabasePaths() {
        try {
            migrateFormsDatabase();
            migrateInstancesDatabase();
        } catch (Exception | Error e) {
            Timber.w(e);
            return false;
        }
        return true;
    }

    void reopenDatabases() {
        FormsProvider.recreateDatabaseHelper();
        InstanceProvider.recreateDatabaseHelper();
    }

    private void migrateInstancesDatabase() {
        InstancesDao instancesDao = new InstancesDao();
        Cursor cursor = instancesDao.getInstancesCursor();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idColumnIndex = cursor.getColumnIndex(_ID);
                int instanceFilePathIndex = cursor.getColumnIndex(INSTANCE_FILE_PATH);

                ContentValues values = new ContentValues();
                values.put(INSTANCE_FILE_PATH, getRelativeInstanceDbPath(cursor.getString(instanceFilePathIndex)));

                String[] whereArgs = {String.valueOf(cursor.getLong(idColumnIndex))};
                instancesDao.updateInstance(values, WHERE_ID, whereArgs);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private void migrateFormsDatabase() {
        FormsDao formsDao = new FormsDao();
        Cursor cursor = formsDao.getFormsCursor();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idColumnIndex = cursor.getColumnIndex(_ID);
                int formFilePathColumnIndex = cursor.getColumnIndex(FORM_FILE_PATH);
                int formMediaPathColumnIndex = cursor.getColumnIndex(FORM_MEDIA_PATH);
                int jrCacheFilePathColumnIndex = cursor.getColumnIndex(JRCACHE_FILE_PATH);

                ContentValues values = new ContentValues();
                values.put(FORM_FILE_PATH, getRelativeFormDbPath(cursor.getString(formFilePathColumnIndex)));
                values.put(FORM_MEDIA_PATH, getRelativeFormDbPath(cursor.getString(formMediaPathColumnIndex)));
                values.put(JRCACHE_FILE_PATH, getRelativeCacheDbPath(cursor.getString(jrCacheFilePathColumnIndex)));

                String[] whereArgs = {String.valueOf(cursor.getLong(idColumnIndex))};
                formsDao.updateForm(values, WHERE_ID, whereArgs);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private String getRelativeFormDbPath(String path) {
        return storagePathProvider.getRelativeFilePath(storagePathProvider.getUnscopedStorageDirPath(StorageSubdirectory.FORMS), path);
    }

    private String getRelativeInstanceDbPath(String path) {
        return storagePathProvider.getRelativeFilePath(storagePathProvider.getUnscopedStorageDirPath(StorageSubdirectory.INSTANCES), path);
    }

    private String getRelativeCacheDbPath(String path) {
        return storagePathProvider.getRelativeFilePath(storagePathProvider.getUnscopedStorageDirPath(StorageSubdirectory.CACHE), path);
    }

    private void migrateMapLayerPath() {
        try {
            String layerPath = (String) generalSharedPreferences.get(KEY_REFERENCE_LAYER);
            if (layerPath != null && !layerPath.isEmpty()) {
                generalSharedPreferences.save(KEY_REFERENCE_LAYER, storagePathProvider.getRelativeMapLayerPath(layerPath));
            }
        } catch (Exception | Error e) {
            generalSharedPreferences.reset(KEY_REFERENCE_LAYER);
        }
    }
}