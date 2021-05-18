package org.odk.collect.android.formmanagement;

import android.content.ContentResolver;
import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.forms.FormSourceException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SyncStatusAppStateTest {

    private final Context context = mock(Context.class);
    private final ContentResolver contentResolver = mock(ContentResolver.class);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setup() {
        when(context.getContentResolver()).thenReturn(contentResolver);
    }

    @Test
    public void getSyncError_isNullAtFirst() {
        SyncStatusAppState syncStatusAppState = new SyncStatusAppState(context);
        assertThat(syncStatusAppState.getSyncError().getValue(), is(nullValue()));
    }

    @Test
    public void getSyncError_whenFinishSyncWithException_isException() {
        SyncStatusAppState syncStatusAppState = new SyncStatusAppState(context);
        syncStatusAppState.startSync();
        FormSourceException exception = new FormSourceException.FetchError();
        syncStatusAppState.finishSync(exception);

        assertThat(syncStatusAppState.getSyncError().getValue(), is(exception));
    }

    @Test
    public void getSyncError_whenFinishSyncWithNull_isNull() {
        SyncStatusAppState syncStatusAppState = new SyncStatusAppState(context);
        syncStatusAppState.startSync();
        syncStatusAppState.finishSync(null);

        assertThat(syncStatusAppState.getSyncError().getValue(), is(nullValue()));
    }

    @Test
    public void finishSync_updatesFormsContentObserver() {
        SyncStatusAppState syncStatusAppState = new SyncStatusAppState(context);
        syncStatusAppState.startSync();
        syncStatusAppState.finishSync(null);
        verify(contentResolver).notifyChange(FormsProviderAPI.CONTENT_URI, null);
    }
}
