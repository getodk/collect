package org.odk.collect.android.formmanagement;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SyncStatusRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void startSync_returnsTrue() {
        SyncStatusRepository syncStatusRepository = new SyncStatusRepository();
        assertThat(syncStatusRepository.startSync(), is(true));
    }

    @Test
    public void startSync_whenSyncAlreadyStarted_returnsFalse() {
        SyncStatusRepository syncStatusRepository = new SyncStatusRepository();
        syncStatusRepository.startSync();

        assertThat(syncStatusRepository.startSync(), is(false));
    }

    @Test
    public void startSync_whenSyncFinished_returnsTrue() {
        SyncStatusRepository syncStatusRepository = new SyncStatusRepository();
        syncStatusRepository.startSync();
        syncStatusRepository.finishSync(true);

        assertThat(syncStatusRepository.startSync(), is(true));
    }

    @Test
    public void isOutOfSync_isFalseAtFirst() {
        SyncStatusRepository syncStatusRepository = new SyncStatusRepository();
        assertThat(syncStatusRepository.isOutOfSync().getValue(), is(false));
    }

    @Test
    public void isOutOfSync_whenFinishSyncWithFalse_isTrue() {
        SyncStatusRepository syncStatusRepository = new SyncStatusRepository();
        syncStatusRepository.startSync();
        syncStatusRepository.finishSync(false);

        assertThat(syncStatusRepository.isOutOfSync().getValue(), is(true));
    }

    @Test
    public void isOutOfSync_whenFinishSyncWithTrue_isFalse() {
        SyncStatusRepository syncStatusRepository = new SyncStatusRepository();
        syncStatusRepository.startSync();
        syncStatusRepository.finishSync(true);

        assertThat(syncStatusRepository.isOutOfSync().getValue(), is(false));
    }
}