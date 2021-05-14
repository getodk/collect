package org.odk.collect.android.fragments.dialogs;

import android.content.DialogInterface;

import androidx.fragment.app.FragmentManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.fragments.support.DialogFragmentHelpers;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.testshared.RobolectricHelpers;

import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class BikramSambatDatePickerDialogTest {
    private FragmentManager fragmentManager;
    private BikramSambatDatePickerDialog dialogFragment;
    private DatePickerDetails datePickerDetails;
    private DialogFragmentHelpers.DatePickerTestActivity activity;

    @Before
    public void setup() {
        activity = CollectHelpers.createThemedActivity(DialogFragmentHelpers.DatePickerTestActivity.class);
        fragmentManager = activity.getSupportFragmentManager();

        dialogFragment = new BikramSambatDatePickerDialog();
        datePickerDetails = DialogFragmentHelpers.setUpDatePickerDetails(DatePickerDetails.DatePickerType.BIKRAM_SAMBAT);
        dialogFragment.setArguments(DialogFragmentHelpers.getDialogFragmentArguments(datePickerDetails));
    }

    @Test
    public void dialogIsCancellable() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDialogIsCancellable(true);
    }

    @Test
    public void dialogShouldShowCorrectDate() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDialogShowsCorrectDate(2077, 0, 30, "30 बैशाख 2077 (2020May12)");
    }

    @Test
    public void dialogShouldShowCorrectDate_forYearMode() {
        when(datePickerDetails.isYearMode()).thenReturn(true);
        when(datePickerDetails.isSpinnerMode()).thenReturn(false);

        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDialogShowsCorrectDateForYearMode(2077, "2076 (2020)");
    }

    @Test
    public void dialogShouldShowCorrectDate_forMonthMode() {
        when(datePickerDetails.isMonthYearMode()).thenReturn(true);
        when(datePickerDetails.isSpinnerMode()).thenReturn(false);

        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDialogShowsCorrectDateForMonthMode(2077, 0, "चैत 2076 (2020Apr)");
    }

    @Test
    public void settingDateInDatePicker_changesDateShownInTextView() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDialogTextViewUpdatesDate("30 बैशाख 2077 (2020May12)");
    }

    @Test
    public void whenScreenIsRotated_dialogShouldRetainDateInDatePickerAndTextView() {
        DialogFragmentHelpers.assertDialogRetainsDateOnScreenRotation(dialogFragment, "12 असोज 2020 (1963Sep28)");
    }

    @Test
    public void clickingOk_updatesDateInActivity() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDateUpdateInActivity(activity, 1963, 9, 28);
    }

    @Test
    public void clickingOk_dismissesTheDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDialogIsDismissedOnButtonClick(DialogInterface.BUTTON_POSITIVE);
    }

    @Test
    public void clickingCancel_dismissesTheDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        DialogFragmentHelpers.assertDialogIsDismissedOnButtonClick(DialogInterface.BUTTON_NEGATIVE);
    }
}
