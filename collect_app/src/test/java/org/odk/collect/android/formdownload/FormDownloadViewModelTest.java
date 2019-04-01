package org.odk.collect.android.formdownload;

import android.os.Bundle;

import org.junit.Assert;
import org.junit.Before;
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

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class FormDownloadViewModelTest {

    private FormDownloadViewModel viewModel;

    private TestObserver<AlertDialogUiModel> alertDialogTestSubscriber;
    private TestObserver<Boolean> progressDialogTestSubscriber;
    private TestObserver<Boolean> cancelDialogTestSubscriber;
    private TestObserver<String> progressDialogMessageTestSubscriber;
    private TestObserver<HashMap<String, FormDetails>> formListDownloadTestSubscriber;

    private NetworkUtils mockNetworkUtils;
    private FormDownloadRepository mockFormDownloadRepository;
    private WebCredentialsUtils mockWebCredentialsUtils;
    private BaseResourceProvider testResourceProvider;

    @Before
    public void setUp() {
        mockNetworkUtils = Mockito.mock(NetworkUtils.class);
        mockFormDownloadRepository = Mockito.spy(FormDownloadRepository.class);
        mockWebCredentialsUtils = Mockito.mock(WebCredentialsUtils.class);
        testResourceProvider = new ResourceProvider(RuntimeEnvironment.application.getApplicationContext());

        viewModel = new FormDownloadViewModel(new TestSchedulerProvider(), mockNetworkUtils, testResourceProvider, mockFormDownloadRepository, mockWebCredentialsUtils);

        // prepare the spy!
        viewModel.setNavigator(Mockito.spy(FormDownloadNavigator.class));

        alertDialogTestSubscriber = new TestObserver<>();
        progressDialogTestSubscriber = new TestObserver<>();
        cancelDialogTestSubscriber = new TestObserver<>();
        progressDialogMessageTestSubscriber = new TestObserver<>();
        formListDownloadTestSubscriber = new TestObserver<>();
    }

    @Test
    public void doNotDisplayDialogsOnInitTest() {
        viewModel.getAlertDialog().subscribe(alertDialogTestSubscriber);
        alertDialogTestSubscriber.assertNoValues();

        viewModel.getProgressDialog().subscribe(progressDialogTestSubscriber);
        progressDialogTestSubscriber.assertNoValues();
    }

    @Test
    public void displaySameAlertDialogOnReopenTest() {
        AlertDialogUiModel expectedModel = new AlertDialogUiModel("Title", "Message", false);

        viewModel.setAlertDialog(expectedModel.getTitle(), expectedModel.getMessage(), expectedModel.shouldExit());

        // now assuming that the activity was recreated. So, the subject was resubscribed.
        viewModel.getAlertDialog().subscribe(alertDialogTestSubscriber);

        AlertDialogUiModel actualModel = (AlertDialogUiModel) alertDialogTestSubscriber.getEvents().get(0).get(0);

        Assert.assertEquals(expectedModel.getTitle(), actualModel.getTitle());
        Assert.assertEquals(expectedModel.getMessage(), actualModel.getMessage());
        Assert.assertEquals(expectedModel.shouldExit(), actualModel.shouldExit());
    }

    @Test
    public void doNotRestoreLastShownAlertDialogIfRemovedTest() {
        AlertDialogUiModel expectedModel = new AlertDialogUiModel("Title", "Message", false);

        viewModel.setAlertDialog(expectedModel.getTitle(), expectedModel.getMessage(), expectedModel.shouldExit());
        viewModel.removeAlertDialog();

        // now assuming that the activity was recreated. So, the subject was resubscribed.
        viewModel.getAlertDialog().subscribe(alertDialogTestSubscriber);

        alertDialogTestSubscriber.assertNoValues();
    }

    @Test
    public void displayProgressDialogTest() {
        viewModel.getProgressDialog().subscribe(progressDialogTestSubscriber);

        viewModel.setProgressDialogShowing(true);
        viewModel.setProgressDialogShowing(false);
        viewModel.setProgressDialogShowing(true);

        progressDialogTestSubscriber.assertValues(true, false, true);

        // re-subscription happens due to activity restoration
        progressDialogTestSubscriber = new TestObserver<>();
        viewModel.getProgressDialog().subscribe(progressDialogTestSubscriber);

        // last emiited value is immediately reported back
        progressDialogTestSubscriber.assertValue(true);
    }

    @Test
    public void displayCancelDialogTest() {
        viewModel.getCancelDialog().subscribe(cancelDialogTestSubscriber);

        viewModel.setCancelDialogShowing(true);
        viewModel.setCancelDialogShowing(false);
        viewModel.setCancelDialogShowing(true);

        cancelDialogTestSubscriber.assertValues(true, false, true);

        // re-subscription happens due to activity restoration
        cancelDialogTestSubscriber = new TestObserver<>();
        viewModel.getCancelDialog().subscribe(cancelDialogTestSubscriber);

        // last emiited value is immediately reported back
        cancelDialogTestSubscriber.assertValue(true);
    }

    @Test
    public void displayLastShownProgressMessageTest() {
        viewModel.setProgressDialogMessage("Progress 1");

        // now assuming that the activity was recreated. So, the subject was resubscribed.
        viewModel.getProgressDialogMessage().subscribe(progressDialogMessageTestSubscriber);

        progressDialogMessageTestSubscriber.assertValue("Progress 1");
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

        viewModel.getFormDownloadList().subscribe(formListDownloadTestSubscriber);

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

        viewModel.getFormDownloadList().subscribe(formListDownloadTestSubscriber);

        viewModel.setDownloadOnlyMode(true);
        viewModel.startDownloadingFormList();
        viewModel.cancelFormListDownloadTask();

        Disposable disposable = viewModel.getFormListDownloadDisposable();

        Assert.assertTrue(disposable == null || disposable.isDisposed());
        Mockito.verify(mockFormDownloadRepository, times(1)).downloadFormList(any(), any(), any());
        Mockito.verify(viewModel.getNavigator(), times(1)).setReturnResult(false, "User cancelled the operation", new HashMap<>());
        Mockito.verify(viewModel.getNavigator(), times(1)).goBack();
    }
}
