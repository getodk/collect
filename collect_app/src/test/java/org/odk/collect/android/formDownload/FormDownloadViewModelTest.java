package org.odk.collect.android.formDownload;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.ui.formDownload.AlertDialogUiModel;
import org.odk.collect.android.ui.formDownload.FormDownloadViewModel;
import org.odk.collect.android.utilities.rx.TestSchedulerProvider;

import io.reactivex.observers.TestObserver;

public class FormDownloadViewModelTest {

    private FormDownloadViewModel viewModel;

    private TestObserver<AlertDialogUiModel> alertDialogTestSubscriber;

    @Before
    public void setUp() {
        viewModel = new FormDownloadViewModel(new TestSchedulerProvider());

        alertDialogTestSubscriber = new TestObserver<>();
    }

    @After
    public void tearDown() {
        viewModel = null;
    }

    @Test
    public void alertDialogTest() {
        viewModel.getAlertDialog().subscribe(alertDialogTestSubscriber);

        alertDialogTestSubscriber.assertNoValues();
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
}
