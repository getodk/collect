package org.odk.collect.android.formmanagement;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SyncStatusRepositoryTest {

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
        syncStatusRepository.finishSync();

        assertThat(syncStatusRepository.startSync(), is(true));
    }
}