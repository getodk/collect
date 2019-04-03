package org.odk.collect.android.formdownload;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.os.Bundle;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.ui.formdownload.AlertDialogUiModel;
import org.odk.collect.android.ui.formdownload.FormDownloadNavigator;
import org.odk.collect.android.ui.formdownload.FormDownloadRepository;
import org.odk.collect.android.ui.formdownload.FormDownloadViewModel;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.NetworkUtils;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.android.utilities.providers.BaseResourceProvider;
import org.odk.collect.android.utilities.providers.ResourceProvider;
import org.odk.collect.android.utilities.rx.TestSchedulerProvider;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.ui.formdownload.FormDownloadActivity.getDownloadResultMessage;
import static org.odk.collect.android.utilities.DownloadFormListUtils.DL_AUTH_REQUIRED;
import static org.odk.collect.android.utilities.LiveDataTestUtil.getValue;

@RunWith(RobolectricTestRunner.class)
public class FormDownloadViewModelTest {

    // Executes each task synchronously using Architecture Components
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private FormDownloadViewModel viewModel;

    private NetworkUtils mockNetworkUtils;
    private FormDownloadRepository mockFormDownloadRepository;
    private BaseResourceProvider testResourceProvider;

    @Before
    public void setUp() {
        mockNetworkUtils = Mockito.mock(NetworkUtils.class);
        mockFormDownloadRepository = Mockito.spy(FormDownloadRepository.class);
        testResourceProvider = new ResourceProvider(RuntimeEnvironment.application.getApplicationContext());

        WebCredentialsUtils mockWebCredentialsUtils = Mockito.mock(WebCredentialsUtils.class);

        viewModel = new FormDownloadViewModel(new TestSchedulerProvider(), mockNetworkUtils, testResourceProvider, mockFormDownloadRepository, mockWebCredentialsUtils);

        // prepare the spy!
        viewModel.setNavigator(Mockito.spy(FormDownloadNavigator.class));
    }

    @Test
    public void doNotDisplayDialogsOnInitTest() throws InterruptedException {
        Assert.assertNull(getValue(viewModel.getAlertDialog()));
        Assert.assertNull(getValue(viewModel.getProgressDialog()));
    }

    @Test
    public void displaySameAlertDialogOnReopenTest() throws InterruptedException {
        AlertDialogUiModel expectedModel = new AlertDialogUiModel("Title", "Message", false);

        viewModel.setAlertDialog(expectedModel.getTitle(), expectedModel.getMessage(), expectedModel.shouldExit());

        // now assuming that the activity was recreated. So, the subject was resubscribed.
        AlertDialogUiModel actualModel = getValue(viewModel.getAlertDialog());

        Assert.assertEquals(expectedModel.getTitle(), actualModel.getTitle());
        Assert.assertEquals(expectedModel.getMessage(), actualModel.getMessage());
        Assert.assertEquals(expectedModel.shouldExit(), actualModel.shouldExit());
    }

    @Test
    public void doNotRestoreLastShownAlertDialogIfRemovedTest() throws InterruptedException {
        AlertDialogUiModel expectedModel = new AlertDialogUiModel("Title", "Message", false);

        viewModel.setAlertDialog(expectedModel.getTitle(), expectedModel.getMessage(), expectedModel.shouldExit());
        viewModel.removeAlertDialog();

        // now assuming that the activity was recreated. So, the subject was resubscribed.
        Assert.assertNull(getValue(viewModel.getAlertDialog()));
    }

    @Test
    public void displayProgressDialogTest() throws InterruptedException {
        for (boolean value : new boolean[]{true, false, true}) {
            viewModel.setProgressDialogShowing(value);
            Assert.assertEquals(value, getValue(viewModel.getProgressDialog()));
        }
    }

    @Test
    public void displayCancelDialogTest() throws InterruptedException {
        viewModel.setCancelDialogShowing(true);
        viewModel.setCancelDialogShowing(false);
        viewModel.setCancelDialogShowing(true);

        Assert.assertEquals(getValue(viewModel.getCancelDialog()), true);
    }

    @Test
    public void displayLastShownProgressMessageTest() throws InterruptedException {
        viewModel.setProgressDialogMessage("Progress 1");

        Assert.assertEquals("Progress 1", getValue(viewModel.getProgressDialogMessage()));
    }

    @Test
    public void finishActivityIfFormIdsAreNull() {
        // verify that nothing happens if a null bundle is used
        viewModel.restoreState(null);
        Mockito.verify(viewModel.getNavigator(), times(0)).setReturnResult(false, "Form Ids is null", null);
        Mockito.verify(viewModel.getNavigator(), times(0)).goBack();

        // use bundle with null form ids for initialization
        Bundle bundle = new Bundle();
        bundle.putStringArray(ApplicationConstants.BundleKeys.FORM_IDS, null);
        viewModel.restoreState(bundle);

        // assert that result was set to false and activity was finished
        Mockito.verify(viewModel.getNavigator(), times(1)).setReturnResult(false, "Form Ids is null", null);
        Mockito.verify(viewModel.getNavigator(), times(1)).goBack();
    }

    @Test
    public void loadDataFromBundleTest() {
        Bundle bundle = new Bundle();
        bundle.putStringArray(ApplicationConstants.BundleKeys.FORM_IDS, new String[0]);
        bundle.putString(ApplicationConstants.BundleKeys.URL, "someurl");
        bundle.putString(ApplicationConstants.BundleKeys.USERNAME, "username");
        bundle.putString(ApplicationConstants.BundleKeys.PASSWORD, "password");

        viewModel.restoreState(bundle);

        Mockito.verify(viewModel.getNavigator(), times(0)).goBack();
        Assert.assertEquals("someurl", viewModel.getUrl());
        Assert.assertEquals("username", viewModel.getUsername());
        Assert.assertEquals("password", viewModel.getPassword());
    }

    @Test
    public void loadFormListDownloadTaskIfNetworkAvailableTest() {
        when(mockNetworkUtils.isNetworkAvailable()).thenReturn(true);

        viewModel.startDownloadingFormList();

        Mockito.verify(mockFormDownloadRepository, times(1)).downloadFormList(any(), any(), any());
    }

    @Test
    public void displayErrorWhenDownloadingFormListIfNetworkUnavailableTest() {
        when(mockNetworkUtils.isNetworkAvailable()).thenReturn(false);

        viewModel.startDownloadingFormList();

        // assert that download task isn't triggered
        Mockito.verify(mockFormDownloadRepository, times(0)).downloadFormList(any(), any(), any());

        // finish the activity as well if in downloadOnly mode
        viewModel.setDownloadOnlyMode(true);
        viewModel.startDownloadingFormList();

        Mockito.verify(mockFormDownloadRepository, times(0)).downloadFormList(any(), any(), any());
        Mockito.verify(viewModel.getNavigator(), times(1)).setReturnResult(false, testResourceProvider.getString(R.string.no_connection), new HashMap<>());
        Mockito.verify(viewModel.getNavigator(), times(1)).goBack();
    }

    @Test
    public void cancelFormListDownloadTest() {
        when(mockFormDownloadRepository.downloadFormList(any(), any(), any())).thenReturn(Observable.just(new HashMap<>()));
        when(mockFormDownloadRepository.isLoading()).thenReturn(true);
        when(mockNetworkUtils.isNetworkAvailable()).thenReturn(true);

        viewModel.startDownloadingFormList();
        viewModel.cancelFormListDownloadTask();

        Disposable disposable = viewModel.getFormListDownloadDisposable();

        Assert.assertTrue(disposable == null || disposable.isDisposed());
        Mockito.verify(mockFormDownloadRepository, times(1)).downloadFormList(any(), any(), any());
    }

    @Test
    public void finishActivityIfFormListCanceledInDownloadOnlyModeTest() {
        when(mockFormDownloadRepository.downloadFormList(any(), any(), any())).thenReturn(Observable.just(new HashMap<>()));
        when(mockFormDownloadRepository.isLoading()).thenReturn(true);
        when(mockNetworkUtils.isNetworkAvailable()).thenReturn(true);

        viewModel.setDownloadOnlyMode(true);
        viewModel.startDownloadingFormList();
        viewModel.cancelFormListDownloadTask();

        Disposable disposable = viewModel.getFormListDownloadDisposable();

        Assert.assertTrue(disposable == null || disposable.isDisposed());
        Mockito.verify(mockFormDownloadRepository, times(1)).downloadFormList(any(), any(), any());
        Mockito.verify(viewModel.getNavigator(), times(1)).setReturnResult(false, "User cancelled the operation", new HashMap<>());
        Mockito.verify(viewModel.getNavigator(), times(1)).goBack();
    }

    @Test
    public void formDownloadTaskTest() throws InterruptedException {
        when(mockFormDownloadRepository.getFormDownloadProgress()).thenReturn(new Observable<String>() {
            @Override
            protected void subscribeActual(Observer<? super String> observer) {
                observer.onNext("message 1");
                observer.onNext("message 2");
                observer.onNext("message 3");
            }
        });

        List<FormDetails> formsToDownload = new ArrayList<>();
        formsToDownload.add(Mockito.mock(FormDetails.class));

        HashMap<FormDetails, String> result = new HashMap<>();
        result.put(formsToDownload.get(0), "value");

        when(mockFormDownloadRepository.downloadForms(formsToDownload)).thenReturn(new Observable<HashMap<FormDetails, String>>() {
            @Override
            protected void subscribeActual(Observer<? super HashMap<FormDetails, String>> observer) {
                observer.onNext(result);
            }
        });

        viewModel.setDownloadOnlyMode(true);
        viewModel.startDownloadingForms(formsToDownload);

        // assert that the progress dialog was displayed and then dismissed later
        Assert.assertEquals(false, getValue(viewModel.getProgressDialog()));

        // assert that the progress dialog showed the following messages in the same order
        Assert.assertEquals("message 3", getValue(viewModel.getProgressDialogMessage()));

        // assert that success message was displayed
        AlertDialogUiModel actualModel = getValue(viewModel.getAlertDialog());
        Assert.assertEquals(testResourceProvider.getString(R.string.download_forms_result), actualModel.getTitle());
        Assert.assertEquals(getDownloadResultMessage(result), actualModel.getMessage());
        Assert.assertTrue(actualModel.shouldExit());

        // assert that result was properly set
        if (viewModel.getNavigator() != null) {
            Mockito.verify(viewModel.getNavigator(), times(1)).setReturnResult(true, null, viewModel.getFormResults());
        }
    }

    @Test
    public void cancelFormDownloadTaskTest() throws InterruptedException {
        when(mockFormDownloadRepository.isLoading()).thenReturn(true);

        List<FormDetails> formsToDownload = new ArrayList<>();
        formsToDownload.add(Mockito.mock(FormDetails.class));

        viewModel.startDownloadingForms(formsToDownload);
        viewModel.cancelFormDownloadTask();

        Disposable disposable = viewModel.getFormDownloadDisposable();

        // assert that cancel dialog was displayed
        Assert.assertEquals(true, getValue(viewModel.getCancelDialog()));

        // assert that progress dialog was earlier being displayed and then dismissed
        Assert.assertEquals(false, getValue(viewModel.getProgressDialog()));

        // assert that download task was disposed
        Assert.assertTrue(disposable == null || disposable.isDisposed());
    }

    @Test
    public void showAuthDialogIfUnauthorizedWhenDownloadingFormsTest() throws InterruptedException {
        when(mockNetworkUtils.isNetworkAvailable()).thenReturn(true);
        when(mockFormDownloadRepository.downloadFormList(any(), any(), any())).thenReturn(new Observable<HashMap<String, FormDetails>>() {
            @Override
            protected void subscribeActual(Observer<? super HashMap<String, FormDetails>> observer) {
                HashMap<String, FormDetails> result = new HashMap<>();
                result.put(DL_AUTH_REQUIRED, new FormDetails("authorization error"));

                observer.onNext(result);
            }
        });

        viewModel.startDownloadingFormList();

        Assert.assertNotNull(getValue(viewModel.getAuthDialog()));
        Assert.assertNull(getValue(viewModel.getFormDownloadList()));
    }
}
