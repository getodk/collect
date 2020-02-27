package org.odk.collect.android.storage;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobManagerCreateException;
import com.evernote.android.job.JobRequest;

import org.javarosa.core.reference.ReferenceManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.storage.migration.StorageEraser;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.storage.migration.StorageMigrationResult;
import org.odk.collect.android.storage.migration.StorageMigrator;
import org.odk.collect.android.storage.utils.FakedAutoSendWorker;
import org.odk.collect.android.storage.utils.FakedServerPollingJob;
import org.odk.collect.android.tasks.ServerPollingJob;
import org.odk.collect.android.upload.AutoSendWorker;

import timber.log.Timber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class StorageMigratorTest {

    private StorageMigrator storageMigrator;
    private final StoragePathProvider storagePathProvider = spy(StoragePathProvider.class);
    private final StorageStateProvider storageStateProvider = mock(StorageStateProvider.class);
    private final StorageEraser storageEraser = mock(StorageEraser.class);
    private final StorageMigrationRepository storageMigrationRepository = mock(StorageMigrationRepository.class);
    private final GeneralSharedPreferences generalSharedPreferences = mock(GeneralSharedPreferences.class);
    private final ReferenceManager referenceManager = mock(ReferenceManager.class);

    @BeforeClass
    public static void setUpJobCreator() {
        try {
            JobManager.instance().removeJobCreator(Collect.getInstance().collectJobCreator);
            JobManager.create(Collect.getInstance()).addJobCreator(s -> new FakedServerPollingJob());
        } catch (JobManagerCreateException e) {
            Timber.e(e);
        }
    }

    @Before
    public void setup() {
        storageMigrator = spy(new StorageMigrator(storagePathProvider, storageStateProvider, storageEraser, storageMigrationRepository, generalSharedPreferences, referenceManager));
    }

    @Test
    public void when_formUploaderIsRunning_should_storageMigratorReturn_FORM_UPLOADER_IS_RUNNING() {
        assertThat(storageMigrator.migrate(), is(not(StorageMigrationResult.FORM_UPLOADER_IS_RUNNING)));

        startFakedAutoSendWorker();

        wait(1);

        assertThat(storageMigrator.migrate(), is(StorageMigrationResult.FORM_UPLOADER_IS_RUNNING));

        wait(5);

        assertThat(storageMigrator.migrate(), is(not(StorageMigrationResult.FORM_UPLOADER_IS_RUNNING)));
    }

    @Test
    public void when_formDownloaderIsRunning_should_storageMigratorReturn_FORM_DOWNLOADER_IS_RUNNING() {
        assertThat(storageMigrator.migrate(), is(not(StorageMigrationResult.FORM_DOWNLOADER_IS_RUNNING)));

        startFakedServerPollingJob();

        wait(1);

        assertThat(storageMigrator.migrate(), is(StorageMigrationResult.FORM_DOWNLOADER_IS_RUNNING));

        wait(5);

        assertThat(storageMigrator.migrate(), is(not(StorageMigrationResult.FORM_DOWNLOADER_IS_RUNNING)));
    }

    private void startFakedAutoSendWorker() {
        OneTimeWorkRequest autoSendWork = new OneTimeWorkRequest.Builder(FakedAutoSendWorker.class)
                .addTag(AutoSendWorker.class.getName())
                .build();

        WorkManager
                .getInstance()
                .beginUniqueWork(FakedAutoSendWorker.class.getName(), ExistingWorkPolicy.KEEP, autoSendWork)
                .enqueue();
    }

    private void startFakedServerPollingJob() {
        new JobRequest.Builder(ServerPollingJob.TAG)
                .startNow()
                .build()
                .schedule();
    }

    private void wait(int sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            Timber.i(e);
        }
    }
}

