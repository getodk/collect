package org.odk.collect.android.formmanagement;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusRepository;
import org.odk.collect.android.forms.FormSourceException;
import org.odk.collect.android.forms.FormSourceException.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class SyncStatusRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void getSyncError_isNullAtFirst() {
        SyncStatusRepository syncStatusRepository = new SyncStatusRepository();
        assertThat(syncStatusRepository.getSyncError().getValue(), is(nullValue()));
    }

    @Test
    public void getSyncError_whenFinishSyncWithException_isException() {
        SyncStatusRepository syncStatusRepository = new SyncStatusRepository();
        syncStatusRepository.startSync();
        FormSourceException exception = new FormSourceException(Type.FETCH_ERROR);
        syncStatusRepository.finishSync(exception);

        assertThat(syncStatusRepository.getSyncError().getValue(), is(exception));
    }

    @Test
    public void getSyncError_whenFinishSyncWithNull_isNull() {
        SyncStatusRepository syncStatusRepository = new SyncStatusRepository();
        syncStatusRepository.startSync();
        syncStatusRepository.finishSync(null);

        assertThat(syncStatusRepository.getSyncError().getValue(), is(nullValue()));
    }
}