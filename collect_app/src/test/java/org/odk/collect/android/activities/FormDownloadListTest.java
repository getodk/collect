package org.odk.collect.android.activities;

import android.os.AsyncTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.tasks.DownloadFormListTask;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for checking form loading in {@link FormDownloadList}
 */
@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class FormDownloadListTest {

    private FormDownloadList formDownloadList;

    // Mocks
    private DownloadFormListTask downloadFormListTask = Mockito.mock(DownloadFormListTask.class);

    /**
     * Runs {@link Before} each test.
     */
    @Before
    public void setUp() throws Exception {
        formDownloadList = Robolectric.setupActivity(FormDownloadList.class);
    }

    @Test
    public void nonNullActivityTest() throws Exception {
        assertNotNull(formDownloadList);
    }

    @Test
    public void loadingWhenDownloadFormListTaskNullTest() throws Exception {
        formDownloadList.setPreparedDownloadFormListTask(downloadFormListTask);

        formDownloadList.downloadFormList();

        verify(downloadFormListTask).setDownloaderListener(formDownloadList);
        verify(downloadFormListTask).execute();
    }

    @Test
    public void loadingWhenDownloadFormListTaskNonNullAndFinishedTest() throws Exception {

        when(downloadFormListTask.getStatus()).thenReturn(AsyncTask.Status.FINISHED);

        formDownloadList.setDownloadFormListTask(downloadFormListTask);

        formDownloadList.downloadFormList();

        verify(downloadFormListTask).setDownloaderListener(null);
        verify(downloadFormListTask).cancel(true);
    }

    @Test
    public void loadingWhenDownloadFormListTaskNonNullTest() throws Exception {

        when(downloadFormListTask.getStatus()).thenReturn(AsyncTask.Status.RUNNING);

        formDownloadList.setDownloadFormListTask(downloadFormListTask);

        formDownloadList.downloadFormList();

        verify(downloadFormListTask, never()).setDownloaderListener(null);
        verify(downloadFormListTask, never()).cancel(true);
        verify(downloadFormListTask, never()).execute();
    }

}