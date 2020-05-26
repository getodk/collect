package org.odk.collect.android.activities.viewmodels;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.analytics.AnalyticsEvents;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class FormDownloadListViewModelTest {

    private final Analytics analytics = mock(Analytics.class);

    private FormDownloadListViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new FormDownloadListViewModel(analytics);
    }

    @Test
    public void logDownloadAnalyticsEvent_whenNoFormsAreOnDevice_logsFirstDownload() {
        viewModel.logDownloadAnalyticsEvent(0, "https://server.example.com");
        verify(analytics).logEvent(eq(AnalyticsEvents.FIRST_FORM_DOWNLOAD), anyString());
    }

    @Test
    public void logDownloadAnalyticsEvent_whenFormsAreOnDevice_logsSubsequentDownload() {
        viewModel.logDownloadAnalyticsEvent(1, "https://server.example.com");
        verify(analytics).logEvent(eq(AnalyticsEvents.SUBSEQUENT_FORM_DOWNLOAD), anyString());
    }

    @Test
    public void logDownloadAnalyticsEvent_logsSelectedFormAndTotalFormCountsAndServerHashAsAction() {
        viewModel.addForm(new HashMap<>());
        viewModel.addForm(new HashMap<>());
        viewModel.addForm(new HashMap<>());

        viewModel.addSelectedFormId("foo");
        viewModel.addSelectedFormId("bar");

        viewModel.logDownloadAnalyticsEvent(0, "https://server.example.com");
        verify(analytics).logEvent(anyString(), eq(
                "2/3-e4534130b01d6707a431adbec4e03912" // Ends with hash of server URL
        ));
    }
}
