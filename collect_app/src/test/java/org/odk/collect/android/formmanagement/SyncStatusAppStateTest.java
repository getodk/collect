package org.odk.collect.android.formmanagement;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState;
import org.odk.collect.forms.FormSourceException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class SyncStatusAppStateTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void getSyncError_isNullAtFirst() {
        SyncStatusAppState syncStatusAppState = new SyncStatusAppState();
        assertThat(syncStatusAppState.getSyncError().getValue(), is(nullValue()));
    }

    @Test
    public void getSyncError_whenFinishSyncWithException_isException() {
        SyncStatusAppState syncStatusAppState = new SyncStatusAppState();
        syncStatusAppState.startSync();
        FormSourceException exception = new FormSourceException.FetchError();
        syncStatusAppState.finishSync(exception);

        assertThat(syncStatusAppState.getSyncError().getValue(), is(exception));
    }

    @Test
    public void getSyncError_whenFinishSyncWithNull_isNull() {
        SyncStatusAppState syncStatusAppState = new SyncStatusAppState();
        syncStatusAppState.startSync();
        syncStatusAppState.finishSync(null);

        assertThat(syncStatusAppState.getSyncError().getValue(), is(nullValue()));
    }
}
